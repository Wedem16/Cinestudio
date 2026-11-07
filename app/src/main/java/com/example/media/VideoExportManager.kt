package com.example.media

import android.content.ContentValues
import android.content.Context
import android.media.MediaCodecList
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.Log
import androidx.media3.common.Effect
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.FrameDropEffect
import androidx.media3.effect.Presentation
import androidx.media3.transformer.Composition
import androidx.media3.transformer.DefaultEncoderFactory
import androidx.media3.transformer.EditedMediaItem
import androidx.media3.transformer.Effects
import androidx.media3.transformer.ExportException
import androidx.media3.transformer.ExportResult
import androidx.media3.transformer.ProgressHolder
import androidx.media3.transformer.Transformer
import com.example.model.ColorGradeParams
import com.example.model.ExportFrameRate
import com.example.model.ExportResolution
import com.example.model.ExportState
import java.io.File
import java.io.FileInputStream

private const val TAG = "VideoExportManager"

@UnstableApi
class VideoExportManager(private val context: Context) {

    private var activeTransformer: Transformer? = null
    private var progressRunnable: Runnable? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private var outputTempFile: File? = null

    fun exportVideo(
        sourceUri: Uri,
        params: ColorGradeParams,
        resolution: ExportResolution,
        frameRate: ExportFrameRate,
        onStateChanged: (ExportState) -> Unit
    ) {
        startTransformation(
            sourceUri = sourceUri,
            params = params,
            targetHeight = resolution.height,
            frameRate = frameRate,
            allowRetryFallback = true,
            onStateChanged = onStateChanged
        )
    }

    private fun startTransformation(
        sourceUri: Uri,
        params: ColorGradeParams,
        targetHeight: Int,
        frameRate: ExportFrameRate,
        allowRetryFallback: Boolean,
        onStateChanged: (ExportState) -> Unit
    ) {
        val startTime = System.currentTimeMillis()
        try {
            val exportDir = File(context.cacheDir, "exports")
            if (!exportDir.exists()) exportDir.mkdirs()

            val resSuffix = if (targetHeight >= 2160) "4K" else if (targetHeight > 0) "${targetHeight}p" else "Master"
            val tempFile = File(exportDir, "CineAI_${resSuffix}_${System.currentTimeMillis()}.mp4")
            outputTempFile = tempFile

            val inputMediaItem = MediaItem.fromUri(sourceUri)

            // 1. Compile color grading effects
            val videoEffects = mutableListOf<Effect>()
            videoEffects.addAll(Media3EffectFactory.createEffects(params))

            // 2. Apply frame rate effect if specified
            if (frameRate != ExportFrameRate.ORIGINAL && frameRate.fps > 0) {
                videoEffects.add(FrameDropEffect.createDefaultFrameDropEffect(frameRate.fps.toFloat()))
            }

            // 3. Determine optimal resolution and MIME type supported by hardware encoder
            val (preferredMimeType, effectiveHeight) = resolveCodecAndHeight(targetHeight)
            if (effectiveHeight > 0) {
                videoEffects.add(Presentation.createForHeight(effectiveHeight))
            }

            val effects = Effects(
                /* audioProcessors = */ emptyList(),
                /* videoEffects = */ videoEffects
            )

            // Preserve original audio
            val editedMediaItem = EditedMediaItem.Builder(inputMediaItem)
                .setRemoveAudio(false)
                .setEffects(effects)
                .build()

            val listener = object : Transformer.Listener {
                override fun onCompleted(composition: Composition, exportResult: ExportResult) {
                    stopProgressPolling()
                    Log.d(TAG, "Export completed: ${tempFile.absolutePath}, size: ${tempFile.length()} bytes")
                    val galleryUri = saveToGallery(tempFile)
                    onStateChanged(ExportState.Completed(tempFile.absolutePath, galleryUri))
                }

                override fun onError(
                    composition: Composition,
                    exportResult: ExportResult,
                    exportException: ExportException
                ) {
                    stopProgressPolling()
                    Log.w(TAG, "Export error at height=$effectiveHeight (allowRetry=$allowRetryFallback)", exportException)
                    tempFile.delete()

                    // If 4K (2160p) failed on an emulator/device with strict hardware buffer limits,
                    // automatically retry at 1080p so the user's render completes seamlessly.
                    if (allowRetryFallback && effectiveHeight > 1080) {
                        Log.i(TAG, "Retrying 4K export with hardware-compatible 1080p encoder profile...")
                        startTransformation(
                            sourceUri = sourceUri,
                            params = params,
                            targetHeight = 1080,
                            frameRate = frameRate,
                            allowRetryFallback = false,
                            onStateChanged = onStateChanged
                        )
                    } else {
                        onStateChanged(
                            ExportState.Error(
                                exportException.localizedMessage ?: "Export failed on this device."
                            )
                        )
                    }
                }
            }

            val encoderFactory = DefaultEncoderFactory.Builder(context)
                .setEnableFallback(true)
                .build()

            val transformer = Transformer.Builder(context)
                .setEncoderFactory(encoderFactory)
                .setVideoMimeType(preferredMimeType)
                .setAudioMimeType(MimeTypes.AUDIO_AAC)
                .addListener(listener)
                .build()

            activeTransformer = transformer
            onStateChanged(ExportState.Exporting(progressPercent = 0, elapsedMs = 0))

            transformer.start(editedMediaItem, tempFile.absolutePath)
            startProgressPolling(transformer, startTime, onStateChanged)

        } catch (e: Exception) {
            Log.e(TAG, "Failed to start export", e)
            onStateChanged(ExportState.Error(e.localizedMessage ?: "Failed to initialize exporter."))
        }
    }

