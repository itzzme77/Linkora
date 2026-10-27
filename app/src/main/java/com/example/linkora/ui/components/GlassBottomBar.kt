package com.example.linkora.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.ChatBubbleOutline
import androidx.compose.material.icons.rounded.PeopleOutline
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LocalGlassColors
import kotlinx.coroutines.launch

enum class NavigationTab(
    val title: String,
    val icon: ImageVector
) {
    CHATS("Chats", Icons.Rounded.ChatBubbleOutline),
    CALLS("Calls", Icons.Rounded.Call),
    CONTACTS("Contacts", Icons.Rounded.PeopleOutline),
    PROFILE("Settings", Icons.Rounded.Settings)
}

/**
 * GlassBottomBar: Liquid Frosted Glass Navigation Bar featuring a sliding water-droplet
 * indicator with liquid stretch & bounce animation flowing seamlessly across tabs, plus
 * a matching standalone circular glass search action button.
 */
@Composable
fun GlassBottomBar(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit,
    onSearchClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val scrimColor = if (glassColors.isDark) Color(0xFF090C16) else Color(0xFFF1F5F9)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Transparent,
                        scrimColor.copy(alpha = if (glassColors.isDark) 0.50f else 0.25f),
                        scrimColor.copy(alpha = if (glassColors.isDark) 0.95f else 0.65f)
                    )
                )
            )
            .navigationBarsPadding()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 1. Main Frosted Glass Stadium Capsule
            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(64.dp)
                    .shadow(
                        elevation = 16.dp,
                        shape = CircleShape,
                        ambientColor = if (glassColors.isDark) Color(0x50000000) else Color(0x18000000),
                        spotColor = if (glassColors.isDark) Color(0x80000000) else Color(0x22000000)
                    )
                    .clip(CircleShape)
                    .background(
                        brush = if (glassColors.isDark) {
                            Brush.verticalGradient(
                                listOf(
                                    Color(0xE61E2333),
                                    Color(0xF0121624)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.85f),
                                    Color(0xFFF1F5F9).copy(alpha = 0.70f)
                                )
                            )
                        }
                    )
                    .drawWithContent {
                        drawContent()
                        // Top rim specular highlight reflection
                        drawLine(
                            brush = Brush.horizontalGradient(
                                listOf(
                                    Color.Transparent,
                                    if (glassColors.isDark) Color.White.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.90f),
                                    Color.Transparent
                                )
                            ),
                            start = Offset(24.dp.toPx(), 0.8f),
                            end = Offset(size.width - 24.dp.toPx(), 0.8f),
                            strokeWidth = 1.2.dp.toPx()
                        )
                    }
                    .border(
                        width = 1.dp,
                        brush = if (glassColors.isDark) {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White.copy(alpha = 0.35f),
                                    Color.White.copy(alpha = 0.10f)
                                )
                            )
                        } else {
                            Brush.verticalGradient(
                                listOf(
                                    Color.White,
                                    Color(0xFFCBD5E1).copy(alpha = 0.60f)
                                )
                            )
                        },
                        shape = CircleShape
                    )
            ) {
                CapsuleContent(
                    selectedTab = selectedTab,
                    onTabSelected = onTabSelected
                )
            }

            // 2. Standalone Circular Glass Droplet Button (Search)
            if (onSearchClick != null) {
                Spacer(modifier = Modifier.width(10.dp))
                CircularGlassActionButton(
                    icon = Icons.Rounded.Search,
                    contentDescription = "Search",
                    onClick = onSearchClick
                )
            }
        }
    }
}

/**
 * CapsuleContent: Contains the animated sliding water droplet pill and tab icons/labels.
 */
