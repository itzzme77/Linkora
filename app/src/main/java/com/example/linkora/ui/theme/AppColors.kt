package com.example.linkora.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Linkora Design Tokens - Colors
 * Premium iOS Liquid Glass & WhatsApp-inspired messaging aesthetic.
 */
object LinkoraColors {
    // Core Brand & WhatsApp Emerald Palette
    val Emerald = Color(0xFF10B981)
    val EmeraldDark = Color(0xFF059669)
    val EmeraldLight = Color(0xFF34D399)
    val EmeraldGlow = Color(0x3310B981)

    val Lavender = Color(0xFFB8B8F5)
    val Periwinkle = Color(0xFF858BE8)
    val Indigo = Color(0xFF555DAE)
    val DeepNavy = Color(0xFF111424)
    val Violet = Color(0xFF8065D8)
    val Cyan = Color(0xFF38BDF8)
    val Rose = Color(0xFFF472B6)
    val White = Color(0xFFFFFFFF)
    val NearBlack = Color(0xFF090D16)

    // Glass Translucency Tokens (Light Mode - Authentic Frosted Ice Liquid Glass)
    val GlassSurfaceLight = Color(0xB3FFFFFF)          // 70% translucent frosted glass fill
    val GlassSurfaceLightElevated = Color(0xD9FFFFFF)  // 85% elevated frosted glass
    val GlassBorderLight = Color(0xB3FFFFFF)           // Bright crisp white glass refraction border
    val GlassHighlightLight = Color(0xF5FFFFFF)        // Luminous top rim highlight
    val GlassInnerGlowLight = Color(0x2E10B981)

    // Glass Translucency Tokens (Dark Mode - Midnight Obsidian Glass)
    val GlassSurfaceDark = Color(0xD9121626)           // 85% deep translucent obsidian slate
    val GlassSurfaceDarkElevated = Color(0xEB1A2035)   // 92% elevated dark slate
    val GlassBorderDark = Color(0x33FFFFFF)            // 20% crisp white border
    val GlassHighlightDark = Color(0x66FFFFFF)         // 40% top specular highlight
    val GlassInnerGlowDark = Color(0x2210B981)

    // Text Tokens (High contrast for accessibility)
    val TextPrimaryLight = Color(0xFF0F172A)
    val TextSecondaryLight = Color(0xFF334155)
    val TextTertiaryLight = Color(0xFF64748B)

    val TextPrimaryDark = Color(0xFFF8FAFC)
    val TextSecondaryDark = Color(0xFF94A3B8)
    val TextTertiaryDark = Color(0xFF64748B)

    // Accents & Badges
    val AccentPrimary = Color(0xFF059669)
    val AccentPrimaryDark = Color(0xFF10B981)
    val AccentSecondary = Color(0xFF6366F1)
    val UnreadBadge = Color(0xFF10B981)
    val OnlineStatus = Color(0xFF10B981)
    val OfflineStatus = Color(0xFF94A3B8)

    // Bubble Colors (WhatsApp iOS-inspired)
    val BubbleOutgoingLight = Color(0xFF059669)
    val BubbleOutgoingTextLight = Color(0xFFFFFFFF)
    val BubbleIncomingLight = Color(0xF2FFFFFF)
    val BubbleIncomingTextLight = Color(0xFF0F172A)

    val BubbleOutgoingDark = Color(0xFF10B981)
    val BubbleOutgoingTextDark = Color(0xFF090D16)
    val BubbleIncomingDark = Color(0xE61E243B)
    val BubbleIncomingTextDark = Color(0xFFF8FAFC)
}
