package com.example.media

import android.content.Context
import android.opengl.GLES20
import androidx.media3.common.Effect
import androidx.media3.common.VideoFrameProcessingException
import androidx.media3.common.util.GlProgram
import androidx.media3.common.util.GlUtil
import androidx.media3.common.util.Size
import androidx.media3.common.util.UnstableApi
import androidx.media3.effect.BaseGlShaderProgram
import androidx.media3.effect.Contrast
import androidx.media3.effect.GlEffect
import androidx.media3.effect.GlShaderProgram
import androidx.media3.effect.HslAdjustment
import androidx.media3.effect.RgbMatrix
import com.example.model.ColorGradeParams
import kotlin.math.pow

@UnstableApi
object Media3EffectFactory {

    /**
     * Builds a list of hardware-accelerated Media3 Effect objects representing
     * the combined color grade parameters (Cinema Filter LUT + 9 manual sliders).
     * These effects are identical in both ExoPlayer preview and Transformer export.
     */
    fun createEffects(params: ColorGradeParams, bypass: Boolean = false): List<Effect> {
        if (bypass || params.isNeutral()) {
            return emptyList()
        }

        val effectList = mutableListOf<Effect>()

        // 1. Contrast adjustment (-1.0 to 1.0)
        val effContrast = params.computeEffectiveContrast()
        if (effContrast != 0.0f) {
            effectList.add(Contrast(effContrast.coerceIn(-1.0f, 1.0f)))
        }

        // 2. Exposure, Temperature, Tint & Preset Split-Toning via RgbMatrix
        val effExposure = params.computeEffectiveExposure()
        val effTemp = params.computeEffectiveTemperature()
        val effTint = params.computeEffectiveTint()

        // Exposure multiplier (2^EV)
        val exposureScale = 2.0f.pow(effExposure)

        // Temperature: warm adds red, subtracts blue; cool adds blue, subtracts red
        val tempR = 1.0f + (effTemp * 0.28f)
        val tempB = 1.0f - (effTemp * 0.28f)

        // Tint: magenta adds red and blue, subtracts green; green adds green
        val tintG = 1.0f - (effTint * 0.22f)
        val tintR = 1.0f + (effTint * 0.12f)
        val tintB = 1.0f + (effTint * 0.12f)

        // Preset color tint multipliers
        val presetR = params.computeTintR()
        val presetG = params.computeTintG()
        val presetB = params.computeTintB()

        // Combined final channel scalers
        val finalScaleR = (exposureScale * tempR * tintR * presetR).coerceIn(0.0f, 4.0f)
        val finalScaleG = (exposureScale * tintG * presetG).coerceIn(0.0f, 4.0f)
        val finalScaleB = (exposureScale * tempB * tintB * presetB).coerceIn(0.0f, 4.0f)

        val matrixEffect = RgbMatrix { _, _ ->
            floatArrayOf(
                finalScaleR, 0f,          0f,          0f,
                0f,          finalScaleG, 0f,          0f,
                0f,          0f,          finalScaleB, 0f,
                0f,          0f,          0f,          1f
            )
        }
        effectList.add(matrixEffect)

        // 3. Saturation via HslAdjustment (Media3 expects -100f..100f)
        val effSat = params.computeEffectiveSaturation()
        if (effSat != 0.0f) {
            val saturationAdjustment = (effSat * 100.0f).coerceIn(-100.0f, 100.0f)
            effectList.add(
                HslAdjustment.Builder()
                    .adjustSaturation(saturationAdjustment)
                    .build()
            )
        }

        // 4. Custom GLSL Spatial Shader for Highlights, Shadows, Sharpen (5-tap convolution),
        // Vignette (radial optical falloff), and Shadow Split-Tone
        val effHighlights = params.computeEffectiveHighlights()
        val effShadows = params.computeEffectiveShadows()
        val effSharpen = params.computeEffectiveSharpen()
        val effVignette = params.computeEffectiveVignette()
        val shadowTintR = params.computeShadowTintR()
        val shadowTintG = params.computeShadowTintG()
        val shadowTintB = params.computeShadowTintB()

        if (effHighlights != 0.0f ||
            effShadows != 0.0f ||
            effSharpen > 0.0f ||
            effVignette > 0.0f ||
            shadowTintR != 1.0f ||
            shadowTintG != 1.0f ||
            shadowTintB != 1.0f
        ) {
            effectList.add(
                CineSpatialGradeEffect(
                    highlights = effHighlights,
                    shadows = effShadows,
                    sharpen = effSharpen,
                    vignette = effVignette,
                    shadowTintR = shadowTintR,
                    shadowTintG = shadowTintG,
                    shadowTintB = shadowTintB
                )
            )
        }

        return effectList
    }
}

@UnstableApi
private class CineSpatialGradeEffect(
    private val highlights: Float,
    private val shadows: Float,
    private val sharpen: Float,
    private val vignette: Float,
    private val shadowTintR: Float,
    private val shadowTintG: Float,
    private val shadowTintB: Float
) : GlEffect {
    override fun toGlShaderProgram(context: Context, useHdr: Boolean): GlShaderProgram {
        return CineSpatialShaderProgram(
            useHdr = useHdr,
            highlights = highlights,
            shadows = shadows,
            sharpen = sharpen,
            vignette = vignette,
            shadowTintR = shadowTintR,
            shadowTintG = shadowTintG,
            shadowTintB = shadowTintB
        )
    }
}

