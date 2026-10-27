package com.example.linkora.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.linkora.ui.theme.LocalGlassColors

import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType

/**
 * GlassIconButton: Circular translucent action button with iOS-like liquid glass styling.
 * Supports ImageVector or Painter/Drawable resource, specular edge highlight, and spring press feedback.
 */
@Composable
fun GlassIconButton(
    contentDescription: String?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    iconRes: Int? = null,
    painter: Painter? = null,
    buttonSize: Dp = 42.dp,
    iconSize: Dp = 20.dp,
    iconTint: Color? = null,
    backgroundColor: Color? = null,
    borderColor: Color? = null
) {
    val glassColors = LocalGlassColors.current
    val haptic = LocalHapticFeedback.current

    val surfaceColor = backgroundColor ?: if (glassColors.isDark) {
        Color.White.copy(alpha = 0.16f)
    } else {
        Color.White.copy(alpha = 0.72f)
    }

    val strokeColor = borderColor ?: if (glassColors.isDark) {
        Color.White.copy(alpha = 0.32f)
    } else {
        Color.White.copy(alpha = 0.85f)
    }

    val tint = iconTint ?: glassColors.textPrimary

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.90f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassBtnPressScale"
    )

    Box(
        modifier = modifier
            .size(buttonSize + 6.dp) // Accessible touch target
            .scale(scale),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(buttonSize)
                .clip(CircleShape)
                .background(
                    Brush.verticalGradient(
                        listOf(
                            surfaceColor,
                            surfaceColor.copy(alpha = surfaceColor.alpha * 0.6f)
                        )
                    ),
                    CircleShape
                )
                .drawWithContent {
                    drawContent()
                    // Top rim specular highlight reflection
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = 0.85f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(size.width * 0.22f, 1f),
                        end = Offset(size.width * 0.78f, 1f),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
                .border(
                    1.dp,
                    Brush.linearGradient(
                        listOf(
                            strokeColor,
                            strokeColor.copy(alpha = strokeColor.alpha * 0.35f)
                        )
                    ),
                    CircleShape
                )
                .clickable(
                    interactionSource = interactionSource,
                    indication = null,
                    role = Role.Button,
                    onClick = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            when {
                icon != null -> {
                    Icon(
                        imageVector = icon,
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
                iconRes != null -> {
                    Icon(
                        painter = painterResource(id = iconRes),
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
                painter != null -> {
                    Icon(
                        painter = painter,
                        contentDescription = contentDescription,
                        tint = tint,
                        modifier = Modifier.size(iconSize)
                    )
                }
            }
        }
    }
}
