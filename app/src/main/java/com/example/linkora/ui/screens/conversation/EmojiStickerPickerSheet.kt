package com.example.linkora.ui.screens.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Backspace
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Gif
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Send
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.ThumbUp
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

enum class PickerTab {
    EMOJI,
    STICKERS,
    GIFS
}

data class StickerItem(
    val id: String,
    val emoji: String,
    val title: String,
    val tag: String,
    val gradient: List<Color>
)

data class GifReactionItem(
    val id: String,
    val title: String,
    val previewEmoji: String,
    val description: String,
    val accentColor: Color
)

@Composable
fun EmojiStickerPickerSheet(
    onEmojiSelected: (String) -> Unit,
    onStickerSelected: (String) -> Unit,
    onGifSelected: (String) -> Unit,
    onBackspace: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val haptic = LocalHapticFeedback.current
    var selectedTab by remember { mutableStateOf(PickerTab.EMOJI) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedCategoryIndex by remember { mutableIntStateOf(0) }

    // Categories of Emojis
    val emojiCategories = remember {
        listOf(
            "Smileys" to listOf(
                "😀", "😃", "😄", "😁", "😆", "😅", "🤣", "😂", "🙂", "🙃", "😉", "😊", "😇", "🥰", "😍", "🤩",
                "😘", "😗", "😋", "😛", "😜", "🤪", "😝", "🤗", "🤭", "🤫", "🤔", "🫡", "🤐", "🤨", "😐", "😑",
                "😶", "🫥", "😏", "😒", "🙄", "😬", "🤥", "😌", "😔", "😪", "🤤", "😴", "😷", "🤒", "🤕", "🤢",
                "🤮", "🤧", "🥵", "🥶", "🥴", "😵", "🤯", "🤠", "🥳", "🥸", "😎", "🤓", "🧐", "😕", "😟", "🙁",
                "😮", "😯", "😲", "😳", "🥺", "🥹", "😦", "😧", "📁", "😱", "🥱", "😤", "😡", "😠", "🤬", "💀"
            ),
            "Gestures" to listOf(
                "👋", "🤚", "🖐️", "✋", "🖖", "🫱", "🫲", "🫳", "🫴", "👌", "🤌", "🤏", "✌️", "🤞", "🫰", "🤟",
                "🤘", "🤙", "👈", "👉", "👆", "🖕", "👇", "☝️", "🫵", "👍", "👎", "✊", "👊", "🤛", "🤜", "👏",
                "🙌", "🫶", "👐", "🤲", "🤝", "🙏", "✍️", "💅", "🤳", "💪", "🦾", "🦿", "🦵", "🦶", "👂", "🦻"
            ),
            "Hearts & Love" to listOf(
                "❤️", "🧡", "💛", "💚", "💙", "💜", "🖤", "🤍", "🤎", "💔", "❤️‍🔥", "❤️‍🩹", "❣️", "💕", "💞", "💓",
                "💗", "💖", "💘", "💝", "💟", "✨", "🌟", "💫", "💥", "💯", "🔥", "⚡", "🌈", "☀️", "🌙", "⭐"
            ),
            "Tech & Mesh" to listOf(
                "📱", "📲", "💻", "⌨️", "🖥️", "🖱️", "🖲️", "🕹️", "💽", "💾", "💿", "📀", "📷", "📸", "📹", "🎥",
                "📞", "☎️", "📟", "📡", "🔋", "🪫", "🔌", "💡", "🔦", "🕯️", "🔐", "🔒", "🔓", "🔑", "🗝️", "🛡️"
            )
        )
    }

    // Curated High-Fidelity Decentralized & Expressive Stickers
    val stickerList = remember {
        listOf(
            StickerItem("st_1", "🚀", "To the Moon!", "Hype", listOf(Color(0xFF3B82F6), Color(0xFF1D4ED8))),
            StickerItem("st_2", "🔒", "100% Encrypted", "Security", listOf(Color(0xFF10B981), Color(0xFF047857))),
            StickerItem("st_3", "🌐", "Mesh Relay Active", "Network", listOf(Color(0xFF06B6D4), Color(0xFF0E7490))),
            StickerItem("st_4", "🎉", "Let's Celebrate!", "Party", listOf(Color(0xFFEC4899), Color(0xFFBE185D))),
            StickerItem("st_5", "☕", "Coffee First", "Chill", listOf(Color(0xFFF59E0B), Color(0xFFB45309))),
            StickerItem("st_6", "💯", "Keep It 100", "True", listOf(Color(0xFFEF4444), Color(0xFFB91C1C))),
            StickerItem("st_7", "⚡", "Offline Power", "Tech", listOf(Color(0xFF8B5CF6), Color(0xFF6D28D9))),
            StickerItem("st_8", "🤙", "Sounds Good", "Agree", listOf(Color(0xFF14B8A6), Color(0xFF0F766E))),
            StickerItem("st_9", "🫡", "Roger That!", "Respect", listOf(Color(0xFF64748B), Color(0xFF334155))),
            StickerItem("st_10", "🤝", "Deal Done", "Business", listOf(Color(0xFF6366F1), Color(0xFF4338CA)))
        )
    }

    // High-impact offline GIF reactions
    val gifReactions = remember {
        listOf(
            GifReactionItem("gif_1", "Mind Blown", "🤯", "Complete disbelief reaction", Color(0xFFF59E0B)),
            GifReactionItem("gif_2", "Party Popper", "🎉", "Celebration vibe", Color(0xFFEC4899)),
            GifReactionItem("gif_3", "Dancing Vibe", "💃", "Groovy mood", Color(0xFF8B5CF6)),
            GifReactionItem("gif_4", "High Five", "✋", "Team accomplishment", Color(0xFF10B981)),
            GifReactionItem("gif_5", "Rolling Laugh", "🤣", "Pure comedy gold", Color(0xFF06B6D4)),
            GifReactionItem("gif_6", "Hacker Mode", "💻", "Decentralized mesh power", Color(0xFF10B981))
        )
    }

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .height(340.dp)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        shape = RoundedCornerShape(24.dp),
        cornerRadius = 24.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = 10.dp, bottom = 6.dp)
        ) {
            // Header: Top Tab Selector (Emoji / Stickers / GIFs) & Close
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 14.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Tab Pill Capsules
                Row(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(glassColors.surface, CircleShape)
                        .border(1.dp, glassColors.border, CircleShape)
                        .padding(3.dp),
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    PickerTabButton(
                        title = "Emoji",
                        selected = selectedTab == PickerTab.EMOJI,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTab = PickerTab.EMOJI
                        }
                    )
                    PickerTabButton(
                        title = "Stickers",
                        selected = selectedTab == PickerTab.STICKERS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTab = PickerTab.STICKERS
                        }
                    )
                    PickerTabButton(
                        title = "GIFs",
                        selected = selectedTab == PickerTab.GIFS,
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            selectedTab = PickerTab.GIFS
                        }
                    )
                }

                // Close Button
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(CircleShape)
                        .background(glassColors.surface, CircleShape)
                        .border(1.dp, glassColors.border, CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close Picker",
                        tint = glassColors.iconTint,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Main Content depending on Tab
            when (selectedTab) {
                PickerTab.EMOJI -> {
                    // Category Jump Buttons & Search / Backspace
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            emojiCategories.forEachIndexed { index, (catName, _) ->
                                val isCatSelected = selectedCategoryIndex == index
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(
                                            if (isCatSelected) LinkoraColors.Emerald.copy(alpha = 0.2f)
                                            else Color.Transparent
                                        )
                                        .border(
                                            1.dp,
                                            if (isCatSelected) LinkoraColors.Emerald.copy(alpha = 0.6f)
                                            else Color.Transparent,
                                            RoundedCornerShape(8.dp)
                                        )
                                        .clickable {
                                            selectedCategoryIndex = index
                                        }
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = catName,
                                        fontSize = 11.sp,
                                        fontWeight = if (isCatSelected) FontWeight.Bold else FontWeight.Normal,
                                        color = if (isCatSelected) LinkoraColors.Emerald else glassColors.textSecondary
                                    )
                                }
                            }
                        }

                        // Backspace Key
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(glassColors.surface, RoundedCornerShape(8.dp))
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onBackspace()
                                },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Backspace,
                                contentDescription = "Delete",
                                tint = glassColors.textPrimary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    val currentEmojis = emojiCategories[selectedCategoryIndex].second

                    // Emojis Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(8),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 10.dp),
                        contentPadding = PaddingValues(bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        items(currentEmojis) { emoji ->
                            EmojiCell(
                                emoji = emoji,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    onEmojiSelected(emoji)
                                }
                            )
                        }
                    }
                }

                PickerTab.STICKERS -> {
                    // Stickers Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(stickerList) { sticker ->
                            StickerCard(
                                sticker = sticker,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onStickerSelected("🏷️ [Sticker: ${sticker.title} ${sticker.emoji}]")
                                }
                            )
                        }
                    }
                }

                PickerTab.GIFS -> {
                    // GIFs / Expressive Reactions Grid
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 12.dp),
                        contentPadding = PaddingValues(top = 4.dp, bottom = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(gifReactions) { gif ->
                            GifReactionCard(
                                gif = gif,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onGifSelected("🎬 [GIF: ${gif.previewEmoji} ${gif.title}]")
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PickerTabButton(
    title: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(
                if (selected) LinkoraColors.Emerald
                else Color.Transparent,
                CircleShape
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 5.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
            color = if (selected) Color.White else glassColors.textSecondary
        )
    }
}

@Composable
private fun EmojiCell(
    emoji: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 1.35f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "emojiScale"
    )

    Box(
        modifier = Modifier
            .size(38.dp)
            .scale(scale)
            .clip(RoundedCornerShape(8.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = emoji,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun StickerCard(
    sticker: StickerItem,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "stickerScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(sticker.gradient),
                RoundedCornerShape(16.dp)
            )
            .border(1.dp, Color.White.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = sticker.emoji,
                fontSize = 32.sp
            )
            Column {
                Text(
                    text = sticker.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = sticker.tag,
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.8f)
                )
            }
        }
    }
}

@Composable
private fun GifReactionCard(
    gif: GifReactionItem,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        label = "gifScale"
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(84.dp)
            .scale(scale)
            .clip(RoundedCornerShape(16.dp))
            .background(glassColors.surfaceElevated, RoundedCornerShape(16.dp))
            .border(1.dp, gif.accentColor.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(10.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(CircleShape)
                    .background(gif.accentColor.copy(alpha = 0.2f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = gif.previewEmoji,
                    fontSize = 22.sp
                )
            }
            Column {
                Text(
                    text = gif.title,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Animated GIF",
                    fontSize = 10.sp,
                    color = gif.accentColor,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
