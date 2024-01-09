package com.example.mycomposetestapp.agsl.opensrc

import android.graphics.RenderEffect
import android.graphics.RuntimeShader
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
import com.example.mycomposetestapp.ui.theme.MyComposeTestAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;

    float3x3 getRotZMat(float a) {
        return float3x3(cos(a), -sin(a), 0., sin(a), cos(a), 0., 0., 0., 1.);
    }

    float dstepf = 0.0;

    float map(float3 p) {
        p.x += sin(p.z*1.8);
        p.y += cos(p.z*.2) * sin(p.x*.8);
        p *= getRotZMat(p.z*0.8+sin(p.x)+cos(p.y));
        p.xy = mod(p.xy, 0.3) - 0.15;
        dstepf += 0.003;
        return length(p.xy);
    }
    
    float4 main(float2 fragCoord) {
        float2 uv = (fragCoord - iResolution.xy*.5) / iResolution.y;
        float3 rd = normalize(float3(uv, (1.-dot(uv, uv)*.5)*.5)); 
        float3 ro = float3(0, 0, iTime*1.26), col = float3(0), sp;
        float cs = cos(iTime*0.5), si = sin(iTime*0.5);    
        rd.xz = float2x2(cs, si,-si, cs)*rd.xz;
        float t = 0.06, layers=0., d=0., aD;
        float thD = 0.02;
        for (float i=0.; i < 250.; i++) {
            if (layers>15. || col.x>1. || t>5.6) break;
            sp = ro + rd*t;
            d = map(sp); 
            aD = (thD-abs(d)*15./16.)/thD;
            if (aD>0.) { 
                col += aD*aD*(3.-2.*aD)/(1. + t*t*0.25)*.2; 
                layers++; 
            }
            t += max(d*.7, thD*1.5) * dstepf; 
        }
        col = max(col, 0.);
        col = mix(col, float3(min(col.x*1.5, 1.), pow(col.x, 2.5), pow(col.x, 12.)), 
                  dot(sin(rd.yzx*8. + sin(rd.zxy*8.)), float3(.1666))+0.4);
        col = mix(col, float3(col.x*col.x*.85, col.x, col.x*col.x*0.3), 
                 dot(sin(rd.yzx*4. + sin(rd.zxy*4.)), float3(.1666))+0.25);
        return float4(clamp(col, 0., 1.), 1.);
    }
"""

@Composable
fun ShadertoyTest(
    modifier: Modifier = Modifier,
) {
    val shader = RuntimeShader(SHADER_SRC)
    val scope = rememberCoroutineScope()
    val timeMs = remember { mutableStateOf(0F) }
    LaunchedEffect(Unit) {
        scope.launch {
            while (true) {
                timeMs.value = (System.currentTimeMillis() % 100_000L) / 1_000F
                delay(5)
            }
        }
    }

    MyComposeTestAppTheme {
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
                    clip = true
                    shader.setFloatUniform("iTime", timeMs.value)
                    renderEffect = RenderEffect
                        .createShaderEffect(shader)
                        .asComposeRenderEffect()
                },
            color = MaterialTheme.colorScheme.background,
        ) {}
    }
}