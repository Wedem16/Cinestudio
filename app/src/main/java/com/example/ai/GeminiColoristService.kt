package com.example.ai

import android.graphics.Bitmap
import android.util.Log
import com.example.model.AiSceneAnalysis
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.json.JSONObject

private const val TAG = "GeminiColoristService"

class GeminiColoristService {

    // Define strict JSON Schema using Schema.obj per firebase-ai-logic-android guidelines
    private val analysisSchema = Schema.obj(
        properties = mapOf(
            "sceneType" to Schema.string(description = "e.g. Coastal Beach, Urban Night, Forest Nature, Portrait, Sunset, Moody Overcast, Travel"),
            "lightingCondition" to Schema.string(description = "e.g. Bright Direct Sunlight, High Contrast Neon, Diffused Overcast, Golden Backlit, Low Light"),
            "environment" to Schema.string(description = "Detailed physical setting and environment description"),
            "dominantColors" to Schema.array(
                items = Schema.string(),
                description = "3 to 5 dominant color names/hues identified in the scene"
            ),
            "exposureAssessment" to Schema.string(description = "Assessment of clipping, highlights, shadow detail, and dynamic range"),
            "recommendedFilterId" to Schema.string(description = "Must be one of: beach_cinematic, city_night, nature_cinematic, sony_fx3, golden_hour, cloudy_cinematic, clean_travel"),
            "recommendedIntensity" to Schema.double(description = "Recommended filter intensity between 0.5 and 1.0"),
            "recommendedExposure" to Schema.double(description = "Exposure adjustment from -0.5 to +0.5"),
            "recommendedContrast" to Schema.double(description = "Contrast adjustment from -0.4 to +0.4"),
            "recommendedSaturation" to Schema.double(description = "Saturation adjustment from -0.3 to +0.4"),
            "recommendedTemperature" to Schema.double(description = "Color temperature from -0.5 (cool/blue) to +0.5 (warm/amber)"),
            "recommendedTint" to Schema.double(description = "Tint from -0.3 (green) to +0.3 (magenta)"),
            "recommendedHighlights" to Schema.double(description = "Highlight recovery or boost from -0.4 to +0.4"),
            "recommendedShadows" to Schema.double(description = "Shadow lift or crush from -0.4 to +0.4"),
            "recommendedSharpen" to Schema.double(description = "Sharpening amount from 0.0 to 0.5"),
            "recommendedVignette" to Schema.double(description = "Vignette amount from 0.0 to 0.4"),
            "coloristRationale" to Schema.string(description = "A professional cinema colorist explanation of why this look and these specific adjustments enhance the footage")
        ),
        optionalProperties = emptyList()
    )

    private val model by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = analysisSchema
                temperature = 0.2f
            }
        )
    }

    suspend fun analyzeVideoFrames(frames: List<Bitmap>): Result<AiSceneAnalysis> = withContext(Dispatchers.IO) {
        if (frames.isEmpty()) {
            return@withContext Result.failure(IllegalArgumentException("No frames provided for AI analysis."))
        }

        try {
            val promptText = """
                You are a senior Hollywood digital cinema colorist working on high-end ARRI and Sony FX3 footage.
                Analyze the attached representative keyframes from the user's video clip.
                Identify the scene, environment, lighting quality, exposure balance, and color palette.
                Select the most fitting cinema filter from this exact list:
                - beach_cinematic (for ocean, coastal, tropical, or bright teal/orange scenes)
                - city_night (for night streets, neon lights, dark moody contrast)
                - nature_cinematic (for forests, foliage, mountains, lush outdoor landscapes)
                - sony_fx3 (for portraits, human subjects, documentary, neutral cinema skin tones)
                - golden_hour (for sunsets, warm backlight, golden morning/afternoon sun)
                - cloudy_cinematic (for overcast, foggy, moody Nordic/silver palettes)
                - clean_travel (for bright daylight travel, clear blue skies, modern commercial clarity)
                
                Provide fine-tuned adjustments for Exposure, Contrast, Saturation, Temperature, Tint, Highlights, Shadows, Sharpen, and Vignette to achieve a breathtaking cinematic master.
            """.trimIndent()

            val response = withTimeout(60_000L) {
                model.generateContent(
                    content {
                        text(promptText)
                        for (bitmap in frames) {
                            image(bitmap)
                        }
                    }
                )
            }

            val jsonString = response.text
                ?: return@withContext Result.failure(IllegalStateException("Gemini returned empty analysis response."))

            Log.d(TAG, "Gemini JSON Response: $jsonString")

            val json = JSONObject(jsonString)
            val colorsArray = json.optJSONArray("dominantColors")
            val dominantColors = mutableListOf<String>()
            if (colorsArray != null) {
                for (i in 0 until colorsArray.length()) {
                    dominantColors.add(colorsArray.getString(i))
                }
            }

            val analysis = AiSceneAnalysis(
                sceneType = json.optString("sceneType", "Cinema Scene"),
                lightingCondition = json.optString("lightingCondition", "Natural Lighting"),
                environment = json.optString("environment", "Outdoor Environment"),
                dominantColors = if (dominantColors.isEmpty()) listOf("Teal", "Amber", "Slate") else dominantColors,
                exposureAssessment = json.optString("exposureAssessment", "Well balanced exposure"),
                recommendedFilterId = json.optString("recommendedFilterId", "sony_fx3"),
                recommendedIntensity = json.optDouble("recommendedIntensity", 0.85).toFloat().coerceIn(0.1f, 1.0f),
                recommendedExposure = json.optDouble("recommendedExposure", 0.08).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedContrast = json.optDouble("recommendedContrast", 0.18).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedSaturation = json.optDouble("recommendedSaturation", 0.12).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedTemperature = json.optDouble("recommendedTemperature", 0.05).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedTint = json.optDouble("recommendedTint", 0.0).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedHighlights = json.optDouble("recommendedHighlights", -0.05).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedShadows = json.optDouble("recommendedShadows", 0.08).toFloat().coerceIn(-1.0f, 1.0f),
                recommendedSharpen = json.optDouble("recommendedSharpen", 0.25).toFloat().coerceIn(0.0f, 1.0f),
                recommendedVignette = json.optDouble("recommendedVignette", 0.15).toFloat().coerceIn(0.0f, 1.0f),
                coloristRationale = json.optString("coloristRationale", "Balanced cinema color roll-off with natural skin tones and rich midtone contrast.")
            )

            Result.success(analysis)
        } catch (e: TimeoutCancellationException) {
            Log.e(TAG, "Gemini analysis timed out after 60s", e)
            Result.failure(Exception("AI Scene Analysis timed out after 60 seconds. Please check your connection and try again."))
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Gemini scene analysis failed", e)
            Result.failure(e)
        }
    }
}
