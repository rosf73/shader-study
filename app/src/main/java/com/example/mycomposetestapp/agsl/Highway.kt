package com.example.mycomposetestapp.agsl

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

private const val HIGHWAY_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform float angle;
    uniform shader composable;

    float4 main(float2 fragCoord) {
        float2 uv = (fragCoord / iResolution).xy;
        float radian = radians(angle);
        
        uv.x += sin(iTime);

        return composable.eval(uv*iResolution);
    }
"""

@Composable
fun Highway(
    modifier: Modifier = Modifier,
    angle: Float = 45F,
    backgroundImage: @Composable (Modifier) -> Unit,
) {
    val shader = RuntimeShader(HIGHWAY_SHADER_SRC).apply {
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
                        size.height.toFloat()
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
