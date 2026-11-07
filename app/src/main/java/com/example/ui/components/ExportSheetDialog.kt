package com.example.ui.components

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.content.FileProvider
import com.example.model.ExportFrameRate
import com.example.model.ExportResolution
import com.example.model.ExportState
import com.example.model.VideoMetadata
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaGreen
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DeckSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import java.io.File

@Composable
fun ExportSheetDialog(
    metadata: VideoMetadata?,
    exportState: ExportState,
    onStartExport: (ExportResolution, ExportFrameRate) -> Unit,
    onCancelExport: () -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current
    var selectedRes by remember { mutableStateOf(ExportResolution.FHD_1080P) }
    var selectedFps by remember { mutableStateOf(ExportFrameRate.ORIGINAL) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = DeckSurface,
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp)
                .testTag("export_dialog")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = null,
                            tint = CinemaCyan
                        )
                        Text(
                            text = "CINEMA RENDER & EXPORT",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimary
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = TextSecondary)
                    }
                }

                when (exportState) {
                    is ExportState.Idle -> {
                        // Resolution Selection (2x2 Grid so 4K UHD is prominent and never truncated)
                        Text(
                            text = "TARGET RESOLUTION",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )

                        val resolutionRows = ExportResolution.entries.chunked(2)
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (rowItems in resolutionRows) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    for (res in rowItems) {
                                        val isSelected = res == selectedRes
                                        val subLabel = when (res) {
                                            ExportResolution.ORIGINAL -> metadata?.resolutionLabel ?: "Source Size"
                                            ExportResolution.HD_720P -> "1280 × 720"
                                            ExportResolution.FHD_1080P -> "1920 × 1080"
                                            ExportResolution.UHD_4K -> "3840 × 2160 UHD"
                                        }

                                        Surface(
                                            color = if (isSelected) CinemaCyan.copy(alpha = 0.16f) else SurfaceElevated,
                                            shape = RoundedCornerShape(8.dp),
                                            border = androidx.compose.foundation.BorderStroke(
                                                width = if (isSelected) 1.5.dp else 1.dp,
                                                color = if (isSelected) CinemaCyan else BorderSubtle
                                            ),
                                            modifier = Modifier
                                                .weight(1f)
                                                .clickable { selectedRes = res }
                                                .testTag("res_option_${res.name}")
                                        ) {
                                            Column(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 10.dp),
                                                horizontalAlignment = Alignment.Start,
                                                verticalArrangement = Arrangement.spacedBy(2.dp)
                                            ) {
                                                Text(
                                                    text = res.label,
                                                    style = MaterialTheme.typography.labelLarge,
                                                    color = if (isSelected) CinemaCyan else TextPrimary,
                                                    maxLines = 1
                                                )
                                                Text(
                                                    text = subLabel,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = TextSecondary,
                                                    maxLines = 1
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        // Frame Rate Selection
                        Text(
                            text = "FRAME RATE",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (fps in ExportFrameRate.entries) {
                                val isSelected = fps == selectedFps
                                Surface(
                                    color = if (isSelected) CinemaCyan.copy(alpha = 0.16f) else SurfaceElevated,
                                    shape = RoundedCornerShape(8.dp),
                                    border = androidx.compose.foundation.BorderStroke(
                                        width = if (isSelected) 1.5.dp else 1.dp,
                                        color = if (isSelected) CinemaCyan else BorderSubtle
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { selectedFps = fps }
                                        .testTag("fps_option_${fps.name}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 10.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = fps.label,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (isSelected) CinemaCyan else TextPrimary,
                                            maxLines = 1
                                        )
                                    }
                                }
                            }
                        }

                        // Info note
                        Surface(
                            color = SurfaceElevated,
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(
                                text = "• Original audio track preserved intact\n• Hardware H.264/HEVC MP4 mastering (${selectedRes.label})\n• Automatically saved to device Movies gallery",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(10.dp)
                            )
                        }

                        // Start Render Button
                        Button(
                            onClick = { onStartExport(selectedRes, selectedFps) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = CinemaCyan,
                                contentColor = ObsidianBg
                            ),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(48.dp)
                                .testTag("start_export_button")
                        ) {
                            Text(
                                text = "START ${selectedRes.label.uppercase()} RENDER",
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    }

                    is ExportState.Exporting -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Text(
                                text = "RENDERING ${selectedRes.label.uppercase()} MASTER...",
                                style = MaterialTheme.typography.labelMedium,
                                color = CinemaCyan
                            )

                            LinearProgressIndicator(
                                progress = { exportState.progressPercent / 100f },
                                color = CinemaCyan,
                                trackColor = SurfaceHighlight,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(8.dp)
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "${exportState.progressPercent}% Completed",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Elapsed: ${exportState.elapsedMs / 1000}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            OutlinedButton(
                                onClick = onCancelExport,
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = CinemaRed),
                                modifier = Modifier.testTag("cancel_export_button")
                            ) {
                                Text("CANCEL EXPORT")
                            }
                        }
                    }

                    is ExportState.Completed -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = CinemaGreen,
                                modifier = Modifier.size(48.dp)
                            )

                            Text(
                                text = "RENDER COMPLETE!",
                                style = MaterialTheme.typography.titleLarge,
                                color = CinemaGreen
                            )

                            Text(
                                text = "Saved to Gallery Movies/CineAI Studio",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Button(
                                    onClick = {
                                        shareExportedVideo(context, exportState.outputPath)
                                    },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CinemaCyan,
                                        contentColor = ObsidianBg
                                    ),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("share_video_button")
                                ) {
                                    Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text("SHARE VIDEO")
                                }

                                OutlinedButton(
                                    onClick = onDismiss,
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.weight(1f)
                                ) {
                                    Text("DONE")
                                }
                            }
                        }
                    }

                    is ExportState.Error -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Error,
                                contentDescription = null,
                                tint = CinemaRed,
                                modifier = Modifier.size(40.dp)
                            )
                            Text(
                                text = "EXPORT FAILED",
                                style = MaterialTheme.typography.titleMedium,
                                color = CinemaRed
                            )
                            Text(
                                text = exportState.message,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimary
                            )
                            Button(
                                onClick = onDismiss,
                                colors = ButtonDefaults.buttonColors(containerColor = SurfaceElevated)
                            ) {
                                Text("CLOSE")
                            }
                        }
                    }

                    is ExportState.Cancelled -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text("Export was cancelled.", style = MaterialTheme.typography.bodyLarge, color = TextSecondary)
                            Button(onClick = onDismiss) { Text("CLOSE") }
                        }
                    }
                }
            }
        }
    }
}

private fun shareExportedVideo(context: Context, filePath: String) {
    try {
        val file = File(filePath)
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "video/mp4"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Share CineAI Master Video"))
    } catch (e: Exception) {
        android.util.Log.e("ExportSheet", "Share failed", e)
    }
}
