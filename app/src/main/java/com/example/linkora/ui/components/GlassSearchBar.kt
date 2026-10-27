package com.example.linkora.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.R
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * GlassSearchBar: iOS-inspired liquid glass capsule search bar with specular highlights
 * and filter slider action.
 */
@Composable
fun GlassSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholderText: String = "Search chats, contacts, messages...",
    onFilterClick: (() -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current

    val bgColor = if (glassColors.isDark) {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.16f),
                Color.White.copy(alpha = 0.08f)
            )
        )
    } else {
        Brush.verticalGradient(
            listOf(
                Color.White.copy(alpha = 0.82f),
                Color(0xFFF1F5F9).copy(alpha = 0.65f)
            )
        )
    }

    val borderColor = if (glassColors.isDark) {
        Brush.linearGradient(
            listOf(
                Color.White.copy(alpha = 0.35f),
                Color.White.copy(alpha = 0.10f)
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                Color.White,
                Color(0xFFCBD5E1).copy(alpha = 0.70f)
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(CircleShape)
            .background(bgColor, CircleShape)
            .drawWithContent {
                drawContent()
                // Top-edge subtle specular sheen
                drawLine(
                    brush = Brush.horizontalGradient(
                        listOf(
                            Color.Transparent,
                            if (glassColors.isDark) Color.White.copy(alpha = 0.70f) else Color.White.copy(alpha = 0.90f),
                            Color.Transparent
                        )
                    ),
                    start = Offset(28.dp.toPx(), 0.5f),
                    end = Offset(size.width - 28.dp.toPx(), 0.5f),
                    strokeWidth = 1.dp.toPx()
                )
            }
            .border(
                1.dp,
                borderColor,
                CircleShape
            )
            .padding(horizontal = LinkoraSpacing.space16),
        contentAlignment = Alignment.CenterStart
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Search,
                contentDescription = "Search",
                tint = glassColors.textSecondary,
                modifier = Modifier.size(20.dp)
            )

            Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

            Box(modifier = Modifier.weight(1f)) {
                if (query.isEmpty()) {
                    Text(
                        text = placeholderText,
                        style = LinkoraTypography.bodyMedium,
                        color = glassColors.textTertiary,
                        fontSize = 14.sp
                    )
                }

                BasicTextField(
                    value = query,
                    onValueChange = onQueryChange,
                    singleLine = true,
                    textStyle = LinkoraTypography.bodyMedium.copy(
                        color = glassColors.textPrimary,
                        fontSize = 14.sp
                    ),
                    cursorBrush = SolidColor(glassColors.accent),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = query.isNotEmpty(),
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (glassColors.isDark) Color.White.copy(alpha = 0.2f)
                            else Color(0xFFCBD5E1).copy(alpha = 0.5f),
                            CircleShape
                        )
                        .clickable { onQueryChange("") },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Clear",
                        tint = glassColors.textPrimary,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            if (onFilterClick != null && query.isEmpty()) {
                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                Box(
                    modifier = Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .clickable(onClick = onFilterClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_tune),
                        contentDescription = "Filters",
                        tint = Color.White.copy(alpha = 0.9f),
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
