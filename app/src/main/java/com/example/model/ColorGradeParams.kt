package com.example.model

data class ColorGradeParams(
    val selectedFilter: CinemaFilter = CinemaFilter.NONE,
    val filterIntensity: Float = 1.0f, // 0.0f..1.0f (0..100%)
    val exposure: Float = 0.0f,        // -1.0f..1.0f
    val contrast: Float = 0.0f,        // -1.0f..1.0f
    val saturation: Float = 0.0f,      // -1.0f..1.0f
    val temperature: Float = 0.0f,     // -1.0f..1.0f (cool to warm)
    val tint: Float = 0.0f,            // -1.0f..1.0f (green to magenta)
    val highlights: Float = 0.0f,      // -1.0f..1.0f
    val shadows: Float = 0.0f,         // -1.0f..1.0f
    val sharpen: Float = 0.0f,         // 0.0f..1.0f
    val vignette: Float = 0.0f         // 0.0f..1.0f
) {
    fun isNeutral(): Boolean {
        return selectedFilter == CinemaFilter.NONE &&
                exposure == 0.0f &&
                contrast == 0.0f &&
                saturation == 0.0f &&
                temperature == 0.0f &&
                tint == 0.0f &&
                highlights == 0.0f &&
                shadows == 0.0f &&
                sharpen == 0.0f &&
                vignette == 0.0f
    }

    /**
     * Compute effective combined parameters by blending the base preset values
     * (scaled by filterIntensity) with user manual slider offsets.
     */
    fun computeEffectiveExposure(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseExposure * filterIntensity else 0.0f
        return (base + exposure).coerceIn(-1.5f, 1.5f)
    }

    fun computeEffectiveContrast(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseContrast * filterIntensity else 0.0f
        return (base + contrast).coerceIn(-1.0f, 1.0f)
    }

    fun computeEffectiveSaturation(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseSaturation * filterIntensity else 0.0f
        return (base + saturation).coerceIn(-1.0f, 1.5f)
    }

    fun computeEffectiveTemperature(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseTemperature * filterIntensity else 0.0f
        return (base + temperature).coerceIn(-1.0f, 1.0f)
    }

    fun computeEffectiveTint(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseTint * filterIntensity else 0.0f
        return (base + tint).coerceIn(-1.0f, 1.0f)
    }

    fun computeEffectiveHighlights(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseHighlights * filterIntensity else 0.0f
        return (base + highlights).coerceIn(-1.0f, 1.0f)
    }

    fun computeEffectiveShadows(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseShadows * filterIntensity else 0.0f
        return (base + shadows).coerceIn(-1.0f, 1.0f)
    }

    fun computeEffectiveSharpen(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseSharpen * filterIntensity else 0.0f
        return (base + sharpen).coerceIn(0.0f, 1.0f)
    }

    fun computeEffectiveVignette(): Float {
        val base = if (selectedFilter != CinemaFilter.NONE) selectedFilter.baseVignette * filterIntensity else 0.0f
        return (base + vignette).coerceIn(0.0f, 1.0f)
    }

    // Blend tint multipliers
    fun computeTintR(): Float {
        if (selectedFilter == CinemaFilter.NONE) return 1.0f
        return 1.0f + (selectedFilter.tintR - 1.0f) * filterIntensity
    }
    fun computeTintG(): Float {
        if (selectedFilter == CinemaFilter.NONE) return 1.0f
        return 1.0f + (selectedFilter.tintG - 1.0f) * filterIntensity
    }
    fun computeTintB(): Float {
        if (selectedFilter == CinemaFilter.NONE) return 1.0f
        return 1.0f + (selectedFilter.tintB - 1.0f) * filterIntensity
    }
    fun computeShadowTintR(): Float {
        if (selectedFilter == CinemaFilter.NONE) return 1.0f
        return 1.0f + (selectedFilter.shadowTintR - 1.0f) * filterIntensity
    }
    fun computeShadowTintG(): Float {
        if (selectedFilter == CinemaFilter.NONE) return 1.0f
        return 1.0f + (selectedFilter.shadowTintG - 1.0f) * filterIntensity
    }
    fun computeShadowTintB(): Float {
        if (selectedFilter == CinemaFilter.NONE) return 1.0f
        return 1.0f + (selectedFilter.shadowTintB - 1.0f) * filterIntensity
    }
}
