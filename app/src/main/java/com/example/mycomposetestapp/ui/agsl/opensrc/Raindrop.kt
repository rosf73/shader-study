package com.example.mycomposetestapp.ui.agsl.opensrc

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

private const val RAINDROP_SHADER_SRC = """
    uniform float2 iResolution;
    uniform float iTime;
    uniform shader composable;
    
    const float size = 0.2;

    const float pi = 6.28318530718; // pi*2

    // GAUSSIAN BLUR SETTINGS
    const float blurDirections = 32.0; // Default 16.0 - More is better but slower
    const float blurQuality = 8.0; // Default 4.0 - More is better but slower
    const float blurSize = 32.0;

    float3 N13(float p) {
        //  from DAVE HOSKINS
        float3 p3 = fract(float3(p) * float3(.1031,.11369,.13787));
        p3 += dot(p3, p3.yzx + 19.19);
        return fract(float3((p3.x + p3.y)*p3.z, (p3.x+p3.z)*p3.y, (p3.y+p3.z)*p3.x));
    }
    float4 N14(float t) {
        return fract(sin(t*float4(123., 1024., 1456., 264.)) * float4(6547., 345., 8799., 1564.));
    }
    float N(float t) {
        return fract(sin(t*12345.564)*7658.76);
    }
    
    float saw(float b, float t) {
        return smoothstep(0., b, t) * smoothstep(1., b, t);
    }
    
    float2 drops(float2 uv, float t) {
        float2 UV = uv;
        
        // DEFINE GRID
        uv.y += t * 0.8;
        float2 a = float2(6., 1.);
        float2 grid = a*2.;
        float2 id = floor(uv*grid);
        
        // RANDOM SHIFT Y
        float colShift = N(id.x); 
        uv.y += colShift;
        
        // DEFINE SPACES
        id = floor(uv*grid);
        float3 n = N13(id.x*35.2+id.y*2376.1);
        float2 st = fract(uv*grid)-float2(.5, 0);
        
        // POSITION DROPS
        //clamp(2*x,0,2)+clamp(1-x*.5, -1.5, .5)+1.5-2
        float x = n.x - .5;
        float y = UV.y * 20.;
        
        float distort = sin(y + sin(y));
        x += distort * (.5-abs(x)) * (n.z-.5);
        x *= .7;
        float ti = fract(t + n.z);
        y = (saw(.85, ti)-.5) * .9+.5;
        float2 p = float2(x, y);
        
        // DROPS
        float d = length((st-p)*a.yx);
        
        float dSize = size; 
        
        float Drop = smoothstep(dSize, .0, d);
        
        float r = sqrt(smoothstep(1., y, st.y));
        float cd = abs(st.x-x);
        
        // TRAILS
        float trail = smoothstep((dSize*.5+.03)*r, (dSize*.5-.05)*r, cd);
        float trailFront = smoothstep(-.02, .02, st.y-y);
        trail *= trailFront;
        
        // DROPLETS
        y = UV.y;
        y += N(id.x);
        float trail2 = smoothstep(dSize*r, .0, cd);
        float droplets = max(0., (sin(y*(1.-y)*120.)-st.y))*trail2*trailFront*n.z;
        y = fract(y*10.)+(st.y-.5);
        float dd = length(st-float2(x, y));
        droplets = smoothstep(dSize*N(id.x), 0., dd);
        float m = Drop+droplets*r*trailFront;
        
        return float2(m, trail);
    }
    
    float staticDrops(float2 uv, float t) {
        uv *= 30.;
        
        float2 id = floor(uv);
        uv = fract(uv)-.5;
        float3 n = N13(id.x*107.45+id.y*3543.654);
        float2 p = (n.xy-.5)*0.5;
        float d = length(uv-p);
        
        float fade = saw(.025, fract(t+n.z));
        float c = smoothstep(size, 0., d) * fract(n.z*10.)*fade;
    
        return c;
    }
    
    float2 rain(float2 uv, float t) {
        float s = staticDrops(uv, t); 
        float2 r1 = drops(uv, t);
        float2 r2 = drops(uv*1.8, t);
        
        float c = s+r1.x+r2.x;
        
        c = smoothstep(.3, 1., c);
        
        return float2(c, max(r1.y, r2.y));
    }
    
    float4 main(float2 fragCoord) {
        float2 uv = (.5*iResolution - fragCoord).xy / iResolution.y;
        float2 UV = (fragCoord/iResolution).xy;
        float t = iTime * .2;
        
        float rainAmount = 0.8;
        
        UV = (UV-.5)*(.9)+.5;
        
        float2 c = rain(uv, t);
    
        float2 e = float2(.001, 0.); // pixel offset
        float cx = rain(uv+e, t).x;
        float cy = rain(uv+e.yx, t).x;
        float2 n = float2(cx-c.x, cy-c.x); // normals

        // BLUR derived from existical https://www.shadertoy.com/view/Xltfzj
        float2 Radius = blurSize/iResolution.xy;

        float3 col = composable.eval(UV * iResolution).rgb;
        // Blur calculations
        for (float d=0.; d<pi; d+=pi/blurDirections) {
            for (float i=1./blurQuality; i<=1.; i+=1./blurQuality) {
                float3 tex = composable.eval((UV + n + float2(cos(d),sin(d))*Radius*i) * iResolution).rgb;
                col += tex;            
            }
        }

        col /= blurQuality * blurDirections;

        float3 tex = composable.eval((UV+n) * iResolution).rgb;
        c.y = clamp(c.y, 0.0, 1.);

        col -= c.y;
        col += c.y*(tex+.6);
    
        return float4(col, 1.);
    }
"""

@Composable
fun Raindrop(
    modifier: Modifier = Modifier,
    backgroundImage: @Composable (Modifier) -> Unit,
) {
    val shader = RuntimeShader(RAINDROP_SHADER_SRC)
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