@Composable
private fun CapsuleContent(
    selectedTab: NavigationTab,
    onTabSelected: (NavigationTab) -> Unit
) {
    val glassColors = LocalGlassColors.current
    val haptic = LocalHapticFeedback.current

    var previousTab by remember { mutableStateOf(selectedTab) }
    val stretchAnimatable = remember { Animatable(0f) }

    LaunchedEffect(selectedTab) {
        if (selectedTab != previousTab) {
            previousTab = selectedTab
            stretchAnimatable.snapTo(1f)
            stretchAnimatable.animateTo(
                targetValue = 0f,
                animationSpec = spring(
                    dampingRatio = 0.65f, // Liquid stretch bounce
                    stiffness = Spring.StiffnessMediumLow
                )
            )
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 6.dp, vertical = 5.dp)
    ) {
        val tabEntries = NavigationTab.entries
        val tabCount = tabEntries.size
        val itemWidth = maxWidth / tabCount
        val dropletWidth = itemWidth - 4.dp
        val dropletHeight = maxHeight
        val targetIndex = selectedTab.ordinal

        val targetOffsetX = (itemWidth * targetIndex) + 2.dp
        val animatedOffsetX by animateDpAsState(
            targetValue = targetOffsetX,
            animationSpec = spring(
                dampingRatio = 0.72f, // Liquid water droplet fluid motion
                stiffness = Spring.StiffnessMediumLow
            ),
            label = "waterDropletOffset"
        )

        val stretchValue = stretchAnimatable.value
        val scaleX = 1.0f + (stretchValue * 0.10f)
        val scaleY = 1.0f - (stretchValue * 0.05f)

        // The Sliding Water Droplet Indicator (Behind tabs in one continuous flow)
        Box(
            modifier = Modifier
                .offset(x = animatedOffsetX, y = 0.dp)
                .width(dropletWidth)
                .height(dropletHeight)
                .scale(scaleX = scaleX, scaleY = scaleY)
                .shadow(
                    elevation = 6.dp,
                    shape = RoundedCornerShape(percent = 50),
                    ambientColor = if (glassColors.isDark) Color(0x60000000) else Color(0x15000000),
                    spotColor = if (glassColors.isDark) Color(0x90000000) else Color(0x20000000)
                )
                .clip(RoundedCornerShape(percent = 50))
                .background(
                    brush = if (glassColors.isDark) {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.22f),
                                Color.White.copy(alpha = 0.08f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.95f),
                                Color(0xFFE2E8F0).copy(alpha = 0.70f)
                            )
                        )
                    }
                )
                .drawWithContent {
                    drawContent()
                    // Water droplet top specular gleaming line
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                if (glassColors.isDark) Color.White.copy(alpha = 0.75f) else Color.White,
                                Color.Transparent
                            )
                        ),
                        start = Offset(12.dp.toPx(), 0.5f),
                        end = Offset(size.width - 12.dp.toPx(), 0.5f),
                        strokeWidth = 1.2.dp.toPx()
                    )
                }
                .border(
                    width = 1.dp,
                    brush = if (glassColors.isDark) {
                        Brush.verticalGradient(
                            listOf(
                                Color.White.copy(alpha = 0.50f),
                                Color.White.copy(alpha = 0.15f)
                            )
                        )
                    } else {
                        Brush.verticalGradient(
                            listOf(
                                Color.White,
                                Color(0xFFCBD5E1).copy(alpha = 0.60f)
                            )
                        )
                    },
                    shape = RoundedCornerShape(percent = 50)
                )
        )

        // Tab Items Row (Icons and Text)
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            tabEntries.forEach { tab ->
                val isSelected = tab == selectedTab

                val iconScale by animateFloatAsState(
                    targetValue = if (isSelected) 1.14f else 1.0f,
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioMediumBouncy,
                        stiffness = Spring.StiffnessMedium
                    ),
                    label = "tabIconScale"
                )

                val activeColor = if (isSelected) {
                    if (glassColors.isDark) Color.White else Color(0xFF0F172A)
                } else {
                    if (glassColors.isDark) Color.White.copy(alpha = 0.55f) else Color(0xFF64748B)
                }

                Box(
                    modifier = Modifier
                        .width(itemWidth)
                        .fillMaxHeight()
                        .clip(RoundedCornerShape(percent = 50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = {
                                if (tab != selectedTab) {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onTabSelected(tab)
                                }
                            }
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = tab.icon,
                            contentDescription = tab.title,
                            tint = activeColor,
                            modifier = Modifier
                                .size(22.dp)
                                .scale(iconScale)
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = tab.title,
                            color = activeColor,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

/**
 * CircularGlassActionButton: The detached circular glass droplet button on the right (Search/Action).
 */
@Composable
private fun CircularGlassActionButton(
    icon: ImageVector,
    contentDescription: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()
    val pressScale = remember { Animatable(1.0f) }

    Box(
        modifier = modifier
            .size(64.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = if (glassColors.isDark) Color(0x50000000) else Color(0x18000000),
                spotColor = if (glassColors.isDark) Color(0x80000000) else Color(0x22000000)
            )
            .clip(CircleShape)
            .background(
                brush = if (glassColors.isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color(0xE61E2333),
                            Color(0xF0121624)
                        )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.85f),
                            Color(0xFFF1F5F9).copy(alpha = 0.70f)
                        )
                    )
                }
            )
            .drawWithContent {
                drawContent()
                // Top rim specular highlight reflection
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (glassColors.isDark) Color.White.copy(alpha = 0.60f) else Color.White.copy(alpha = 0.90f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(12.dp.toPx(), 0.8f),
                    end = Offset(size.width - 12.dp.toPx(), 0.8f),
                    strokeWidth = 1.2.dp.toPx()
                )
            }
            .border(
                width = 1.dp,
                brush = if (glassColors.isDark) {
                    Brush.verticalGradient(
                        listOf(
                            Color.White.copy(alpha = 0.35f),
                            Color.White.copy(alpha = 0.10f)
                            )
                    )
                } else {
                    Brush.verticalGradient(
                        listOf(
                            Color.White,
                            Color(0xFFCBD5E1).copy(alpha = 0.60f)
                        )
                    )
                },
                shape = CircleShape
            )
            .scale(pressScale.value)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        coroutineScope.launch {
                            pressScale.animateTo(0.92f, tween(100))
                        }
                        tryAwaitRelease()
                        coroutineScope.launch {
                            pressScale.animateTo(
                                1.0f,
                                spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    },
                    onTap = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        onClick()
                    }
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            tint = if (glassColors.isDark) Color.White.copy(alpha = 0.85f) else Color(0xFF334155),
            modifier = Modifier.size(24.dp)
        )
    }
}
