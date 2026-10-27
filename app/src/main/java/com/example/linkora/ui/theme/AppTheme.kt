package com.example.linkora.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

@Immutable
data class GlassThemeColors(
    val surface: Color,
    val surfaceElevated: Color,
    val border: Color,
    val highlight: Color,
    val innerGlow: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val textTertiary: Color,
    val accent: Color,
    val accentSecondary: Color,
    val unreadBadge: Color,
    val isDark: Boolean
) {
    val iconTint: Color get() = if (isDark) Color.White else textPrimary
}

val LocalGlassColors = staticCompositionLocalOf {
    GlassThemeColors(
        surface = LinkoraColors.GlassSurfaceLight,
        surfaceElevated = LinkoraColors.GlassSurfaceLightElevated,
        border = LinkoraColors.GlassBorderLight,
        highlight = LinkoraColors.GlassHighlightLight,
        innerGlow = LinkoraColors.GlassInnerGlowLight,
        textPrimary = LinkoraColors.TextPrimaryLight,
        textSecondary = LinkoraColors.TextSecondaryLight,
        textTertiary = LinkoraColors.TextTertiaryLight,
        accent = LinkoraColors.AccentPrimary,
        accentSecondary = LinkoraColors.AccentSecondary,
        unreadBadge = LinkoraColors.UnreadBadge,
        isDark = false
    )
}

private val LightColorScheme = lightColorScheme(
    primary = LinkoraColors.AccentPrimary,
    secondary = LinkoraColors.AccentSecondary,
    tertiary = LinkoraColors.Lavender,
    background = Color.Transparent,
    surface = LinkoraColors.GlassSurfaceLight,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LinkoraColors.TextPrimaryLight,
    onSurface = LinkoraColors.TextPrimaryLight
)

private val DarkColorScheme = darkColorScheme(
    primary = LinkoraColors.AccentPrimaryDark,
    secondary = LinkoraColors.Periwinkle,
    tertiary = LinkoraColors.Indigo,
    background = Color.Transparent,
    surface = LinkoraColors.GlassSurfaceDark,
    onPrimary = Color.White,
    onSecondary = Color.White,
    onBackground = LinkoraColors.TextPrimaryDark,
    onSurface = LinkoraColors.TextPrimaryDark
)

@Composable
fun LinkoraTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    val glassColors = if (darkTheme) {
        GlassThemeColors(
            surface = LinkoraColors.GlassSurfaceDark,
            surfaceElevated = LinkoraColors.GlassSurfaceDarkElevated,
            border = LinkoraColors.GlassBorderDark,
            highlight = LinkoraColors.GlassHighlightDark,
            innerGlow = LinkoraColors.GlassInnerGlowDark,
            textPrimary = LinkoraColors.TextPrimaryDark,
            textSecondary = LinkoraColors.TextSecondaryDark,
            textTertiary = LinkoraColors.TextTertiaryDark,
            accent = LinkoraColors.AccentPrimaryDark,
            accentSecondary = LinkoraColors.Periwinkle,
            unreadBadge = LinkoraColors.UnreadBadge,
            isDark = true
        )
    } else {
        GlassThemeColors(
            surface = LinkoraColors.GlassSurfaceLight,
            surfaceElevated = LinkoraColors.GlassSurfaceLightElevated,
            border = LinkoraColors.GlassBorderLight,
            highlight = LinkoraColors.GlassHighlightLight,
            innerGlow = LinkoraColors.GlassInnerGlowLight,
            textPrimary = LinkoraColors.TextPrimaryLight,
            textSecondary = LinkoraColors.TextSecondaryLight,
            textTertiary = LinkoraColors.TextTertiaryLight,
            accent = LinkoraColors.AccentPrimary,
            accentSecondary = LinkoraColors.AccentSecondary,
            unreadBadge = LinkoraColors.UnreadBadge,
            isDark = false
        )
    }

    CompositionLocalProvider(LocalGlassColors provides glassColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = LinkoraTypography,
            content = content
        )
    }
}
