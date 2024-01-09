package com.example.mycomposetestapp.ui.agsl

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val ROTATE_SINE_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float angle;

    const float half_thickness = .02;

    float4 main(float2 fragCoord) {
        float2 uv = (fragCoord / iResolution).xy;
        float radian = radians(angle);

        float2 U = float2(cos(radian), sin(radian));
        float2 V = float2(-U.y, U.x);
        float2 O = float2(.5);

        float u = dot(uv - O, U);
        float v = .08 * sin(iTime*5. + u*40.);

        float2 dP = O + u*U + v*V - uv;
        if (length(dP) > half_thickness) return float4(.0);

        return float4(0.2, 0.3, 0.5, 1.0);
    }
"""

private const val ANGLE_WAVE_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float angle;
    uniform shader composable;

    half4 main(float2 fragCoord) {
        float2 uv = (fragCoord / iResolution).xy;
        float radian = radians(angle);

        float2 U = float2(cos(radian), sin(radian));

        float u = dot(uv, U);
        float v = .05 * sin(iTime*5. + u*30.);

        uv += v;
        return composable.eval(uv*iResolution);
    }
"""

@Composable
fun AngleSine(
    modifier: Modifier = Modifier,
    angle: Float = 45F,
) {
    val shader = RuntimeShader(ROTATE_SINE_SHADER_SRC).apply {
        setFloatUniform("angle", angle)
    }
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0F) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 2_500L) / 1_000F
                delay(10)
            }
        }
    }

    Surface(
        modifier = modifier
            .onSizeChanged { size ->
                shader.setFloatUniform(
                    "iResolution",
                    size.width.toFloat(),
                    size.height.toFloat(),
                )
            }
            .graphicsLayer {
                clip = true
                shader.setFloatUniform("iTime", timeMs.value)
                renderEffect = RenderEffect
                    .createShaderEffect(shader)
                    .asComposeRenderEffect()
            }
    ) {}
}

@Composable
fun AngleWave(
    modifier: Modifier = Modifier,
    angle: Float = 45F,
    backgroundImage: @Composable (Modifier) -> Unit,
) {
    val shader = RuntimeShader(ANGLE_WAVE_SHADER_SRC).apply {
        setFloatUniform("angle", angle)
    }
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0F) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 100_000L) / 1_000F
                delay(10)
            }
        }
    }

    Surface(modifier = modifier) {
        backgroundImage(
            Modifier
                .onSizeChanged { size ->
                    shader.setFloatUniform(
                        "iResolution",
                        size.width.toFloat(),
                        size.height.toFloat(),
                    )
                }
                .graphicsLayer {
                    clip = true
                    shader.setFloatUniform("iTime", timeMs.value)
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
        )
    }
}
