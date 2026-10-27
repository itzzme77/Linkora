package com.example.linkora.ui.screens.chats.model

import androidx.compose.ui.graphics.Color

data class StoryItem(
    val id: String,
    val name: String,
    val isMyStatus: Boolean = false,
    val hasUnseenStatus: Boolean = false
)

data class ConversationItem(
    val id: String,
    val name: String,
    val lastMessage: String,
    val timestamp: String,
    val unreadCount: Int = 0,
    val isPinned: Boolean = false,
    val isMuted: Boolean = false,
    val isOnline: Boolean = false,
    val isGroup: Boolean = false,
    val isOutgoingLast: Boolean = false,
    val isDelivered: Boolean = true,
    val isRead: Boolean = true,
    val avatarGradient: List<Color>? = null,
    val isArchived: Boolean = false
)