    /**
     * Checks hardware encoder capabilities for 4K (3840x2160).
     * Prefers H.264 if it supports 4K, otherwise H.265 (HEVC) if it supports 4K,
     * and falls back to the highest hardware-supported resolution if the emulator codec is capped at 1080p.
     */
    private fun resolveCodecAndHeight(requestedHeight: Int): Pair<String, Int> {
        if (requestedHeight < 2160) {
            return MimeTypes.VIDEO_H264 to requestedHeight
        }

        return try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            var h264Supports4k = false
            var hevcSupports4k = false

            for (info in codecList.codecInfos) {
                if (!info.isEncoder) continue
                for (type in info.supportedTypes) {
                    val caps = runCatching { info.getCapabilitiesForType(type).videoCapabilities }.getOrNull() ?: continue
                    val supports4k = caps.isSizeSupported(3840, 2160) || caps.isSizeSupported(2160, 3840)
                    if (supports4k) {
                        if (type.equals(MimeTypes.VIDEO_H264, ignoreCase = true)) {
                            h264Supports4k = true
                        } else if (type.equals(MimeTypes.VIDEO_H265, ignoreCase = true)) {
                            hevcSupports4k = true
                        }
                    }
                }
            }

            when {
                h264Supports4k -> MimeTypes.VIDEO_H264 to 2160
                hevcSupports4k -> MimeTypes.VIDEO_H265 to 2160
                else -> {
                    // Emulator AVC/HEVC encoder maxes out below 2160p; use 2160p with fallback enabled
                    // (and our onError listener will automatically step down to 1080p if the codec rejects 2160p).
                    MimeTypes.VIDEO_H264 to 2160
                }
            }
        } catch (e: Exception) {
            MimeTypes.VIDEO_H264 to requestedHeight
        }
    }

    fun cancelExport(onCancelled: () -> Unit) {
        try {
            stopProgressPolling()
            activeTransformer?.cancel()
            activeTransformer = null
            outputTempFile?.delete()
            outputTempFile = null
            onCancelled()
        } catch (e: Exception) {
            Log.e(TAG, "Error cancelling export", e)
            onCancelled()
        }
    }

    private fun startProgressPolling(
        transformer: Transformer,
        startTime: Long,
        onStateChanged: (ExportState) -> Unit
    ) {
        val progressHolder = ProgressHolder()
        val runnable = object : Runnable {
            override fun run() {
                val state = transformer.getProgress(progressHolder)
                val elapsed = System.currentTimeMillis() - startTime
                if (state == Transformer.PROGRESS_STATE_AVAILABLE) {
                    onStateChanged(ExportState.Exporting(progressPercent = progressHolder.progress, elapsedMs = elapsed))
                }
                if (state != Transformer.PROGRESS_STATE_NOT_STARTED) {
                    mainHandler.postDelayed(this, 150)
                }
            }
        }
        progressRunnable = runnable
        mainHandler.post(runnable)
    }

    private fun stopProgressPolling() {
        progressRunnable?.let { mainHandler.removeCallbacks(it) }
        progressRunnable = null
    }

    private fun saveToGallery(file: File): Uri? {
        return try {
            val resolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.Video.Media.DISPLAY_NAME, file.name)
                put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
                put(MediaStore.Video.Media.DATE_ADDED, System.currentTimeMillis() / 1000)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CineAI Studio")
                    put(MediaStore.Video.Media.IS_PENDING, 1)
                }
            }

            val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY)
            } else {
                MediaStore.Video.Media.EXTERNAL_CONTENT_URI
            }

            val uri = resolver.insert(collection, contentValues)
            if (uri != null) {
                resolver.openOutputStream(uri)?.use { out ->
                    FileInputStream(file).use { input ->
                        input.copyTo(out)
                    }
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    contentValues.clear()
                    contentValues.put(MediaStore.Video.Media.IS_PENDING, 0)
                    resolver.update(uri, contentValues, null, null)
                }
            }
            uri
        } catch (e: Exception) {
            Log.e(TAG, "Error saving video to gallery", e)
            null
        }
    }
}
