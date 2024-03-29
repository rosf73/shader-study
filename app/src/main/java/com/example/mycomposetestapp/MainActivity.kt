package com.example.mycomposetestapp

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.mycomposetestapp.ui.agsl.ColorCircle
import com.example.mycomposetestapp.ui.agsl.GradientColorCircle
import com.example.mycomposetestapp.ui.agsl.Halftone
import com.example.mycomposetestapp.ui.agsl.opensrc.Raindrop
import com.example.mycomposetestapp.ui.theme.MyComposeTestAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val photoParrot = painterResource(id = R.drawable.parrots)

            MyComposeTestAppTheme {
//                AnimatedGradation(
////                ShadertoyTest(
//                    modifier = Modifier.fillMaxSize(),
//                )
                Raindrop(
//                AnimatedGradationOnImage(
//                AnimatedWaveOnImage(
                    modifier = Modifier.fillMaxSize(),
                    backgroundImage = {
                        Image(
                            painter = photoParrot,
                            modifier = it,
                            contentScale = ContentScale.FillHeight,
                            contentDescription = null,
                        )
                    }
                )
//                AngleSine(
//                    modifier = Modifier.fillMaxWidth().aspectRatio(1F),
//                    angle = 45F,
//                )
//                AngleWave(
//                    modifier = Modifier.fillMaxSize(),
//                    angle = 140F,
//                    backgroundImage = {
//                        Image(
//                            painter = photoParrot,
//                            modifier = it,
//                            contentScale = ContentScale.FillHeight,
//                            contentDescription = null
//                        )
//                    }
//                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CirClePreview() {
    MyComposeTestAppTheme {
        GradientColorCircle(modifier = Modifier.fillMaxSize())
        ColorCircle(modifier = Modifier.size(100.dp), color = Color.GREEN)
    }
}

@Preview(showBackground = true)
@Composable
fun HalftonePreview() {
    val photo = painterResource(id = R.drawable.parrots)

    MyComposeTestAppTheme {
        Halftone(
            modifier = Modifier.fillMaxSize(),
            widthWeight = 40F,
            backgroundImage = {
                Image(
                    painter = photo,
                    modifier = it,
                    contentScale = ContentScale.FillHeight,
                    contentDescription = null,
                )
            },
        )
    }
}
