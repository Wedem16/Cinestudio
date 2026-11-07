package com.example.media

import android.content.Context
import android.graphics.Bitmap
import android.media.MediaCodecList
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.util.Log
import com.example.model.VideoMetadata
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "VideoMetadataHelper"

object VideoMetadataHelper {

    fun isDevice4kCapable(): Boolean {
        return try {
            val codecList = MediaCodecList(MediaCodecList.REGULAR_CODECS)
            codecList.codecInfos.any { info ->
                info.isEncoder && info.supportedTypes.any { type ->
                    (type.equals("video/avc", ignoreCase = true) ||
                        type.equals("video/hevc", ignoreCase = true) ||
                        type.equals("video/vp9", ignoreCase = true)) &&
                        runCatching {
                            val caps = info.getCapabilitiesForType(type).videoCapabilities
                            caps != null && (caps.isSizeSupported(3840, 2160) || caps.isSizeSupported(2160, 3840))
                        }.getOrDefault(false)
                }
            }
        } catch (e: Exception) {
            true
        }
    }

    suspend fun extractMetadata(context: Context, videoUri: Uri): VideoMetadata = withContext(Dispatchers.IO) {
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)

            val title = videoUri.lastPathSegment ?: "Video Footage"
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 5000L

            val widthStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
            val heightStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
            val width = widthStr?.toIntOrNull() ?: 1920
            val height = heightStr?.toIntOrNull() ?: 1080

            val rotationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_VIDEO_ROTATION)
            val rotation = rotationStr?.toIntOrNull() ?: 0

            val frameRateStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_CAPTURE_FRAMERATE)
            val frameRate = frameRateStr?.toFloatOrNull() ?: 30.0f

            // Always allow 4K Cinema UHD mastering (device hardware or automatic encoder fallback)
            VideoMetadata(
                uri = videoUri,
                title = title,
                durationMs = durationMs,
                width = width,
                height = height,
                frameRate = frameRate,
                rotation = rotation,
                is4kSupported = true
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to extract metadata for $videoUri", e)
            VideoMetadata(
                uri = videoUri,
                title = "Video Clip",
                durationMs = 5000L,
                width = 1920,
                height = 1080,
                frameRate = 30.0f,
                rotation = 0,
                is4kSupported = true
            )
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
    }

    /**
     * Extracts representative keyframes (start, middle, end) scaled down for Gemini multimodal analysis.
     */
    suspend fun extractKeyframes(context: Context, videoUri: Uri, count: Int = 3): List<Bitmap> = withContext(Dispatchers.IO) {
        val bitmaps = mutableListOf<Bitmap>()
        val retriever = MediaMetadataRetriever()
        try {
            retriever.setDataSource(context, videoUri)
            val durationStr = retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
            val durationMs = durationStr?.toLongOrNull() ?: 5000L
            val durationUs = durationMs * 1000L

            // Pick timepoints at 15%, 50%, 85%
            val sampleFractions = when (count) {
                1 -> listOf(0.5f)
                5 -> listOf(0.10f, 0.30f, 0.50f, 0.70f, 0.90f)
                else -> listOf(0.15f, 0.50f, 0.85f)
            }

            for (frac in sampleFractions) {
                val timeUs = (durationUs * frac).toLong().coerceIn(0L, durationUs)
                val frame = retriever.getFrameAtTime(timeUs, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)
                if (frame != null) {
                    val maxDim = 720
                    val width = frame.width
                    val height = frame.height
                    val scale = if (width > maxDim || height > maxDim) {
                        maxDim.toFloat() / maxOf(width, height)
                    } else 1.0f

                    val scaled = if (scale < 1.0f) {
                        Bitmap.createScaledBitmap(
                            frame,
                            (width * scale).toInt().coerceAtLeast(1),
                            (height * scale).toInt().coerceAtLeast(1),
                            true
                        )
                    } else {
                        frame
                    }
                    bitmaps.add(scaled)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error extracting keyframes", e)
        } finally {
            try {
                retriever.release()
            } catch (ignored: Exception) {}
        }
        bitmaps
    }
}
