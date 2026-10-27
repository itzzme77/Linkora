package com.example.linkora.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.R
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * GlassAvatar: Circular contact avatar with photo or initials, refined glass rim, and online status badge.
 */
@Composable
fun GlassAvatar(
    name: String,
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
    avatarRes: Int? = null,
    avatarGradient: List<Color>? = null,
    isOnline: Boolean = false
) {
    val glassColors = LocalGlassColors.current

    // Resolve matching photo if available from assets or parameter
    val resolvedAvatarRes = avatarRes ?: getAvatarResForName(name)

    // Deterministic initials fallback
    val initials = name.split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .map { it.first().uppercase() }
        .joinToString("")
        .ifEmpty { "L" }

    val defaultGradient = avatarGradient ?: run {
        val hash = name.hashCode()
        when (kotlin.math.abs(hash) % 4) {
            0 -> listOf(Color(0xFF858BE8), Color(0xFF555DAE))
            1 -> listOf(Color(0xFF8065D8), Color(0xFFB8B8F5))
            2 -> listOf(Color(0xFF6366F1), Color(0xFF7BDFF2))
            else -> listOf(Color(0xFF7C5CE8), Color(0xFFE7A6D8))
        }
    }

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        if (resolvedAvatarRes != null) {
            Image(
                painter = painterResource(id = resolvedAvatarRes),
                contentDescription = name,
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        CircleShape
                    )
            )
        } else {
            Box(
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .background(Brush.linearGradient(defaultGradient), CircleShape)
                    .border(
                        1.5.dp,
                        Brush.linearGradient(
                            listOf(
                                Color.White.copy(alpha = 0.6f),
                                Color.White.copy(alpha = 0.2f)
                            )
                        ),
                        CircleShape
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = Color.White,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = (size.value * 0.36f).sp
                )
            }
        }

        if (isOnline) {
            Box(
                modifier = Modifier
                    .size(size * 0.28f)
                    .align(Alignment.BottomEnd)
                    .offset(x = 1.dp, y = 1.dp)
                    .clip(CircleShape)
                    .background(LinkoraColors.OnlineStatus, CircleShape)
                    .border(2.dp, if (glassColors.isDark) Color(0xFF1E233D) else Color.White, CircleShape)
            )
        }
    }
}

private fun getAvatarResForName(name: String): Int? {
    val lower = name.lowercase()
    return when {
        lower == "you" || lower.contains("self") -> R.drawable.avatar_user
        lower.contains("rohan") -> R.drawable.avatar_rohan
        lower.contains("sneha") -> R.drawable.avatar_sneha
        lower.contains("college") -> R.drawable.avatar_college
        lower.contains("travel") -> R.drawable.avatar_travel
        lower.contains("family") -> R.drawable.avatar_family
        lower.contains("aditya") -> R.drawable.avatar_aditya
        lower.contains("priya") -> R.drawable.avatar_priya
        lower.contains("arjun") -> R.drawable.avatar_arjun
        lower.contains("elena") -> R.drawable.avatar_sneha
        lower.contains("marcus") -> R.drawable.avatar_rohan
        lower.contains("sophia") -> R.drawable.avatar_priya
        lower.contains("alex") -> R.drawable.avatar_aditya
        else -> null
    }
}
