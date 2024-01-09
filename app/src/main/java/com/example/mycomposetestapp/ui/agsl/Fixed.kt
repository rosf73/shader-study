package com.example.mycomposetestapp.ui.agsl

import android.graphics.RuntimeShader
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ShaderBrush

private const val COLOR_SHADER_SRC = """
    layout(color) uniform half4 iColor;
    half4 main(float2 fragCoord) {
        return iColor;
    }
"""

private const val GRADIENT_COLOR_SHADER_SRC = """
    uniform float2 iResolution;
    half4 main(float2 fragCoord) {
        float2 scaled = fragCoord/iResolution.xy;
        return half4(scaled, 0, 1);
    }
"""

@Composable
fun ColorCircle(
    modifier: Modifier = Modifier,
    color: Int,
) {
    val colorShader = RuntimeShader(COLOR_SHADER_SRC)
    val shaderBrush = ShaderBrush(colorShader)

    Canvas(modifier = modifier) {
        colorShader.setColorUniform("iColor", color)
        drawCircle(brush = shaderBrush)
    }
}

@Composable
fun GradientColorCircle(
    modifier: Modifier = Modifier,
) {
    val gradientColorShader = RuntimeShader(GRADIENT_COLOR_SHADER_SRC)
    val shaderBrush = ShaderBrush(gradientColorShader)

    Canvas(modifier = modifier) {
        gradientColorShader.setFloatUniform("iResolution", size.width, size.height)
        drawCircle(brush = shaderBrush)
    }
}
