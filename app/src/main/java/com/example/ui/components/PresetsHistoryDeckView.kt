package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.PlayCircleOutline
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.CustomPresetEntity
import com.example.data.RecentProjectEntity
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CinemaAmber
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.CinemaRed
import com.example.ui.theme.DeckSurface
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PresetsHistoryDeckView(
    presets: List<CustomPresetEntity>,
    recentProjects: List<RecentProjectEntity>,
    onSaveCurrentPreset: () -> Unit,
    onApplyPreset: (CustomPresetEntity) -> Unit,
    onDeletePreset: (Long) -> Unit,
    onSelectRecentProject: (RecentProjectEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("presets_history_deck_view"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // Top action: Save Current Look as Preset
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "SAVED PRESETS (${presets.size})",
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )

            OutlinedButton(
                onClick = onSaveCurrentPreset,
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = CinemaCyan
                ),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_current_preset_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Bookmark,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text("SAVE CURRENT LOOK", style = MaterialTheme.typography.labelSmall)
            }
        }

        // Custom Presets List
        if (presets.isEmpty()) {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No saved presets yet. Fine-tune any grade and tap 'Save Current Look' to save it for future clips.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (preset in presets) {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onApplyPreset(preset) }
                            .testTag("preset_item_${preset.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    text = preset.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Base: ${preset.baseFilterId} • Intensity: ${(preset.filterIntensity * 100).toInt()}%",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondary
                                )
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Button(
                                    onClick = { onApplyPreset(preset) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = CinemaCyan.copy(alpha = 0.15f),
                                        contentColor = CinemaCyan
                                    ),
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.testTag("apply_preset_${preset.id}")
                                ) {
                                    Text("APPLY", style = MaterialTheme.typography.labelSmall)
                                }

                                IconButton(
                                    onClick = { onDeletePreset(preset.id) },
                                    modifier = Modifier.testTag("delete_preset_${preset.id}")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Preset",
                                        tint = TextSecondary,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        // Recent Projects History Section
        Text(
            text = "RECENT PROJECTS",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary
        )

        if (recentProjects.isEmpty()) {
            Surface(
                color = SurfaceElevated,
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "No recent project sessions.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    modifier = Modifier.padding(14.dp)
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                for (proj in recentProjects.take(5)) {
                    Surface(
                        color = SurfaceElevated,
                        shape = RoundedCornerShape(8.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSubtle),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelectRecentProject(proj) }
                            .testTag("recent_project_${proj.id}")
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = proj.videoTitle,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimary,
                                    maxLines = 1
                                )
                                Text(
                                    text = "${proj.resolutionLabel} • Look: ${proj.selectedFilterId}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = CinemaCyan
                                )
                                if (!proj.aiSummary.isNullOrBlank()) {
                                    Text(
                                        text = proj.aiSummary,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = TextSecondary,
                                        maxLines = 1,
                                        modifier = Modifier.padding(top = 2.dp)
                                    )
                                }
                            }

                            Icon(
                                imageVector = Icons.Default.PlayCircleOutline,
                                contentDescription = "Load Session",
                                tint = CinemaCyan,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}
