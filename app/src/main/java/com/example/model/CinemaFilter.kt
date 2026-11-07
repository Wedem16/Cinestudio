package com.example.model

enum class CinemaFilter(
    val id: String,
    val title: String,
    val subtitle: String,
    val tags: List<String>,
    val defaultIntensity: Float,
    // Matrix multipliers & offsets (applied relative to intensity)
    val baseExposure: Float,
    val baseContrast: Float,
    val baseSaturation: Float,
    val baseTemperature: Float,
    val baseTint: Float,
    val baseHighlights: Float,
    val baseShadows: Float,
    val baseSharpen: Float,
    val baseVignette: Float,
    // Color tint RGB weights for lookup/grade in shader
    val tintR: Float,
    val tintG: Float,
    val tintB: Float,
    val shadowTintR: Float,
    val shadowTintG: Float,
    val shadowTintB: Float
) {
    NONE(
        id = "none",
        title = "Rec.709 Natural",
        subtitle = "Neutral Cinema Standard",
        tags = listOf("Standard", "Flat", "Clean"),
        defaultIntensity = 1.0f,
        baseExposure = 0.0f,
        baseContrast = 0.0f,
        baseSaturation = 0.0f,
        baseTemperature = 0.0f,
        baseTint = 0.0f,
        baseHighlights = 0.0f,
        baseShadows = 0.0f,
        baseSharpen = 0.0f,
        baseVignette = 0.0f,
        tintR = 1.0f, tintG = 1.0f, tintB = 1.0f,
        shadowTintR = 1.0f, shadowTintG = 1.0f, shadowTintB = 1.0f
    ),
    BEACH_CINEMATIC(
        id = "beach_cinematic",
        title = "Beach Cinematic",
        subtitle = "Ocean blue & teal shadows",
        tags = listOf("Tropical", "Teal/Orange", "Vibrant"),
        defaultIntensity = 0.85f,
        baseExposure = 0.15f,
        baseContrast = 0.22f,
        baseSaturation = 0.28f,
        baseTemperature = 0.12f,
        baseTint = -0.05f,
        baseHighlights = 0.18f,
        baseShadows = -0.15f,
        baseSharpen = 0.25f,
        baseVignette = 0.20f,
        tintR = 1.08f, tintG = 1.02f, tintB = 0.92f, // Golden highlights
        shadowTintR = 0.85f, shadowTintG = 1.12f, shadowTintB = 1.25f // Deep teal shadows
    ),
    CITY_NIGHT(
        id = "city_night",
        title = "City Night",
        subtitle = "Neon contrast & moody skin tones",
        tags = listOf("Night", "Neon", "Cyberpunk"),
        defaultIntensity = 0.80f,
        baseExposure = -0.10f,
        baseContrast = 0.35f,
        baseSaturation = 0.15f,
        baseTemperature = -0.22f,
        baseTint = 0.18f,
        baseHighlights = -0.12f,
        baseShadows = -0.25f,
        baseSharpen = 0.35f,
        baseVignette = 0.40f,
        tintR = 1.15f, tintG = 0.90f, tintB = 1.20f, // Magenta-blue highlights
        shadowTintR = 0.80f, shadowTintG = 0.95f, shadowTintB = 1.30f // Deep cobalt shadows
    ),
    NATURE_CINEMATIC(
        id = "nature_cinematic",
        title = "Nature Cinematic",
        subtitle = "Rich foliage greens & mountain depth",
        tags = listOf("Forest", "Organic", "Documentary"),
        defaultIntensity = 0.85f,
        baseExposure = 0.05f,
        baseContrast = 0.18f,
        baseSaturation = 0.20f,
        baseTemperature = -0.08f,
        baseTint = -0.15f,
        baseHighlights = 0.10f,
        baseShadows = 0.12f,
        baseSharpen = 0.30f,
        baseVignette = 0.15f,
        tintR = 0.95f, tintG = 1.10f, tintB = 0.92f, // Lush emerald midtones
        shadowTintR = 0.88f, shadowTintG = 1.05f, shadowTintB = 0.95f
    ),
    SONY_FX3(
        id = "sony_fx3",
        title = "Sony FX3 Inspired",
        subtitle = "S-Cinetone cinema color & soft roll-off",
        tags = listOf("Venice/FX3", "Cinema", "Skin Tones"),
        defaultIntensity = 0.90f,
        baseExposure = 0.08f,
        baseContrast = 0.15f,
        baseSaturation = 0.08f,
        baseTemperature = 0.05f,
        baseTint = 0.02f,
        baseHighlights = -0.08f,
        baseShadows = 0.15f,
        baseSharpen = 0.20f,
        baseVignette = 0.18f,
        tintR = 1.05f, tintG = 1.02f, tintB = 0.96f, // Warm flattering skin tones
        shadowTintR = 0.95f, shadowTintG = 0.98f, shadowTintB = 1.04f
    ),
    GOLDEN_HOUR(
        id = "golden_hour",
        title = "Golden Hour",
        subtitle = "Warm low sun & honey cinematic glow",
        tags = listOf("Sunset", "Warm", "Romantic"),
        defaultIntensity = 0.85f,
        baseExposure = 0.18f,
        baseContrast = 0.20f,
        baseSaturation = 0.24f,
        baseTemperature = 0.38f,
        baseTint = 0.08f,
        baseHighlights = 0.25f,
        baseShadows = -0.10f,
        baseSharpen = 0.15f,
        baseVignette = 0.28f,
        tintR = 1.25f, tintG = 1.08f, tintB = 0.82f, // Amber sunset glow
        shadowTintR = 1.05f, shadowTintG = 0.92f, shadowTintB = 0.85f
    ),
    CLOUDY_CINEMATIC(
        id = "cloudy_cinematic",
        title = "Cloudy Cinematic",
        subtitle = "Soft moody silver skies & muted greens",
        tags = listOf("Overcast", "Moody", "Nordic"),
        defaultIntensity = 0.80f,
        baseExposure = -0.05f,
        baseContrast = 0.12f,
        baseSaturation = -0.12f,
        baseTemperature = -0.18f,
        baseTint = 0.05f,
        baseHighlights = -0.15f,
        baseShadows = 0.08f,
        baseSharpen = 0.28f,
        baseVignette = 0.25f,
        tintR = 0.92f, tintG = 0.98f, tintB = 1.08f, // Cool slate highlights
        shadowTintR = 0.88f, shadowTintG = 0.94f, shadowTintB = 1.02f
    ),
    CLEAN_TRAVEL(
        id = "clean_travel",
        title = "Clean Travel Film",
        subtitle = "Clear blue skies & crisp commercial clarity",
        tags = listOf("Travel", "Commercial", "Crisp"),
        defaultIntensity = 0.85f,
        baseExposure = 0.12f,
        baseContrast = 0.20f,
        baseSaturation = 0.22f,
        baseTemperature = 0.02f,
        baseTint = -0.02f,
        baseHighlights = 0.12f,
        baseShadows = 0.05f,
        baseSharpen = 0.40f,
        baseVignette = 0.12f,
        tintR = 1.02f, tintG = 1.04f, tintB = 1.06f,
        shadowTintR = 0.96f, shadowTintG = 0.98f, shadowTintB = 1.02f
    );

    companion object {
        fun fromId(id: String): CinemaFilter {
            return entries.find { it.id.equals(id, ignoreCase = true) } ?: NONE
        }
    }
}
