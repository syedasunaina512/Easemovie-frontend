package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.delay

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Standard Window behavior
        WindowCompat.setDecorFitsSystemWindows(window, true)

        setContent {
            MaterialTheme {
                SplashScreen {
                    val auth = FirebaseAuth.getInstance()
                    if (auth.currentUser != null) {
                        // User is logged in, go to Main
                        startActivity(Intent(this, MainActivity::class.java))
                    } else {
                        // Not logged in, go to Onboarding
                        startActivity(Intent(this, OnboardingActivity::class.java))
                    }
                    overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    finish()
                }
            }
        }
    }
}

@Composable
fun SplashScreen(onAnimationFinished: () -> Unit) {
    val scale = remember { Animatable(0.8f) }
    val alpha = remember { Animatable(0f) }

    // Pulsating animation for the logo to give it "life"
    val infiniteTransition = rememberInfiniteTransition(label = "logo_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ), label = "pulse"
    )

    LaunchedEffect(Unit) {
        scale.animateTo(1f, animationSpec = tween(1200, easing = EaseOutBack))
        alpha.animateTo(1f, animationSpec = tween(1000))
        delay(3500)
        onAnimationFinished()
    }

    // Modern futuristic background using the provided image
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.bg_splash),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        // Overlay with a very subtle dark tint to make logo pop if needed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White.copy(alpha = 0.2f))
        )

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .scale(scale.value)
                .alpha(alpha.value)
        ) {
            // Central Brain Logo with dynamic Glow
            Box(
                modifier = Modifier
                    .size(240.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // High-End Glow Effect
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    Color(0x804A90E2),
                                    Color(0x004A90E2)
                                )
                            ),
                            shape = androidx.compose.foundation.shape.CircleShape
                        )
                )

                Image(
                    painter = painterResource(id = R.drawable.splash_logo),
                    contentDescription = "App Logo",
                    modifier = Modifier.size(170.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // EASEMOVIE Text - Professional Premium Branding
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "EASE",
                    color = Color.White,
                    fontSize = 40.sp,
                    fontWeight = FontWeight.Light,
                    letterSpacing = 4.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
                Text(
                    text = "MOVIE",
                    fontSize = 45.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 4.sp,
                    style = androidx.compose.ui.text.TextStyle(
                        brush = Brush.linearGradient(
                            colors = listOf(
                                Color(0xFF4A90E2), // Bright Blue
                                Color(0xFF00D2FF), // Cyan
                                Color(0xFF9B51E0)  // Purple
                            )
                        ),
                        shadow = androidx.compose.ui.graphics.Shadow(
                            color = Color.Black.copy(alpha = 0.3f),
                            offset = androidx.compose.ui.geometry.Offset(2f, 4f),
                            blurRadius = 8f
                        )
                    )
                )
            }

            // AI POWERED Tagline
            Text(
                text = "AI POWERED",
                color = Color(0xFF2A7AD0),
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
        }
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SplashScreenPreview() {
    MaterialTheme {
        SplashScreen(onAnimationFinished = {})
    }
}
