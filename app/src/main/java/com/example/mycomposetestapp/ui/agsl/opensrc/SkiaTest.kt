package com.example.mycomposetestapp.ui.agsl.opensrc

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
import androidx.compose.ui.tooling.preview.Preview
import com.example.mycomposetestapp.ui.theme.MyComposeTestAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SHADER_SRC = """
    uniform float2 size;
    uniform float time;

    float f(float3 p) {
        p.z -= time * 10.;
        float a = p.z * .1;
        p.xy *= mat2(cos(a), sin(a), -sin(a), cos(a));
        return .1 - length(cos(p.xy) + sin(p.yz));
    }
    
    half4 main(float2 fragCoord) { 
        float3 d = .5 - fragCoord.xy1 / size.y;
        float3 p = float3(0);
        for (int i = 0; i < 32; i++) {
          p += f(p) * d;
        }
        return ((sin(p) + float3(2, 5, 12)) / length(p)).xyz1;
    }
"""

@Preview
@Composable
fun SkiaTest() {
    val shader = RuntimeShader(SHADER_SRC)
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0f) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 100_000L) / 1_000f
                delay(10)
            }
        }
    }

    MyComposeTestAppTheme {
        Surface(
            modifier = Modifier.fillMaxSize()
                .onSizeChanged { size ->
                    shader.setFloatUniform(
                        "size",
                        size.width.toFloat(),
                        size.height.toFloat()
                    )
                }
                .graphicsLayer {
                    clip = true
                    shader.setFloatUniform("time", timeMs.value)
                    renderEffect = RenderEffect
                        .createShaderEffect(shader)
                        .asComposeRenderEffect()
                },
            color = MaterialTheme.colorScheme.background,
        ) {}
    }
}