@UnstableApi
private class CineSpatialShaderProgram(
    useHdr: Boolean,
    private val highlights: Float,
    private val shadows: Float,
    private val sharpen: Float,
    private val vignette: Float,
    private val shadowTintR: Float,
    private val shadowTintG: Float,
    private val shadowTintB: Float
) : BaseGlShaderProgram(useHdr, /* texturePoolCapacity = */ 1) {

    private val glProgram: GlProgram
    private var texelWidth = 1.0f / 1920.0f
    private var texelHeight = 1.0f / 1080.0f

    init {
        try {
            glProgram = GlProgram(VERTEX_SHADER, FRAGMENT_SHADER)
            glProgram.setBufferAttribute(
                "aFramePosition",
                GlUtil.getNormalizedCoordinateBounds(),
                GlUtil.HOMOGENEOUS_COORDINATE_VECTOR_SIZE
            )
        } catch (e: GlUtil.GlException) {
            throw VideoFrameProcessingException(e)
        }
    }

    override fun configure(inputWidth: Int, inputHeight: Int): Size {
        texelWidth = 1.0f / inputWidth.coerceAtLeast(1).toFloat()
        texelHeight = 1.0f / inputHeight.coerceAtLeast(1).toFloat()
        return Size(inputWidth, inputHeight)
    }

    override fun drawFrame(inputTexId: Int, presentationTimeUs: Long) {
        try {
            glProgram.use()
            glProgram.setSamplerTexIdUniform("uTexSampler", inputTexId, /* texUnitIndex = */ 0)
            glProgram.setFloatsUniform("uTexelSize", floatArrayOf(texelWidth, texelHeight))
            glProgram.setFloatUniform("uHighlights", highlights)
            glProgram.setFloatUniform("uShadows", shadows)
            glProgram.setFloatUniform("uSharpen", sharpen)
            glProgram.setFloatUniform("uVignette", vignette)
            glProgram.setFloatsUniform(
                "uShadowTint",
                floatArrayOf(shadowTintR, shadowTintG, shadowTintB)
            )
            glProgram.bindAttributesAndUniforms()

            GLES20.glDrawArrays(GLES20.GL_TRIANGLE_STRIP, 0, 4)
            GlUtil.checkGlError()
        } catch (e: GlUtil.GlException) {
            throw VideoFrameProcessingException(e)
        }
    }

    override fun release() {
        super.release()
        try {
            glProgram.delete()
        } catch (e: GlUtil.GlException) {
            throw VideoFrameProcessingException(e)
        }
    }

    companion object {
        private val VERTEX_SHADER = """
            attribute vec4 aFramePosition;
            varying vec2 vTexSamplingCoord;
            void main() {
                gl_Position = aFramePosition;
                vTexSamplingCoord = vec2(aFramePosition.x * 0.5 + 0.5, aFramePosition.y * 0.5 + 0.5);
            }
        """.trimIndent()

        private val FRAGMENT_SHADER = """
            precision mediump float;
            varying vec2 vTexSamplingCoord;
            uniform sampler2D uTexSampler;
            uniform vec2 uTexelSize;
            uniform float uHighlights;
            uniform float uShadows;
            uniform float uSharpen;
            uniform float uVignette;
            uniform vec3 uShadowTint;

            void main() {
                vec4 center = texture2D(uTexSampler, vTexSamplingCoord);
                vec3 rgb = center.rgb;

                // 1. 5-tap Spatial Convolution Sharpen
                if (uSharpen > 0.001) {
                    vec3 north = texture2D(uTexSampler, vTexSamplingCoord + vec2(0.0, uTexelSize.y)).rgb;
                    vec3 south = texture2D(uTexSampler, vTexSamplingCoord - vec2(0.0, uTexelSize.y)).rgb;
                    vec3 east  = texture2D(uTexSampler, vTexSamplingCoord + vec2(uTexelSize.x, 0.0)).rgb;
                    vec3 west  = texture2D(uTexSampler, vTexSamplingCoord - vec2(uTexelSize.x, 0.0)).rgb;
                    vec3 detail = rgb * 4.0 - (north + south + east + west);
                    rgb = clamp(rgb + detail * (uSharpen * 1.35), 0.0, 1.0);
                }

                // 2. Luminance-masked Shadows & Highlights + Shadow Split-Tone
                float luma = dot(rgb, vec3(0.2126, 0.7152, 0.0722));
                float shadowMask = pow(clamp(1.0 - luma, 0.0, 1.0), 1.6);
                float highlightMask = pow(clamp(luma, 0.0, 1.0), 1.6);

                // Apply shadow lift/crush and shadow split-toning
                rgb += vec3(uShadows * 0.28) * shadowMask;
                rgb = mix(rgb, rgb * uShadowTint, shadowMask * 0.45);

                // Apply highlight recovery/boost
                rgb += vec3(uHighlights * 0.28) * highlightMask;

                // 3. Radial Optical Lens Vignette
                if (uVignette > 0.001) {
                    vec2 centered = vTexSamplingCoord - vec2(0.5, 0.5);
                    float dist = length(centered) * 1.4142;
                    float vigFactor = smoothstep(0.35, 1.05, dist);
                    rgb *= clamp(1.0 - vigFactor * (uVignette * 0.85), 0.0, 1.0);
                }

                gl_FragColor = vec4(clamp(rgb, 0.0, 1.0), center.a);
            }
        """.trimIndent()
    }
}
