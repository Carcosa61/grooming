package com.petgrooming.manager.ui.feature.splash

import android.media.MediaPlayer
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.petgrooming.manager.R
import kotlinx.coroutines.delay

// Sunrise gradient colors
private val SunriseTop = Color(0xFFFF7E5F)      // Coral/orange
private val SunriseMid = Color(0xFFFEB47B)      // Warm orange
private val SunriseBottom = Color(0xFFFFF1C1)  // Soft yellow

@Composable
fun SplashScreen(
    onSplashComplete: () -> Unit
) {
    val context = LocalContext.current

    // Animation state
    var startAnimation by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (startAnimation) 1f else 0.6f,
        animationSpec = tween(durationMillis = 800),
        label = "splashScale"
    )

    // Play sound on composition
    DisposableEffect(Unit) {
        var mediaPlayer: MediaPlayer? = null
        try {
            // Try to load ranat_ek sound from raw resources
            val resId = context.resources.getIdentifier("ranat_ek", "raw", context.packageName)
            if (resId != 0) {
                mediaPlayer = MediaPlayer.create(context, resId)
                mediaPlayer?.start()
            }
        } catch (e: Exception) {
            // Sound file not found or playback failed - continue silently
        }
        onDispose {
            mediaPlayer?.release()
        }
    }

    // Start animation and navigate after delay
    LaunchedEffect(Unit) {
        startAnimation = true
        delay(1800L)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(SunriseTop, SunriseMid, SunriseBottom)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.ic_launcher_foreground),
            contentDescription = null,
            modifier = Modifier
                .size(180.dp)
                .scale(scale)
        )
    }
}
