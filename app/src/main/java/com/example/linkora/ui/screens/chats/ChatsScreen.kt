package com.example.linkora.ui.screens.chats

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.Archive
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.NotificationsOff
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Unarchive
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.R
import com.example.linkora.model.Contact
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.components.GlassSearchBar
import com.example.linkora.ui.screens.chats.model.ConversationItem
import com.example.linkora.ui.screens.chats.model.StoryItem
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

enum class ChatFilter(val label: String) {
    ALL("All"),
    UNREAD("Unread"),
    GROUPS("Groups"),
    FAVORITES("Favorites"),
    ARCHIVED("Archived")
}

/**
 * ChatsScreen: Linkora Liquid Glass Chats Home screen matching the high-fidelity iOS mockup.
 */
@Composable
fun ChatsScreen(
    conversations: List<ConversationItem>,
    contacts: List<Contact> = emptyList(),
    isMeshActive: Boolean = true,
    connectedPeersCount: Int = 0,
    onToggleMesh: (Boolean) -> Unit = {},
    onConversationClick: (ConversationItem) -> Unit,
    onDeleteConversation: ((conversationId: String) -> Unit)? = null,
    onArchiveConversation: ((conversationId: String, archive: Boolean) -> Unit)? = null,
    onContactClick: (String) -> Unit = {},
    onNewChatClick: () -> Unit,
    onShowMyQr: () -> Unit,
    onScanQrClick: () -> Unit,
    onOpenRadar: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    var activeFilter by remember { mutableStateOf(ChatFilter.ALL) }
    var searchQuery by remember { mutableStateOf("") }
    var showSearchBar by remember { mutableStateOf(true) }
    var conversationToDelete by remember { mutableStateOf<ConversationItem?>(null) }

    val unreadTotal = remember(conversations) {
        conversations.sumOf { it.unreadCount }
    }

    val filteredConversations = remember(activeFilter, searchQuery, conversations) {
        conversations.filter { conv ->
            val matchesSearch = searchQuery.isBlank() ||
                conv.name.contains(searchQuery, ignoreCase = true) ||
                conv.lastMessage.contains(searchQuery, ignoreCase = true)

            val matchesFilter = when (activeFilter) {
                ChatFilter.ALL -> !conv.isArchived
                ChatFilter.UNREAD -> !conv.isArchived && conv.unreadCount > 0
                ChatFilter.GROUPS -> !conv.isArchived && conv.isGroup
                ChatFilter.FAVORITES -> !conv.isArchived && conv.isPinned
                ChatFilter.ARCHIVED -> conv.isArchived
            }

            matchesSearch && matchesFilter
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
        ) {
            // Top App Bar: "Linkora" + Circular Action Buttons
            TopGlassHeader(
                onScanQrClick = onScanQrClick,
                onSearchToggle = { showSearchBar = !showSearchBar },
                onShowMyQr = onShowMyQr,
                onAvatarClick = onShowMyQr,
                unreadCount = unreadTotal,
                onUnreadClick = { activeFilter = ChatFilter.UNREAD }
            )

            // Filter Pills Row (All, Unread 3, Groups, Favorites)
            FilterPillsRow(
                activeFilter = activeFilter,
                unreadCount = unreadTotal,
                onFilterSelected = { activeFilter = it }
            )

            // Liquid Glass Search Bar
            AnimatedVisibility(
                visible = showSearchBar,
                enter = fadeIn(tween(180)),
                exit = fadeOut(tween(180))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LinkoraSpacing.space16, vertical = 6.dp)
                ) {
                    GlassSearchBar(
                        query = searchQuery,
                        onQueryChange = { searchQuery = it },
                        placeholderText = "Search chats, contacts, messages...",
                        onFilterClick = null
                    )
                }
            }

            // Compact Stories Carousel ("Add Story", "Your Story", and Contacts)
            StoriesCarousel(
                contacts = contacts,
                onAddStoryClick = onShowMyQr,
                onYourStoryClick = onShowMyQr,
                onContactClick = onContactClick
            )

            // Conversations List (Liquid Glass Cards)
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    start = LinkoraSpacing.space16,
                    end = LinkoraSpacing.space16,
                    top = 6.dp,
                    bottom = 120.dp // Clearance for floating bottom dock
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (filteredConversations.isEmpty()) {
                    item {
                        EmptyChatsCard(
                            searchQuery = searchQuery,
                            onNewChatClick = onNewChatClick,
                            onScanQrClick = onScanQrClick
                        )
                    }
                } else {
                    items(
                        items = filteredConversations,
                        key = { it.id }
                    ) { conversation ->
                        LiquidGlassConversationCard(
                            conversation = conversation,
                            onClick = { onConversationClick(conversation) },
                            onLongClick = { conversationToDelete = conversation }
                        )
                    }
                }
            }
        }

        // Confirmation Dialog for Deleting Entire Conversation
        if (conversationToDelete != null) {
            val conv = conversationToDelete!!
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { conversationToDelete = null }
            ) {
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(LinkoraSpacing.space16),
                    shape = RoundedCornerShape(24.dp),
                    cornerRadius = 24.dp,
                    elevation = LinkoraElevation.elevationFloating,
                    isElevated = true
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFFEF4444).copy(alpha = 0.15f), CircleShape)
                                .border(1.dp, Color(0xFFEF4444).copy(alpha = 0.4f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Delete,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                        Text(
                            text = "Delete Conversation?",
                            style = LinkoraTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

                        Text(
                            text = "Delete chat with \"${conv.name}\"? All messages and chat history will be permanently deleted.",
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textSecondary,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space20))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            androidx.compose.material3.OutlinedButton(
                                onClick = {
                                    onArchiveConversation?.invoke(conv.id, !conv.isArchived)
                                    conversationToDelete = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, LinkoraColors.Cyan.copy(alpha = 0.5f))
                            ) {
                                Icon(
                                    imageVector = if (conv.isArchived) Icons.Rounded.Unarchive else Icons.Rounded.Archive,
                                    contentDescription = null,
                                    tint = LinkoraColors.Cyan,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (conv.isArchived) "Unarchive" else "Archive",
                                    color = LinkoraColors.Cyan,
                                    fontSize = 13.sp
                                )
                            }

                            androidx.compose.material3.Button(
                                onClick = {
                                    onDeleteConversation?.invoke(conv.id)
                                    conversationToDelete = null
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444)
                                )
                            ) {
                                Text("Delete", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button: Vibrant Mint/Emerald Gradient with Pencil Icon
        MintFloatingActionButton(
            onClick = onNewChatClick,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = LinkoraSpacing.space20, bottom = 96.dp)
        )
    }
}

