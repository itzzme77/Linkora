package com.example.linkora.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.linkora.R
import com.example.linkora.ui.theme.LocalGlassColors
import kotlin.math.cos
import kotlin.math.sin

/**
 * GlassBackground: The signature Linkora liquid-glass atmospheric backdrop.
 *
 * Renders the aesthetic blurred warm interior scene (bg_glass_aesthetic_blur)
 * with slow, organic ambient drift, layered golden/lavender lighting orbs,
 * and a soft optical vignette that makes translucent glass surfaces pop with
 * authentic iOS-like depth and readability.
 */
@Composable
fun GlassBackground(
    modifier: Modifier = Modifier,
    enableAmbientMotion: Boolean = true,
    content: @Composable BoxScope.() -> Unit
) {
    val isDark = LocalGlassColors.current.isDark

    val infiniteTransition = rememberInfiniteTransition(label = "ambientDrift")

    // Gentle breathing scale for realistic organic lifestyle background movement
    val backgroundScale by if (enableAmbientMotion) {
        infiniteTransition.animateFloat(
            initialValue = 1.0f,
            targetValue = 1.045f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 16000, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bgScale"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(1f) }
    }

    val phase by if (enableAmbientMotion) {
        infiniteTransition.animateFloat(
            initialValue = 0f,
            targetValue = (2 * Math.PI).toFloat(),
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 18000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "phase"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(0f) }
    }

    // Occasional subtle glass reflection sweep across the backdrop
    val shimmerProgress by if (enableAmbientMotion) {
        infiniteTransition.animateFloat(
            initialValue = -0.5f,
            targetValue = 1.8f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 12000, easing = LinearEasing),
                repeatMode = RepeatMode.Restart
            ),
            label = "shimmerProgress"
        )
    } else {
        androidx.compose.runtime.remember { androidx.compose.runtime.mutableFloatStateOf(-0.5f) }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(if (isDark) Color(0xFF090C16) else Color(0xFFF1F5F9))
    ) {
        // High-fidelity liquid glass wallpaper dynamically selected by device dark/light mode
        // Light mode restores the previous warm aesthetic frosted glass backdrop (bg_glass_aesthetic_blur)
        val bgDrawableRes = if (isDark) R.drawable.bg_linkora_dark else R.drawable.bg_glass_aesthetic_blur

        Image(
            painter = painterResource(id = bgDrawableRes),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .fillMaxSize()
                .scale(backgroundScale)
        )

        // Ambient liquid glass lighting and light orbs
        Canvas(modifier = Modifier.fillMaxSize()) {
            val width = size.width
            val height = size.height

            // Upper-right ambient accent glow (emerald in dark mode, luminous mint in light mode)
            val sunX = width * 0.78f + sin(phase) * (width * 0.05f)
            val sunY = height * 0.25f + cos(phase) * (height * 0.04f)
            val sunRadius = width * 0.85f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = if (isDark) {
                        listOf(
                            Color(0x3310B981), // Emerald glow
                            Color(0x12059669),
                            Color.Transparent
                        )
                    } else {
                        listOf(
                            Color(0x4034D399), // Soft luminous mint glow
                            Color(0x1810B981),
                            Color.Transparent
                        )
                    },
                    center = Offset(sunX, sunY),
                    radius = sunRadius
                ),
                radius = sunRadius,
                center = Offset(sunX, sunY)
            )

            // Lavender / Periwinkle ambient soft aura on left
            val orbLavenderX = width * 0.20f - cos(phase * 0.8f) * (width * 0.08f)
            val orbLavenderY = height * 0.65f + sin(phase * 0.8f) * (height * 0.06f)
            val orbLavenderRadius = width * 0.75f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x306366F1) else Color(0x35818CF8),
                        Color.Transparent
                    ),
                    center = Offset(orbLavenderX, orbLavenderY),
                    radius = orbLavenderRadius
                ),
                radius = orbLavenderRadius,
                center = Offset(orbLavenderX, orbLavenderY)
            )

            // Cyan / Soft blue atmospheric depth orb at bottom right
            val orbCyanX = width * 0.85f + sin(phase * 0.6f) * (width * 0.05f)
            val orbCyanY = height * 0.80f - cos(phase * 0.6f) * (height * 0.05f)
            val orbCyanRadius = width * 0.65f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        if (isDark) Color(0x2038BDF8) else Color(0x2E38BDF8),
                        Color.Transparent
                    ),
                    center = Offset(orbCyanX, orbCyanY),
                    radius = orbCyanRadius
                ),
                radius = orbCyanRadius,
                center = Offset(orbCyanX, orbCyanY)
            )

            // Diagonal ambient glass reflection sheen (occasional gentle sweep)
            if (shimmerProgress in 0f..1.2f) {
                val startX = width * shimmerProgress
                val startY = 0f
                val endX = startX - width * 0.45f
                val endY = height
                drawLine(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            Color.Transparent,
                            if (isDark) Color.White.copy(alpha = 0.07f) else Color.White.copy(alpha = 0.18f),
                            Color.Transparent
                        ),
                        start = Offset(startX, startY),
                        end = Offset(endX, endY)
                    ),
                    start = Offset(startX, startY),
                    end = Offset(endX, endY),
                    strokeWidth = width * 0.35f
                )
            }
        }

        // Tonal optical vignette layer: keeps text crisp and readable in both dark and light modes
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    if (isDark) {
                        Brush.verticalGradient(
                            listOf(
                                Color.Black.copy(alpha = 0.45f),
                                Color.Black.copy(alpha = 0.15f),
                                Color.Black.copy(alpha = 0.55f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.08f),
                                Color.Transparent,
                                Color(0xFFF1F5F9).copy(alpha = 0.18f)
                            )
                        )
                    }
                )
        )

        // Render children on top of the liquid glass background
        content()
    }
}
