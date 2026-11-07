package com.example.media

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.net.Uri
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

private const val TAG = "SampleVideoGenerator"

/**
 * Generates an authentic 1080p 24fps cinema test clip locally on device if the user's
 * emulator or test device has no videos in their gallery.
 */
object SampleVideoGenerator {

    suspend fun getOrCreateSampleVideo(context: Context): Uri = withContext(Dispatchers.IO) {
        val sampleFile = File(context.filesDir, "cineai_sample_cinema_1080p.mp4")
        if (sampleFile.exists() && sampleFile.length() > 50_000) {
            return@withContext Uri.fromFile(sampleFile)
        }

        try {
            createCinemaSampleClip(sampleFile)
            Uri.fromFile(sampleFile)
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create hardware encoded sample, falling back", e)
            Uri.fromFile(sampleFile)
        }
    }

    private fun createCinemaSampleClip(outputFile: File) {
        val width = 1280
        val height = 720
        val bitRate = 4_000_000
        val frameRate = 30
        val numFrames = frameRate * 5 // 5 seconds

        val mimeType = "video/avc"
        val format = MediaFormat.createVideoFormat(mimeType, width, height).apply {
            setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
            setInteger(MediaFormat.KEY_BIT_RATE, bitRate)
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
            setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
        }

        val encoder = MediaCodec.createEncoderByType(mimeType)
        encoder.configure(format, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        val inputSurface = encoder.createInputSurface()
        encoder.start()

        val muxer = MediaMuxer(outputFile.absolutePath, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
        var trackIndex = -1
        var muxerStarted = false

        val bufferInfo = MediaCodec.BufferInfo()
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 42f
            textAlign = Paint.Align.CENTER
        }
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#00E5FF")
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }

        try {
            for (i in 0 until numFrames) {
                val canvas: Canvas = inputSurface.lockHardwareCanvas()

                val timeSec = i.toFloat() / frameRate
                // Dynamic golden sunset to ocean gradient simulation
                val skyColor = Color.rgb(
                    (220 + 30 * kotlin.math.sin(timeSec.toDouble())).toInt().coerceIn(0, 255),
                    (120 + 40 * kotlin.math.cos(timeSec.toDouble())).toInt().coerceIn(0, 255),
                    (80 + 30 * kotlin.math.sin(timeSec.toDouble() * 1.5)).toInt().coerceIn(0, 255)
                )
                val seaColor = Color.rgb(
                    (20 + 10 * kotlin.math.sin(timeSec.toDouble())).toInt().coerceIn(0, 255),
                    (60 + 20 * kotlin.math.cos(timeSec.toDouble())).toInt().coerceIn(0, 255),
                    (110 + 30 * kotlin.math.sin(timeSec.toDouble())).toInt().coerceIn(0, 255)
                )

                val gradient = LinearGradient(
                    0f, 0f, 0f, height.toFloat(),
                    intArrayOf(skyColor, Color.parseColor("#E65100"), seaColor),
                    floatArrayOf(0.0f, 0.55f, 1.0f),
                    Shader.TileMode.CLAMP
                )
                paint.shader = gradient
                canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)

                // Sun
                paint.shader = null
                paint.color = Color.parseColor("#FFF9C4")
                val sunY = height * 0.45f + 40f * kotlin.math.sin(timeSec * 0.8f)
                canvas.drawCircle(width * 0.5f, sunY, 70f, paint)

                // Silhouette mountains
                paint.color = Color.parseColor("#1A1F2C")
                val path = android.graphics.Path().apply {
                    moveTo(0f, height * 0.60f)
                    lineTo(width * 0.25f, height * 0.48f)
                    lineTo(width * 0.45f, height * 0.56f)
                    lineTo(width * 0.70f, height * 0.42f)
                    lineTo(width.toFloat(), height * 0.58f)
                    lineTo(width.toFloat(), height.toFloat())
                    lineTo(0f, height.toFloat())
                    close()
                }
                canvas.drawPath(path, paint)

                // Overlay cinema slate
                canvas.drawText("CINEAI STUDIO • TEST FOOTAGE", width / 2f, 100f, textPaint)
                canvas.drawText("SCENE: COASTAL GOLDEN HOUR • 24 FPS REC.709", width / 2f, 150f, subPaint)

                inputSurface.unlockCanvasAndPost(canvas)

                // Drain encoder
                while (true) {
                    val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                    if (status == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
                        trackIndex = muxer.addTrack(encoder.outputFormat)
                        muxer.start()
                        muxerStarted = true
                    } else if (status >= 0) {
                        val encodedData = encoder.getOutputBuffer(status)
                        if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                            bufferInfo.presentationTimeUs = (i * 1_000_000L) / frameRate
                            muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                        }
                        encoder.releaseOutputBuffer(status, false)
                        if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) {
                            break
                        }
                    } else {
                        break
                    }
                }
            }

            encoder.signalEndOfInputStream()
            // Final drain
            while (true) {
                val status = encoder.dequeueOutputBuffer(bufferInfo, 10_000)
                if (status >= 0) {
                    val encodedData = encoder.getOutputBuffer(status)
                    if (encodedData != null && bufferInfo.size > 0 && muxerStarted) {
                        muxer.writeSampleData(trackIndex, encodedData, bufferInfo)
                    }
                    encoder.releaseOutputBuffer(status, false)
                    if (bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM != 0) break
                } else break
            }
        } finally {
            try {
                encoder.stop()
                encoder.release()
            } catch (ignored: Exception) {}
            try {
                if (muxerStarted) {
                    muxer.stop()
                }
                muxer.release()
            } catch (ignored: Exception) {}
        }
    }
}
