package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.model.ColorGradeParams
import com.example.ui.theme.CinemaAmber
import com.example.ui.theme.CinemaCyan
import com.example.ui.theme.SurfaceHighlight
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun AdjustmentsDeckView(
    params: ColorGradeParams,
    onExposureChanged: (Float) -> Unit,
    onContrastChanged: (Float) -> Unit,
    onSaturationChanged: (Float) -> Unit,
    onTemperatureChanged: (Float) -> Unit,
    onTintChanged: (Float) -> Unit,
    onHighlightsChanged: (Float) -> Unit,
    onShadowsChanged: (Float) -> Unit,
    onSharpenChanged: (Float) -> Unit,
    onVignetteChanged: (Float) -> Unit,
    onGestureEnd: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .testTag("adjustments_deck_view"),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        ColorSliderItem(
            label = "EXPOSURE",
            value = params.exposure,
            valueRange = -1.0f..1.0f,
            formatValue = { String.format("%+.2f EV", it) },
            onValueChange = onExposureChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_exposure"
        )

        ColorSliderItem(
            label = "CONTRAST",
            value = params.contrast,
            valueRange = -1.0f..1.0f,
            formatValue = { String.format("%+.0f%%", it * 100) },
            onValueChange = onContrastChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_contrast"
        )

        ColorSliderItem(
            label = "SATURATION",
            value = params.saturation,
            valueRange = -1.0f..1.0f,
            formatValue = { String.format("%+.0f%%", it * 100) },
            onValueChange = onSaturationChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_saturation"
        )

        ColorSliderItem(
            label = "TEMPERATURE (KELVIN)",
            value = params.temperature,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) String.format("Warm +%.0f", it * 100) else String.format("Cool %.0f", it * 100) },
            onValueChange = onTemperatureChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_temperature"
        )

        ColorSliderItem(
            label = "TINT (GREEN / MAGENTA)",
            value = params.tint,
            valueRange = -1.0f..1.0f,
            formatValue = { if (it > 0) String.format("Magenta +%.0f", it * 100) else String.format("Green %.0f", it * 100) },
            onValueChange = onTintChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_tint"
        )

        ColorSliderItem(
            label = "HIGHLIGHTS",
            value = params.highlights,
            valueRange = -1.0f..1.0f,
            formatValue = { String.format("%+.0f%%", it * 100) },
            onValueChange = onHighlightsChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_highlights"
        )

        ColorSliderItem(
            label = "SHADOWS",
            value = params.shadows,
            valueRange = -1.0f..1.0f,
            formatValue = { String.format("%+.0f%%", it * 100) },
            onValueChange = onShadowsChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_shadows"
        )

        ColorSliderItem(
            label = "SHARPEN",
            value = params.sharpen,
            valueRange = 0.0f..1.0f,
            formatValue = { String.format("%.0f%%", it * 100) },
            onValueChange = onSharpenChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_sharpen"
        )

        ColorSliderItem(
            label = "VIGNETTE",
            value = params.vignette,
            valueRange = 0.0f..1.0f,
            formatValue = { String.format("%.0f%%", it * 100) },
            onValueChange = onVignetteChanged,
            onValueChangeFinished = onGestureEnd,
            testTag = "slider_vignette"
        )
    }
}

@Composable
private fun ColorSliderItem(
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    formatValue: (Float) -> String,
    onValueChange: (Float) -> Unit,
    onValueChangeFinished: () -> Unit,
    testTag: String
) {
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
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = formatValue(value),
                style = MaterialTheme.typography.labelMedium,
                color = if (value != 0.0f) CinemaCyan else TextPrimary
            )
        }

        Slider(
            value = value,
            onValueChange = onValueChange,
            onValueChangeFinished = onValueChangeFinished,
            valueRange = valueRange,
            colors = SliderDefaults.colors(
                thumbColor = if (value != 0f) CinemaCyan else TextSecondary,
                activeTrackColor = CinemaCyan,
                inactiveTrackColor = SurfaceHighlight
            ),
            modifier = Modifier
                .fillMaxWidth()
                .testTag(testTag)
        )
    }
}
