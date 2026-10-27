package com.example.linkora.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * GlassCard: The core Liquid Glass translucent card component for Linkora.
 *
 * Implements layered glassmorphism with:
 * - Refined translucent gradient fill
 * - Luminous multi-stop specular border (bright top-left to soft bottom-right)
 * - Subtle top-edge specular sheen highlight (iOS glass feel)
 * - Soft tonal shadow elevation
 * - Fluid spring press feedback
 */
@Composable
fun GlassCard(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(LinkoraRadius.radius20),
    cornerRadius: Dp = LinkoraRadius.radius20,
    elevation: Dp = LinkoraElevation.elevationCard,
    backgroundColor: Color? = null,
    backgroundBrush: Brush? = null,
    borderColor: Color? = null,
    borderBrush: Brush? = null,
    borderWidth: Dp = 1.dp,
    showTopHighlight: Boolean = true,
    isElevated: Boolean = false,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    val glassColors = LocalGlassColors.current

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    // Smooth spring press bounce
    val scale by animateFloatAsState(
        targetValue = if (isPressed && onClick != null) 0.975f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "glassCardPressScale"
    )

    val actualShape = if (shape != RoundedCornerShape(LinkoraRadius.radius20)) {
        shape
    } else {
        RoundedCornerShape(cornerRadius)
    }

    // Default liquid glass gradient
    // Default liquid glass gradient
    val defaultBgBrush = backgroundBrush ?: if (backgroundColor != null) {
        Brush.verticalGradient(listOf(backgroundColor, backgroundColor))
    } else {
        if (glassColors.isDark) {
            Brush.verticalGradient(
                listOf(
                    if (isElevated) Color(0xD91E2338) else Color(0xBF131728),
                    if (isElevated) Color(0xCC171C2E) else Color(0x990E111E)
                )
            )
        } else {
            Brush.verticalGradient(
                listOf(
                    if (isElevated) Color(0xD9FFFFFF) else Color(0xB8FFFFFF),
                    if (isElevated) Color(0xC7F8FAFC) else Color(0x99FFFFFF)
                )
            )
        }
    }

    // Specular luminous border
    val defaultBorderBrush = borderBrush ?: if (borderColor != null) {
        Brush.linearGradient(listOf(borderColor, borderColor))
    } else {
        if (glassColors.isDark) {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.38f),
                    Color.White.copy(alpha = 0.12f),
                    Color.White.copy(alpha = 0.22f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        } else {
            Brush.linearGradient(
                colors = listOf(
                    Color.White.copy(alpha = 0.95f),
                    Color(0xFFCBD5E1).copy(alpha = 0.45f),
                    Color.White.copy(alpha = 0.70f)
                ),
                start = Offset(0f, 0f),
                end = Offset(Float.POSITIVE_INFINITY, Float.POSITIVE_INFINITY)
            )
        }
    }

    val highlightColor = glassColors.highlight

    Box(
        modifier = modifier
            .scale(scale)
            .shadow(
                elevation = elevation,
                shape = actualShape,
                ambientColor = if (glassColors.isDark) Color(0x40000000) else Color(0x180F172A),
                spotColor = if (glassColors.isDark) Color(0x60000000) else Color(0x220F172A)
            )
            .clip(actualShape)
            .background(defaultBgBrush, actualShape)
            .then(
                if (showTopHighlight) {
                    Modifier.drawWithContent {
                        drawContent()
                        // Top-edge specular highlight reflection (iOS liquid glass feel)
                        val highlightBrush = Brush.horizontalGradient(
                            colors = listOf(
                                Color.Transparent,
                                highlightColor.copy(alpha = 0.4f),
                                highlightColor.copy(alpha = 0.85f),
                                highlightColor.copy(alpha = 0.4f),
                                Color.Transparent
                            )
                        )
                        drawLine(
                            brush = highlightBrush,
                            start = Offset(x = 18.dp.toPx(), y = 0.5f),
                            end = Offset(x = size.width - 18.dp.toPx(), y = 0.5f),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                } else Modifier
            )
            .border(
                width = borderWidth,
                brush = defaultBorderBrush,
                shape = actualShape
            )
            .then(
                if (onClick != null || onLongClick != null) {
                    @OptIn(ExperimentalFoundationApi::class)
                    Modifier.combinedClickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onClick?.invoke() },
                        onLongClick = onLongClick
                    )
                } else Modifier
            )
    ) {
        content()
    }
}