/**
 * Top App Bar matching the mockup:
 * "Linkora" title on left with SMS arrival badge, circular glass buttons (Camera, Search, More, Avatar) on right.
 */
@Composable
private fun TopGlassHeader(
    onScanQrClick: () -> Unit,
    onSearchToggle: () -> Unit,
    onShowMyQr: () -> Unit,
    onAvatarClick: () -> Unit,
    unreadCount: Int = 0,
    onUnreadClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = LinkoraSpacing.space20,
                end = LinkoraSpacing.space16,
                top = 8.dp,
                bottom = 6.dp
            ),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        val glassColors = LocalGlassColors.current

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = "Linkora",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = glassColors.textPrimary,
                letterSpacing = (-0.5).sp
            )

            if (unreadCount > 0) {
                // Animated glowing SMS arrival icon badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(LinkoraColors.Emerald.copy(alpha = 0.18f))
                        .border(1.dp, LinkoraColors.Emerald.copy(alpha = 0.50f), RoundedCornerShape(12.dp))
                        .clickable { onUnreadClick?.invoke() }
                        .padding(horizontal = 8.dp, vertical = 4.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Sms,
                            contentDescription = "New SMS arrived",
                            tint = LinkoraColors.Emerald,
                            modifier = Modifier.size(14.dp)
                        )
                        Text(
                            text = "$unreadCount new",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Bold,
                            color = LinkoraColors.Emerald
                        )
                    }
                }
            }
        }

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Camera Button
            GlassIconButton(
                iconRes = R.drawable.ic_camera,
                contentDescription = "Camera",
                onClick = onScanQrClick,
                buttonSize = 40.dp,
                iconSize = 20.dp
            )

            // Search Button
            GlassIconButton(
                iconRes = R.drawable.ic_search,
                contentDescription = "Search",
                onClick = onSearchToggle,
                buttonSize = 40.dp,
                iconSize = 20.dp
            )

            // More Options (3 vertical dots)
            GlassIconButton(
                iconRes = R.drawable.ic_more,
                contentDescription = "More",
                onClick = onShowMyQr,
                buttonSize = 40.dp,
                iconSize = 20.dp
            )

            // Self Profile Avatar with Online Status
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .clickable(onClick = onAvatarClick)
            ) {
                Image(
                    painter = painterResource(id = R.drawable.avatar_user),
                    contentDescription = "Profile",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .border(1.5.dp, Color.White.copy(alpha = 0.6f), CircleShape)
                )
                // Online Dot
                Box(
                    modifier = Modifier
                        .size(11.dp)
                        .align(Alignment.BottomEnd)
                        .clip(CircleShape)
                        .background(LinkoraColors.OnlineStatus, CircleShape)
                        .border(1.5.dp, Color.White, CircleShape)
                )
            }
        }
    }
}

