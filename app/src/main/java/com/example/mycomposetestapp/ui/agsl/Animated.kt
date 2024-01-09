package com.example.mycomposetestapp.ui.agsl

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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

private const val GRADATION_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float time;
    
    half4 main(float2 fragCoord) {
        float2 scaled = abs(1.0 - mod(fragCoord/iResolution.xy + time * 2.0, 2.0));
        return half4(scaled, 0.75, 0.5);
    }
"""

private const val GRADATION_IMAGE_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float time;
    uniform shader composable;

    half4 main(float2 fragCoord) {
        half4 image = composable.eval(fragCoord);
        float2 imgCoord = image.rg - fragCoord;
        float2 scaled = abs(1.0 - mod(imgCoord/iResolution.xy + time * 2.0, 2.0));
        return half4(scaled, image.b, 1.0);
    }
"""

private const val WAVE_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float time;
    uniform shader composable;

    half4 main(float2 fragCoord) {
        float scale = 1 / iResolution.x;
        float2 scaledCoord = fragCoord * scale;
        float2 center = iResolution * 0.5 * scale;
        float dist = distance(scaledCoord, center);
        float2 dir = scaledCoord - center;
        float sin = sin(dist * 20 - time * 6.28);
        float2 offset = dir * sin;
        float2 textCoord = scaledCoord + offset / 50;
        return composable.eval(textCoord / scale);
    }
"""

@Composable
fun AnimatedGradation(
    modifier: Modifier = Modifier,
) {
    val shader = RuntimeShader(GRADATION_SHADER_SRC)
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0F) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 1_000L) / 1_000F
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
                    size.height.toFloat()
                )
            }
            .graphicsLayer {
                shader.setFloatUniform("time", timeMs.value)
                renderEffect = RenderEffect
                    .createShaderEffect(shader)
                    .asComposeRenderEffect()
            },
        color = MaterialTheme.colorScheme.background,
    ) {}
}

@Composable
fun AnimatedGradationOnImage(
    modifier: Modifier = Modifier,
    backgroundImage: @Composable (Modifier) -> Unit,
) {
    val shader = RuntimeShader(GRADATION_IMAGE_SHADER_SRC)
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0F) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 1_000L) / 1_000F
                delay(10)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        backgroundImage(
            Modifier
                .onSizeChanged { size ->
                    shader.setFloatUniform(
                        "iResolution",
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                }
                .graphicsLayer {
                    clip = true
                    shader.setFloatUniform("time", timeMs.value)
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
        )
    }
}

@Composable
fun AnimatedWaveOnImage(
    modifier: Modifier = Modifier,
    backgroundImage: @Composable (Modifier) -> Unit,
) {
    val shader = RuntimeShader(WAVE_SHADER_SRC)
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0F) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 1_000L) / 1_000F
                delay(10)
            }
        }
    }

    Surface(
        modifier = modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background,
    ) {
        backgroundImage(
            Modifier
                .onSizeChanged { size ->
                    shader.setFloatUniform(
                        "iResolution",
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                }
                .graphicsLayer {
                    clip = true
                    shader.setFloatUniform("time", timeMs.value)
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
        )
    }
}
