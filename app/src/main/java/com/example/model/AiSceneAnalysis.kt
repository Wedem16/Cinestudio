package com.example.model

data class AiSceneAnalysis(
    val sceneType: String,
    val lightingCondition: String,
    val environment: String,
    val dominantColors: List<String>,
    val exposureAssessment: String,
    val recommendedFilterId: String,
    val recommendedIntensity: Float,
    val recommendedExposure: Float,
    val recommendedContrast: Float,
    val recommendedSaturation: Float,
    val recommendedTemperature: Float,
    val recommendedTint: Float,
    val recommendedHighlights: Float,
    val recommendedShadows: Float,
    val recommendedSharpen: Float,
    val recommendedVignette: Float,
    val coloristRationale: String
)