/**
 * Filter Pills Row:
 * "All", "Unread 3", "Groups", "Favorites".
 * Active: Pure frosted white pill with dark text.
 * Inactive: Translucent dark glass pill with white text.
 */
@Composable
private fun FilterPillsRow(
    activeFilter: ChatFilter,
    unreadCount: Int,
    onFilterSelected: (ChatFilter) -> Unit
) {
    val glassColors = LocalGlassColors.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState())
            .padding(horizontal = LinkoraSpacing.space16, vertical = 6.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        ChatFilter.entries.forEach { filter ->
            val isSelected = filter == activeFilter

            val chipBgColor by animateColorAsState(
                targetValue = if (isSelected) {
                    glassColors.textPrimary
                } else {
                    glassColors.surfaceElevated
                },
                animationSpec = tween(durationMillis = 180),
                label = "chipBg"
            )

            val chipTextColor by animateColorAsState(
                targetValue = if (isSelected) {
                    glassColors.surface
                } else {
                    glassColors.textSecondary
                },
                animationSpec = tween(durationMillis = 180),
                label = "chipText"
            )

            val interactionSource = remember { MutableInteractionSource() }

            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(chipBgColor, CircleShape)
                    .border(
                        1.dp,
                        if (isSelected) glassColors.textPrimary.copy(alpha = 0.5f) else glassColors.border,
                        CircleShape
                    )
                    .clickable(
                        interactionSource = interactionSource,
                        indication = null,
                        onClick = { onFilterSelected(filter) }
                    )
                    .padding(horizontal = 16.dp, vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = filter.label,
                        color = chipTextColor,
                        fontSize = 14.sp,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
                        maxLines = 1,
                        softWrap = false
                    )

                    // Unread badge count if applicable
                    if (filter == ChatFilter.UNREAD && unreadCount > 0) {
                        Box(
                            modifier = Modifier
                                .clip(CircleShape)
                                .background(
                                    LinkoraColors.Emerald,
                                    CircleShape
                                )
                                .padding(horizontal = 7.dp, vertical = 1.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = unreadCount.toString(),
                                color = Color.White,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Stories Carousel matching the mockup:
 * - "Add Story" with dark circle and + icon
 * - "Your Story" with mountain image and + badge
 * - Contact stories with glowing gradient border ring & online dot
 */
@Composable
private fun StoriesCarousel(
    contacts: List<Contact>,
    onAddStoryClick: () -> Unit,
    onYourStoryClick: () -> Unit,
    onContactClick: (String) -> Unit
) {
    val glassColors = LocalGlassColors.current

    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentPadding = PaddingValues(horizontal = LinkoraSpacing.space16),
        horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. "Add Story" button
        item(key = "add_story") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onAddStoryClick)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.verticalGradient(
                                listOf(
                                    glassColors.surfaceElevated,
                                    glassColors.surface
                                )
                            ),
                            CircleShape
                        )
                        .border(
                            1.2.dp,
                            Brush.linearGradient(
                                listOf(
                                    glassColors.border,
                                    glassColors.border.copy(alpha = 0.25f)
                                )
                            ),
                            CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Add,
                        contentDescription = "Add Story",
                        tint = glassColors.iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Add Story",
                    color = glassColors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 2. "Your Story" item
        item(key = "your_story") {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable(onClick = onYourStoryClick)
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.avatar_story_mountain),
                        contentDescription = "Your Story",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .size(54.dp)
                            .clip(CircleShape)
                            .border(1.5.dp, glassColors.border, CircleShape)
                    )

                    // Small Emerald plus badge at bottom right
                    Box(
                        modifier = Modifier
                            .size(19.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(LinkoraColors.Emerald, CircleShape)
                            .border(1.5.dp, glassColors.surface, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Add,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Your Story",
                    color = glassColors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }

        // 3. Dynamic Contact Stories
        items(contacts, key = { it.identityId }) { contact ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onContactClick(contact.identityId) }
                    .padding(2.dp)
            ) {
                Box(
                    modifier = Modifier.size(56.dp),
                    contentAlignment = Alignment.Center
                ) {
                    // Glowing multicolor border ring
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .border(
                                2.dp,
                                Brush.sweepGradient(
                                    listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF06B6D4),
                                        Color(0xFF8B5CF6),
                                        Color(0xFF10B981)
                                    )
                                ),
                                CircleShape
                            )
                            .padding(2.5.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        GlassAvatar(
                            name = contact.username,
                            size = 48.dp,
                            isOnline = false
                        )
                    }

                    // Green online badge
                    Box(
                        modifier = Modifier
                            .size(14.dp)
                            .align(Alignment.BottomEnd)
                            .clip(CircleShape)
                            .background(LinkoraColors.OnlineStatus, CircleShape)
                            .border(2.dp, glassColors.surface, CircleShape)
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = contact.username.take(10),
                    color = glassColors.textPrimary,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

/**
 * Liquid Glass Conversation Card:
 * Rounded capsule (28dp corner radius), translucent gradient glass fill,
 * luminous border, top specular sheen, avatar, bold white name, message,
 * timestamp, and royal blue unread badge.
 */
@Composable
private fun LiquidGlassConversationCard(
    conversation: ConversationItem,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(28.dp),
        cornerRadius = 28.dp,
        elevation = LinkoraElevation.elevationCard,
        onClick = onClick,
        onLongClick = onLongClick
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Avatar with online status dot
            GlassAvatar(
                name = conversation.name,
                size = 54.dp,
                isOnline = conversation.isOnline
            )

            Spacer(modifier = Modifier.width(14.dp))

            // Contact Name and Last Message
            Column(
                modifier = Modifier.weight(1f)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = conversation.name,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )

                    // Timestamp
                    Text(
                        text = conversation.timestamp,
                        fontSize = 12.sp,
                        color = glassColors.textSecondary,
                        fontWeight = FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        if (conversation.isOutgoingLast) {
                            Icon(
                                imageVector = if (conversation.isRead) Icons.Rounded.DoneAll else Icons.Rounded.Check,
                                contentDescription = if (conversation.isRead) "Read" else "Sent",
                                tint = if (conversation.isRead) Color(0xFF10B981) else glassColors.iconTint.copy(alpha = 0.6f),
                                modifier = Modifier
                                    .size(16.dp)
                                    .padding(end = 4.dp)
                            )
                        }

                        Text(
                            text = conversation.lastMessage,
                            fontSize = 14.sp,
                            color = glassColors.textSecondary,
                            fontWeight = FontWeight.Normal,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    // Status Icons (Muted, Pinned, Unread count)
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        modifier = Modifier.padding(start = 8.dp)
                    ) {
                        if (conversation.isMuted) {
                            Icon(
                                imageVector = Icons.Rounded.NotificationsOff,
                                contentDescription = "Muted",
                                tint = glassColors.iconTint.copy(alpha = 0.5f),
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (conversation.isPinned) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = "Pinned",
                                tint = glassColors.iconTint,
                                modifier = Modifier.size(15.dp)
                            )
                        }

                        if (conversation.unreadCount > 0) {
                            Box(
                                modifier = Modifier
                                    .clip(CircleShape)
                                    .background(LinkoraColors.Emerald, CircleShape)
                                    .padding(horizontal = 8.dp, vertical = 2.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = conversation.unreadCount.toString(),
                                    color = Color.White,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Empty state card: Displays frosted glass card inviting the user to start a chat.
 */
@Composable
private fun EmptyChatsCard(
    searchQuery: String,
    onNewChatClick: () -> Unit,
    onScanQrClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
        shape = RoundedCornerShape(28.dp),
        cornerRadius = 28.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (searchQuery.isNotEmpty()) "No results for \"$searchQuery\"" else "No Conversations Yet",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = glassColors.textPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = if (searchQuery.isNotEmpty()) {
                    "Try searching for a different friend's name or keyword."
                } else {
                    "Start a conversation with a friend or scan their QR code to begin chatting privately."
                },
                fontSize = 14.sp,
                color = glassColors.textSecondary,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(LinkoraColors.Emerald, CircleShape)
                        .clickable(onClick = onNewChatClick)
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "New Chat",
                        color = Color.White,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(CircleShape)
                        .background(glassColors.surfaceElevated, CircleShape)
                        .border(1.dp, glassColors.border, CircleShape)
                        .clickable(onClick = onScanQrClick)
                        .padding(horizontal = 18.dp, vertical = 9.dp)
                ) {
                    Text(
                        text = "Scan QR Code",
                        color = glassColors.textPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Mint Floating Action Button:
 * Vibrant Emerald / Mint gradient circle with compose pencil icon and spring press physics.
 */
@Composable
private fun MintFloatingActionButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.88f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "fabScale"
    )

    Box(
        modifier = modifier
            .scale(scale)
            .size(60.dp)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = Color(0x6600E676),
                spotColor = Color(0x8800B0FF)
            )
            .clip(CircleShape)
            .background(
                Brush.linearGradient(
                    listOf(
                        Color(0xFF00E676), // Vibrant Mint Green
                        Color(0xFF00B0FF)  // Vibrant Cyan
                    )
                ),
                CircleShape
            )
            .border(
                1.5.dp,
                Color.White.copy(alpha = 0.5f),
                CircleShape
            )
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_compose),
            contentDescription = "New chat",
            tint = Color.White,
            modifier = Modifier.size(26.dp)
        )
    }
}
