package com.example.model

import android.net.Uri

data class VideoMetadata(
    val uri: Uri,
    val title: String,
    val durationMs: Long,
    val width: Int,
    val height: Int,
    val frameRate: Float,
    val rotation: Int = 0,
    val is4kSupported: Boolean = false
) {
    val durationFormatted: String
        get() {
            val totalSeconds = (durationMs / 1000).coerceAtLeast(0)
            val minutes = totalSeconds / 60
            val seconds = totalSeconds % 60
            val millis = (durationMs % 1000) / 10
            return String.format("%02d:%02d:%02d", minutes, seconds, millis)
        }

    val resolutionLabel: String
        get() = "${width}x${height}"
}

enum class ExportResolution(val label: String, val height: Int) {
    ORIGINAL("Source Match", 0),
    HD_720P("720p HD", 720),
    FHD_1080P("1080p FHD", 1080),
    UHD_4K("4K Cinema UHD", 2160)
}

enum class ExportFrameRate(val label: String, val fps: Int) {
    ORIGINAL("Source FPS", 0),
    FPS_30("30 FPS Cinema", 30),
    FPS_60("60 FPS Smooth", 60)
}

sealed interface ExportState {
    data object Idle : ExportState
    data class Exporting(val progressPercent: Int, val elapsedMs: Long) : ExportState
    data class Completed(val outputPath: String, val galleryUri: Uri?) : ExportState
    data class Error(val message: String) : ExportState
    data object Cancelled : ExportState
}
