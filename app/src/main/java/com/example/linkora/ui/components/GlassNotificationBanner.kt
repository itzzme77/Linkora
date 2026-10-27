package com.example.linkora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

data class TopNotificationData(
    val conversationId: String,
    val senderName: String,
    val messageText: String,
    val timestamp: Long = System.currentTimeMillis()
)

/**
 * GlassNotificationBanner: An iOS Dynamic Island / liquid glass heads-up banner
 * displayed at the top of the screen whenever a new message/SMS arrives.
 *
 * Features:
 * - Animated SMS arrival icon with glowing pulse
 * - Liquid glass translucent frosted surface & top specular highlight
 * - Tap to open conversation immediately
 * - Swipe-up to dismiss gesture
 * - Auto-dismiss timer (4.5s)
 */
@Composable
fun GlassNotificationBanner(
    notificationData: TopNotificationData?,
    onBannerClick: (String) -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val coroutineScope = rememberCoroutineScope()
    val offsetY = remember { Animatable(0f) }
    val density = LocalDensity.current

    // Pulsing aura animation for the SMS arrived icon
    val infiniteTransition = rememberInfiniteTransition(label = "smsPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    // Auto-dismiss countdown
    LaunchedEffect(notificationData) {
        if (notificationData != null) {
            offsetY.snapTo(0f)
            delay(4500)
            onDismiss()
        }
    }

    AnimatedVisibility(
        visible = notificationData != null,
        enter = slideInVertically(
            initialOffsetY = { -it },
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMediumLow
            )
        ) + fadeIn(tween(220)),
        exit = slideOutVertically(
            targetOffsetY = { -it },
            animationSpec = tween(180)
        ) + fadeOut(tween(140)),
        modifier = modifier
            .statusBarsPadding()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 6.dp)
    ) {
        notificationData?.let { data ->
            val swipeThresholdPx = with(density) { -35.dp.toPx() }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { IntOffset(0, offsetY.value.roundToInt()) }
                    .pointerInput(data.timestamp) {
                        detectVerticalDragGestures(
                            onDragEnd = {
                                if (offsetY.value <= swipeThresholdPx) {
                                    onDismiss()
                                } else {
                                    coroutineScope.launch {
                                        offsetY.animateTo(0f, spring(Spring.DampingRatioMediumBouncy))
                                    }
                                }
                            },
                            onVerticalDrag = { _, dragAmount ->
                                if (dragAmount < 0 || offsetY.value < 0) {
                                    coroutineScope.launch {
                                        offsetY.snapTo((offsetY.value + dragAmount).coerceAtMost(0f))
                                    }
                                }
                            }
                        )
                    }
                    .shadow(
                        elevation = LinkoraElevation.elevationFloating,
                        shape = RoundedCornerShape(LinkoraRadius.radius24),
                        ambientColor = if (glassColors.isDark) Color(0x70000000) else Color(0x250F172A),
                        spotColor = if (glassColors.isDark) Color(0x99000000) else Color(0x350F172A)
                    )
                    .clip(RoundedCornerShape(LinkoraRadius.radius24))
                    .background(
                        if (glassColors.isDark) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xF0181E32),
                                    Color(0xF50F1322)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.92f),
                                    Color(0xFFF8FAFC).copy(alpha = 0.82f)
                                )
                            )
                        },
                        RoundedCornerShape(LinkoraRadius.radius24)
                    )
                    .drawWithContent {
                        drawContent()
                        // Specular glass top edge reflection sheen
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (glassColors.isDark) Color.White.copy(alpha = 0.75f) else Color.White.copy(alpha = 0.95f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(24.dp.toPx(), 0.5f),
                            end = Offset(size.width - 24.dp.toPx(), 0.5f),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                    .border(
                        1.dp,
                        if (glassColors.isDark) {
                            Brush.linearGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.40f),
                                    Color.White.copy(alpha = 0.15f)
                                )
                            )
                        } else {
                            Brush.linearGradient(
                                listOf(
                                    Color.White,
                                    Color(0xFFCBD5E1).copy(alpha = 0.60f)
                                )
                            )
                        },
                        RoundedCornerShape(LinkoraRadius.radius24)
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = { onBannerClick(data.conversationId) }
                    )
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Glowing SMS Arrived Icon Badge
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .scale(pulseScale)
                            .clip(CircleShape)
                            .background(
                                Brush.radialGradient(
                                    listOf(
                                        LinkoraColors.Emerald,
                                        LinkoraColors.EmeraldDark
                                    )
                                ),
                                CircleShape
                            )
                            .border(
                                1.5.dp,
                                Brush.linearGradient(
                                    listOf(
                                        Color.White.copy(alpha = 0.85f),
                                        LinkoraColors.EmeraldLight
                                    )
                                ),
                                CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sms,
                            contentDescription = "New SMS Arrived",
                            tint = Color.White,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                    // Sender Name, Message Text, "New SMS" Tag
                    Column(modifier = Modifier.weight(1f)) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            // "NEW SMS" Pill Tag
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(LinkoraColors.Emerald.copy(alpha = 0.18f))
                                    .border(0.8.dp, LinkoraColors.Emerald.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                                    .padding(horizontal = 6.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = "NEW SMS",
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = LinkoraColors.Emerald
                                )
                            }

                            Text(
                                text = data.senderName,
                                style = LinkoraTypography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = glassColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Text(
                            text = data.messageText,
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textSecondary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Close Button
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(
                                if (glassColors.isDark) Color.White.copy(alpha = 0.12f)
                                else Color(0xFFCBD5E1).copy(alpha = 0.40f),
                                CircleShape
                            )
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Dismiss",
                            tint = glassColors.textSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}
