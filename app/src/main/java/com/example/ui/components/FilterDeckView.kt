package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.CinemaFilter
import com.example.ui.theme.BorderSubtle
import com.example.ui.theme.CinemaAmber
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.ObsidianBg
import com.example.ui.theme.SurfaceElevated
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun FilterDeckView(
    selectedFilter: CinemaFilter,
    intensity: Float,
    onFilterSelected: (CinemaFilter) -> Unit,
    onIntensityChanged: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("filter_deck_view"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Master Filter Intensity Slider (0–100%)
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "FILTER INTENSITY",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary
                )
                Text(
                    text = "${(intensity * 100).toInt()}%",
                    style = MaterialTheme.typography.labelLarge,
                    color = CinemaCyan
                )
            }

            Slider(
                value = intensity,
                onValueChange = onIntensityChanged,
                valueRange = 0.0f..1.0f,
                colors = SliderDefaults.colors(
                    thumbColor = CinemaCyan,
                    activeTrackColor = CinemaCyan,
                    inactiveTrackColor = SurfaceHighlight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("filter_intensity_slider")
            )
        }

        // Horizontal Carousel of the 7 Signature Cinema Filters
        Text(
            text = "CINEMA LOOKS",
            style = MaterialTheme.typography.labelSmall,
            color = TextSecondary,
            modifier = Modifier.padding(horizontal = 4.dp)
        )

        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
            items(CinemaFilter.entries) { filter ->
                val isSelected = filter == selectedFilter

                Surface(
                    color = if (isSelected) SurfaceElevated else ObsidianBg,
                    shape = RoundedCornerShape(10.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        width = if (isSelected) 1.5.dp else 1.dp,
                        color = if (isSelected) CinemaCyan else BorderSubtle
                    ),
                    modifier = Modifier
                        .width(135.dp)
                        .clickable { onFilterSelected(filter) }
                        .testTag("filter_card_${filter.id}")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(8.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        // Color swatch preview swatch showing the filter's dual tone grading
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .clip(RoundedCornerShape(6.dp))
                                .background(
                                    Brush.linearGradient(
                                        colors = listOf(
                                            Color(
                                                (filter.tintR * 80).toInt().coerceIn(0, 255),
                                                (filter.tintG * 80).toInt().coerceIn(0, 255),
                                                (filter.tintB * 80).toInt().coerceIn(0, 255)
                                            ),
                                            Color(
                                                (filter.shadowTintR * 30).toInt().coerceIn(0, 255),
                                                (filter.shadowTintG * 30).toInt().coerceIn(0, 255),
                                                (filter.shadowTintB * 30).toInt().coerceIn(0, 255)
                                            )
                                        )
                                    )
                                ),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            if (isSelected) {
                                Box(
                                    modifier = Modifier
                                        .padding(4.dp)
                                        .size(18.dp)
                                        .clip(CircleShape)
                                        .background(CinemaCyan),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "Selected",
                                        tint = ObsidianBg,
                                        modifier = Modifier.size(12.dp)
                                    )
                                }
                            }
                        }

                        Text(
                            text = filter.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (isSelected) CinemaCyan else TextPrimary,
                            maxLines = 1
                        )

                        Text(
                            text = filter.subtitle,
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
