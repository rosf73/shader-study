package com.example.mycomposetestapp.ui.agsl

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asComposeRenderEffect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged

private const val CIRCLE_SHADER_SRC = """
    uniform float2 iResolution;

    float4 main(float2 fragCoord) {
        float2 uv = (fragCoord / iResolution).xy;
        float dist = distance(float2(.5), uv);
        dist = step(dist, .5);
        return float4(float3(dist), 1.0);
    }
"""

private const val HALFTONE_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float pw;
    uniform shader composable;

    float4 main(float2 fragCoord) {
        float2 uv = (fragCoord / iResolution).xy;
        float2 p = float2(pw, pw*(iResolution.y/iResolution.x));
        float4 mosaic = composable.eval((floor(uv * p) / p) * iResolution);

        float dist = distance(float2(.5), fract(uv * p));
        dist = step(dist, .5);
        return float4(float3(dist), 1.) * mosaic;
    }
"""

@Composable
fun Circle(
    modifier: Modifier = Modifier,
) {
    val shader = RuntimeShader(CIRCLE_SHADER_SRC)

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
                renderEffect = RenderEffect
                    .createShaderEffect(shader)
                    .asComposeRenderEffect()
            },
        color = MaterialTheme.colorScheme.background,
    ) {}
}

@Composable
fun Halftone(
    modifier: Modifier = Modifier,
    widthWeight: Float = 20F,
    backgroundImage: @Composable (Modifier) -> Unit,
) {
    val shader = RuntimeShader(HALFTONE_SHADER_SRC)
    shader.setFloatUniform("pw", widthWeight)

    Surface(
        modifier = modifier,
        color = Color.Black,
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
                    renderEffect = RenderEffect
                        .createRuntimeShaderEffect(shader, "composable")
                        .asComposeRenderEffect()
                }
        )
    }
}