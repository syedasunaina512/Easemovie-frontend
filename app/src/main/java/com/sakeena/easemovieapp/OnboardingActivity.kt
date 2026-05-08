package com.sakeena.easemovieapp

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.view.WindowCompat

class OnboardingActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            MaterialTheme {
                OnboardingScreen {
                    startActivity(Intent(this, LoginActivity::class.java))
                    finish()
                }
            }
        }
    }
}

@Composable
fun OnboardingScreen(onGetStartedClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF9FAFF) // Soft Blue Background (Matches Splash theme)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
        ) {
            // Section 1: Top Image Card with Depth
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1.3f)
                    .padding(16.dp)
                    .shadow(20.dp, RoundedCornerShape(40.dp))
                    .clip(RoundedCornerShape(40.dp))
            ) {
                Image(
                    painter = painterResource(id = R.drawable.img_onboarding),
                    contentDescription = "Onboarding Art",
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
            }

            // Section 2: Text and Button Content
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // Decorative Floating Icons (In Theme Colors)
                OnboardingDecorations()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 28.dp, vertical = 20.dp),
                    horizontalAlignment = Alignment.Start,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Spacer(modifier = Modifier.height(10.dp))
                        
                        // Header with Premium Navy color from theme
                        Text(
                            text = "AI Story to Animation\nCreator",
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color(0xFF0D2137), // Deep Navy
                            lineHeight = 40.sp
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Text(
                            text = "Your imagination, animated. Let your AI storyteller weave tales and bring them to life instantly. Explore endless stories, animations, and magic!",
                            fontSize = 15.sp,
                            color = Color(0xFF666666),
                            lineHeight = 24.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Get Started Button with Solid Theme Color
                    Button(
                        onClick = onGetStartedClick,
                        modifier = Modifier
                            .padding(bottom = 30.dp)
                            .height(60.dp)
                            .wrapContentWidth(),
                        shape = RoundedCornerShape(30.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF9B51E0) // Matches Splash Bright Blue
                        ),
                        contentPadding = PaddingValues(horizontal = 35.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "Get Started!",
                                color = Color.White,
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Icon(
                                imageVector = Icons.Default.ArrowForward,
                                contentDescription = null,
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OnboardingDecorations() {
    val themeColor = Color(0xFF3A7BD5).copy(alpha = 0.3f) // Primary Blue Tint
    
    Box(modifier = Modifier.fillMaxSize()) {
        // Star Icon (Top Right)
        Icon(
            painter = painterResource(id = R.drawable.ic_star_rate_24),
            contentDescription = null,
            tint = themeColor,
            modifier = Modifier
                .size(28.dp)
                .align(Alignment.TopEnd)
                .offset(x = (-30).dp, y = 20.dp)
        )

        // Movie Icon (Left Middle)
        Icon(
            painter = painterResource(id = R.drawable.ic_camera_alt_24),
            contentDescription = null,
            tint = themeColor,
            modifier = Modifier
                .size(40.dp)
                .align(Alignment.CenterStart)
                .offset(x = 10.dp, y = (-30).dp)
                .rotate(-15f)
        )

        // Book/Script Icon (Right Middle)
        Icon(
            painter = painterResource(id = R.drawable.ic_description_24),
            contentDescription = null,
            tint = Color(0xFF9B51E0).copy(alpha = 0.3f), // Purple Tint
            modifier = Modifier
                .size(45.dp)
                .align(Alignment.CenterEnd)
                .offset(x = (-20).dp, y = 50.dp)
                .rotate(12f)
        )
        
        // Small Floating Dot (Left Top)
        Box(
            modifier = Modifier
                .size(16.dp)
                .align(Alignment.TopStart)
                .offset(x = 40.dp, y = 10.dp)
                .background(themeColor, CircleShape)
        )
    }
}

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun OnboardingScreenPreview() {
    MaterialTheme {
        OnboardingScreen(onGetStartedClick = {})
    }
}
