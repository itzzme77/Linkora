package com.example.linkora.ui.screens.conversation

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaPlayer
import android.net.Uri
import android.provider.OpenableColumns
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.OpenInNew
import androidx.compose.material.icons.automirrored.rounded.Reply
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AttachFile
import androidx.compose.material.icons.rounded.AudioFile
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.ContentCopy
import androidx.compose.material.icons.rounded.Delete
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.DoneAll
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.EmojiEmotions
import androidx.compose.material.icons.rounded.ErrorOutline
import androidx.compose.material.icons.rounded.GraphicEq
import androidx.compose.material.icons.rounded.Image
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.OpenInNew
import androidx.compose.material.icons.rounded.Pause
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.PushPin
import androidx.compose.material.icons.rounded.Schedule
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.StarOutline
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material.icons.rounded.VideoLibrary
import com.example.linkora.ui.screens.dialogs.ForwardMessageDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.R
import com.example.linkora.model.ChatMessage
import com.example.linkora.model.Contact
import com.example.linkora.model.DeliveryState
import com.example.linkora.model.GroupChat
import com.example.linkora.model.MessageType
import com.example.linkora.model.VerificationState
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.core.content.FileProvider
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.roundToInt

/**
 * ConversationScreen: Premium WhatsApp-inspired encrypted messaging experience for Linkora
 * featuring iOS Liquid Glass UI, swipe-to-reply, emoji reactions popups, live audio recording
 * waveforms, typing indicator, and theme-adaptive glass bubbles.
 */
@Composable
fun ConversationScreen(
    conversationId: String,
    contact: Contact?,
    group: GroupChat?,
    messages: List<ChatMessage>,
    onSendMessage: (text: String) -> Unit,
    onBackClick: () -> Unit,
    onSendMedia: ((messageType: MessageType, file: File, fileName: String, mimeType: String, caption: String) -> Unit)? = null,
    onDeleteMessage: ((messageId: String) -> Unit)? = null,
    onClearChat: (() -> Unit)? = null,
    onVoiceCallClick: (() -> Unit)? = null,
    onToggleStarMessage: ((messageId: String, isStarred: Boolean) -> Unit)? = null,
    onTogglePinMessage: ((messageId: String, isPinned: Boolean) -> Unit)? = null,
    onForwardMessage: ((message: ChatMessage, targetId: String, isGroup: Boolean) -> Unit)? = null,
    onReactToMessage: ((messageId: String, emoji: String?) -> Unit)? = null,
    onEditMessage: ((messageId: String, newContent: String) -> Unit)? = null,
    isPeerTyping: Boolean = false,
    allContacts: List<Contact> = emptyList(),
    allGroups: List<GroupChat> = emptyList(),
    isDirectlyConnected: Boolean = false,
    modifier: Modifier = Modifier
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val glassColors = LocalGlassColors.current
    val haptic = LocalHapticFeedback.current
    val coroutineScope = rememberCoroutineScope()

    var inputText by remember { mutableStateOf("") }
    var replyingToMessage by remember { mutableStateOf<ChatMessage?>(null) }
    var editingMessage by remember { mutableStateOf<ChatMessage?>(null) }
    val effectiveTyping = isPeerTyping
    var activeReactionMessageId by remember { mutableStateOf<String?>(null) }
    val messageReactions = remember { mutableStateMapOf<String, String>() }
    var isRecordingVoice by remember { mutableStateOf(false) }
    var recordingSeconds by remember { mutableIntStateOf(0) }
    var messageToDelete by remember { mutableStateOf<ChatMessage?>(null) }
    var messageToForward by remember { mutableStateOf<ChatMessage?>(null) }
    var showClearChatDialog by remember { mutableStateOf(false) }
    var showAttachmentMenu by remember { mutableStateOf(false) }
    var showEmojiStickerPicker by remember { mutableStateOf(false) }
    var activeDocumentViewer by remember { mutableStateOf<Triple<String, String, String?>?>(null) }
    var isScreenProtected by remember { mutableStateOf(false) }

    val activity = context as? android.app.Activity
    LaunchedEffect(isScreenProtected) {
        activity?.window?.let { win ->
            if (isScreenProtected) {
                win.setFlags(
                    android.view.WindowManager.LayoutParams.FLAG_SECURE,
                    android.view.WindowManager.LayoutParams.FLAG_SECURE
                )
                Toast.makeText(context, "🛡️ Anti-Screenshot Security Enabled", Toast.LENGTH_SHORT).show()
            } else {
                win.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
            }
        }
    }
    DisposableEffect(Unit) {
        onDispose {
            activity?.window?.clearFlags(android.view.WindowManager.LayoutParams.FLAG_SECURE)
        }
    }

    val pinnedMessages = remember(messages) { messages.filter { it.isPinned } }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processSelectedUri(context, uri, MessageType.IMAGE, inputText, onSendMedia)
            inputText = ""
        }
    }

    val videoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processSelectedUri(context, uri, MessageType.VIDEO, inputText, onSendMedia)
            inputText = ""
        }
    }

    val audioPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processSelectedUri(context, uri, MessageType.AUDIO, inputText, onSendMedia)
            inputText = ""
        }
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            processSelectedUri(context, uri, MessageType.DOCUMENT, inputText, onSendMedia)
            inputText = ""
        }
    }

    val listState = rememberLazyListState()
    val titleName = group?.name ?: contact?.username ?: conversationId
    val isGroupChat = group != null

    // Scroll to bottom on initial load and when new messages arrive
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    // Voice recording timer tick
    LaunchedEffect(isRecordingVoice) {
        if (isRecordingVoice) {
            recordingSeconds = 0
            while (isRecordingVoice) {
                delay(1000)
                recordingSeconds++
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .imePadding()
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null
            ) {
                // Tap outside dismisses active emoji reactions
                activeReactionMessageId = null
            }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. iOS Translucent Header Bar
            ConversationHeader(
                title = titleName,
                subtitle = when {
                    effectiveTyping -> "typing..."
                    isGroupChat -> "${group?.memberIds?.size ?: 0} members • Encrypted Group"
                    contact != null -> if (isDirectlyConnected) "Direct Peer • Verified E2EE" else "Multi-Hop Mesh Route • Verified E2EE"
                    else -> "Nearby Mesh Node"
                },
                isVerified = contact?.verificationState == VerificationState.VERIFIED,
                isTyping = effectiveTyping,
                isScreenProtected = isScreenProtected,
                onBackClick = onBackClick,
                onVoiceCallClick = if (!isGroupChat && contact != null) onVoiceCallClick else null,
                onToggleScreenProtection = { isScreenProtected = !isScreenProtected },
                onClearChatClick = if (messages.isNotEmpty()) {
                    { showClearChatDialog = true }
                } else null
            )

            // Pinned Messages Banner (Feature #16)
            if (pinnedMessages.isNotEmpty()) {
                val latestPinned = pinnedMessages.last()
                GlassCard(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = LinkoraSpacing.space16, vertical = 4.dp)
                        .clickable {
                            val targetIndex = messages.indexOfFirst { it.messageId == latestPinned.messageId }
                            if (targetIndex >= 0) {
                                coroutineScope.launch {
                                    listState.animateScrollToItem(targetIndex)
                                }
                            }
                        },
                    shape = RoundedCornerShape(14.dp),
                    cornerRadius = 14.dp
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(LinkoraColors.Cyan.copy(alpha = 0.18f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.PushPin,
                                contentDescription = "Pinned",
                                tint = LinkoraColors.Cyan,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Pinned Message",
                                style = LinkoraTypography.labelSmall,
                                color = LinkoraColors.Cyan,
                                fontWeight = FontWeight.Bold,
                                fontSize = 11.sp
                            )
                            Text(
                                text = if (latestPinned.content.isNotBlank()) latestPinned.content else "[${latestPinned.messageType.name}]",
                                style = LinkoraTypography.bodySmall,
                                color = glassColors.textPrimary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis,
                                fontSize = 12.sp
                            )
                        }
                        GlassIconButton(
                            icon = Icons.Rounded.Close,
                            contentDescription = "Unpin",
                            onClick = { onTogglePinMessage?.invoke(latestPinned.messageId, false) },
                            buttonSize = 28.dp,
                            iconSize = 14.dp
                        )
                    }
                }
            }

            // 2. Chat Timeline List
            LazyColumn(
                state = listState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentPadding = PaddingValues(
                    horizontal = LinkoraSpacing.space16,
                    vertical = LinkoraSpacing.space12
                ),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                if (messages.isEmpty()) {
                    item {
                        EmptyConversationNotice(titleName = titleName, isGroup = isGroupChat)
                    }
                } else {
                    items(messages, key = { it.messageId }) { msg ->
                        MessageBubbleItem(
                            message = msg,
                            reaction = msg.reaction ?: messageReactions[msg.messageId],
                            isReactionPickerOpen = activeReactionMessageId == msg.messageId,
                            onSwipeReply = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                replyingToMessage = msg
                            },
                            onLongPress = {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                activeReactionMessageId = if (activeReactionMessageId == msg.messageId) null else msg.messageId
                            },
                            onSelectReaction = { emoji ->
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                val current = msg.reaction ?: messageReactions[msg.messageId]
                                val newReaction = if (current == emoji) null else emoji
                                if (newReaction == null) {
                                    messageReactions.remove(msg.messageId)
                                } else {
                                    messageReactions[msg.messageId] = newReaction
                                }
                                onReactToMessage?.invoke(msg.messageId, newReaction)
                                activeReactionMessageId = null
                            },
                            onCopyMessage = {
                                val clipboard = context.getSystemService(android.content.Context.CLIPBOARD_SERVICE) as ClipboardManager
                                clipboard.setPrimaryClip(ClipData.newPlainText("Message", msg.content))
                                Toast.makeText(context, "Message copied to clipboard", Toast.LENGTH_SHORT).show()
                                activeReactionMessageId = null
                            },
                            onDeleteClick = {
                                activeReactionMessageId = null
                                messageToDelete = msg
                            },
                            onToggleStar = {
                                activeReactionMessageId = null
                                onToggleStarMessage?.invoke(msg.messageId, !msg.isStarred)
                            },
                            onTogglePin = {
                                activeReactionMessageId = null
                                onTogglePinMessage?.invoke(msg.messageId, !msg.isPinned)
                            },
                            onForward = {
                                activeReactionMessageId = null
                                messageToForward = msg
                            },
                            onEditClick = if (msg.isOutgoing && msg.messageType == MessageType.TEXT) {
                                {
                                    activeReactionMessageId = null
                                    editingMessage = msg
                                    inputText = msg.content
                                }
                            } else null,
                            onOpenDocument = { path, name, mime ->
                                activeDocumentViewer = Triple(path, name, mime)
                            },
                            onJumpToQuotedMessage = { quoteSnippet ->
                                val targetIndex = messages.indexOfFirst { it.content.contains(quoteSnippet) }
                                if (targetIndex >= 0) {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        listState.animateScrollToItem(targetIndex)
                                    }
                                }
                            }
                        )
                    }

                    // Real-Time Animated Liquid Glass Typing Indicator (Feature #19)
                    if (effectiveTyping) {
                        item(key = "typing_bubble") {
                            TypingIndicatorBubble(
                                peerName = contact?.username ?: group?.name ?: "Peer",
                                modifier = Modifier.padding(vertical = 4.dp)
                            )
                        }
                    }
                }
            }

            // 3. Swipe-to-Reply Quote Banner
            AnimatedVisibility(
                visible = replyingToMessage != null,
                enter = expandVertically(tween(220)) + fadeIn(tween(180)),
                exit = shrinkVertically(tween(180)) + fadeOut(tween(150))
            ) {
                replyingToMessage?.let { replyMsg ->
                    ReplyingBanner(
                        senderName = if (replyMsg.isOutgoing) "You" else (contact?.username ?: "Sender"),
                        quotedText = replyMsg.content,
                        onDismiss = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            replyingToMessage = null
                        }
                    )
                }
            }

            // 4. WhatsApp-style Composer / Voice Recording Bar
            if (isRecordingVoice) {
                VoiceRecordingBar(
                    recordingSeconds = recordingSeconds,
                    onCancel = {
                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        isRecordingVoice = false
                    },
                    onSend = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        val durationStr = String.format(Locale.getDefault(), "%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)
                        onSendMessage("🎙️ Voice message ($durationStr)")
                        isRecordingVoice = false
                        coroutineScope.launch {
                            if (messages.isNotEmpty()) {
                                listState.animateScrollToItem(messages.size - 1)
                            }
                        }
                    }
                )
            } else {
                // Attachment Options Sheet (Photos, Videos, Audio, Documents)
                AnimatedVisibility(
                    visible = showAttachmentMenu,
                    enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                ) {
                    AttachmentOptionsSheet(
                        onPickImage = {
                            showAttachmentMenu = false
                            imagePickerLauncher.launch("image/*")
                        },
                        onPickVideo = {
                            showAttachmentMenu = false
                            videoPickerLauncher.launch("video/*")
                        },
                        onPickAudio = {
                            showAttachmentMenu = false
                            audioPickerLauncher.launch("audio/*")
                        },
                        onPickDocument = {
                            showAttachmentMenu = false
                            documentPickerLauncher.launch("*/*")
                        },
                        onDismiss = { showAttachmentMenu = false }
                    )
                }

                // 3.5 Editing Message Banner (Feature #13)
                AnimatedVisibility(
                    visible = editingMessage != null,
                    enter = expandVertically(tween(220)) + fadeIn(tween(180)),
                    exit = shrinkVertically(tween(180)) + fadeOut(tween(150))
                ) {
                    editingMessage?.let { editMsg ->
                        EditingBanner(
                            originalText = editMsg.content,
                            onCancel = {
                                editingMessage = null
                                inputText = ""
                            }
                        )
                    }
                }

                MessageComposer(
                    text = inputText,
                    onTextChange = { inputText = it },
                    onAttachClick = {
                        showEmojiStickerPicker = false
                        showAttachmentMenu = !showAttachmentMenu
                    },
                    isEmojiPickerOpen = showEmojiStickerPicker,
                    onEmojiClick = {
                        showAttachmentMenu = false
                        showEmojiStickerPicker = !showEmojiStickerPicker
                    },
                    onStartVoiceRecord = {
                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                        isRecordingVoice = true
                    },
                    onSend = {
                        if (editingMessage != null) {
                            val newText = inputText.trim()
                            val msgId = editingMessage!!.messageId
                            editingMessage = null
                            inputText = ""
                            if (newText.isNotBlank()) {
                                onEditMessage?.invoke(msgId, newText)
                            }
                        } else if (inputText.isNotBlank()) {
                            val textToSend = if (replyingToMessage != null) {
                                val replyHeader = "Replying to: \"${replyingToMessage?.content?.take(36)}\"\n"
                                replyHeader + inputText.trim()
                            } else {
                                inputText.trim()
                            }
                            inputText = ""
                            replyingToMessage = null
                            onSendMessage(textToSend)
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        }
                    }
                )

                // Emoji, Sticker & GIF Picker Drawer (Feature #9 & #10)
                AnimatedVisibility(
                    visible = showEmojiStickerPicker,
                    enter = expandVertically(tween(250)) + fadeIn(tween(200)),
                    exit = shrinkVertically(tween(200)) + fadeOut(tween(150))
                ) {
                    EmojiStickerPickerSheet(
                        onEmojiSelected = { emoji ->
                            inputText += emoji
                        },
                        onStickerSelected = { stickerText ->
                            onSendMessage(stickerText)
                            showEmojiStickerPicker = false
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        onGifSelected = { gifText ->
                            onSendMessage(gifText)
                            showEmojiStickerPicker = false
                            coroutineScope.launch {
                                if (messages.isNotEmpty()) {
                                    listState.animateScrollToItem(messages.size - 1)
                                }
                            }
                        },
                        onBackspace = {
                            if (inputText.isNotEmpty()) {
                                inputText = inputText.dropLast(1)
                            }
                        },
                        onDismiss = { showEmojiStickerPicker = false }
                    )
                }
            }
        }

        // Confirmation Dialog for Deleting Single Message (SMS)
        if (messageToDelete != null) {
            val msg = messageToDelete!!
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { messageToDelete = null }
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
                            text = "Delete Message?",
                            style = LinkoraTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

                        Text(
                            text = "Are you sure you want to delete this message? It will be permanently removed from your chat history.",
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
                                onClick = { messageToDelete = null },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, glassColors.border)
                            ) {
                                Text("Cancel", color = glassColors.textPrimary)
                            }

                            androidx.compose.material3.Button(
                                onClick = {
                                    onDeleteMessage?.invoke(msg.messageId)
                                    messageToDelete = null
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "Message deleted", Toast.LENGTH_SHORT).show()
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

        // Confirmation Dialog for Clearing Entire Chat History
        if (showClearChatDialog) {
            androidx.compose.ui.window.Dialog(
                onDismissRequest = { showClearChatDialog = false }
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
                                imageVector = Icons.Rounded.DeleteOutline,
                                contentDescription = null,
                                tint = Color(0xFFEF4444),
                                modifier = Modifier.size(24.dp)
                            )
                        }

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                        Text(
                            text = "Clear Chat History?",
                            style = LinkoraTypography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.textPrimary
                        )

                        Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

                        Text(
                            text = "All messages in this conversation with $titleName will be permanently deleted.",
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
                                onClick = { showClearChatDialog = false },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                border = androidx.compose.foundation.BorderStroke(1.dp, glassColors.border)
                            ) {
                                Text("Cancel", color = glassColors.textPrimary)
                            }

                            androidx.compose.material3.Button(
                                onClick = {
                                    onClearChat?.invoke()
                                    showClearChatDialog = false
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    Toast.makeText(context, "Chat cleared", Toast.LENGTH_SHORT).show()
                                },
                                modifier = Modifier.weight(1f),
                                shape = RoundedCornerShape(14.dp),
                                colors = androidx.compose.material3.ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFFEF4444)
                                )
                            ) {
                                Text("Clear All", color = Color.White, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // Forward Message Dialog (Feature #12)
        if (messageToForward != null) {
            ForwardMessageDialog(
                message = messageToForward!!,
                contacts = allContacts,
                groups = allGroups,
                onDismiss = { messageToForward = null },
                onForward = { targetId, isGroup ->
                    val msg = messageToForward!!
                    messageToForward = null
                    onForwardMessage?.invoke(msg, targetId, isGroup)
                }
            )
        }

        // In-App Document & PDF Viewer Dialog (Feature #8)
        if (activeDocumentViewer != null) {
            val (docPath, docName, docMime) = activeDocumentViewer!!
            DocumentViewerDialog(
                filePath = docPath,
                fileName = docName,
                mimeType = docMime,
                onDismiss = { activeDocumentViewer = null }
            )
        }
    }
}

/**
 * Conversation Header with iOS back arrow, avatar, title, verified shield badge, and clear chat action.
 */
@Composable
private fun ConversationHeader(
    title: String,
    subtitle: String,
    isVerified: Boolean,
    isTyping: Boolean = false,
    isScreenProtected: Boolean = false,
    onBackClick: () -> Unit,
    onVoiceCallClick: (() -> Unit)? = null,
    onToggleScreenProtection: (() -> Unit)? = null,
    onClearChatClick: (() -> Unit)? = null
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(bottomStart = 28.dp, bottomEnd = 28.dp),
        cornerRadius = 28.dp,
        elevation = LinkoraElevation.elevationSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            GlassIconButton(
                icon = Icons.AutoMirrored.Rounded.ArrowBack,
                contentDescription = "Back",
                onClick = onBackClick,
                buttonSize = 40.dp,
                iconSize = 20.dp
            )

            Spacer(modifier = Modifier.width(LinkoraSpacing.space8))

            GlassAvatar(name = title, size = 44.dp)

            Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = LinkoraTypography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = glassColors.textPrimary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (isVerified) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Rounded.Verified,
                            contentDescription = "Verified Contact",
                            tint = LinkoraColors.Emerald,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = if (isTyping) "typing..." else subtitle,
                    style = LinkoraTypography.labelSmall,
                    color = if (isTyping) LinkoraColors.Cyan else glassColors.textSecondary,
                    fontWeight = if (isTyping) FontWeight.SemiBold else FontWeight.Normal,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            if (onToggleScreenProtection != null) {
                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                GlassIconButton(
                    icon = Icons.Rounded.Security,
                    contentDescription = if (isScreenProtected) "Anti-Screenshot Active" else "Enable Anti-Screenshot",
                    onClick = onToggleScreenProtection,
                    buttonSize = 40.dp,
                    iconSize = 20.dp,
                    iconTint = if (isScreenProtected) LinkoraColors.Cyan else glassColors.textSecondary
                )
            }



            if (onVoiceCallClick != null) {
                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                GlassIconButton(
                    icon = Icons.Rounded.Call,
                    contentDescription = "Voice Call",
                    onClick = onVoiceCallClick,
                    buttonSize = 40.dp,
                    iconSize = 20.dp,
                    iconTint = LinkoraColors.Emerald
                )
            }

            if (onClearChatClick != null) {
                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                GlassIconButton(
                    icon = Icons.Rounded.DeleteOutline,
                    contentDescription = "Clear Chat History",
                    onClick = onClearChatClick,
                    buttonSize = 40.dp,
                    iconSize = 20.dp,
                    iconTint = Color(0xFFEF4444)
                )
            }
        }
    }
}

/**
 * Message Bubble with Swipe-To-Reply and Long-Press Reaction Menu.
 */
@Composable
private fun MessageBubbleItem(
    message: ChatMessage,
    reaction: String?,
    isReactionPickerOpen: Boolean,
    onSwipeReply: () -> Unit,
    onLongPress: () -> Unit,
    onSelectReaction: (String) -> Unit,
    onCopyMessage: () -> Unit,
    onDeleteClick: () -> Unit,
    onToggleStar: () -> Unit,
    onTogglePin: () -> Unit,
    onForward: () -> Unit,
    onEditClick: (() -> Unit)? = null,
    onOpenDocument: ((filePath: String, fileName: String, mimeType: String?) -> Unit)? = null,
    onJumpToQuotedMessage: ((quotedText: String) -> Unit)? = null
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val glassColors = LocalGlassColors.current
    val isOutgoing = message.isOutgoing
    val coroutineScope = rememberCoroutineScope()

    // Swipe-to-reply horizontal offset
    val offsetX = remember { Animatable(0f) }
    var hasTriggeredHaptic by remember { mutableStateOf(false) }

    val timeString = remember(message.createdAt) {
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        sdf.format(Date(message.createdAt))
    }

    val bubbleShape = if (isOutgoing) {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 20.dp,
            bottomEnd = 4.dp
        )
    } else {
        RoundedCornerShape(
            topStart = 20.dp,
            topEnd = 20.dp,
            bottomStart = 4.dp,
            bottomEnd = 20.dp
        )
    }

    // Adaptive bubble background matching WhatsApp iOS Liquid Glass
    val bubbleBg = if (isOutgoing) {
        if (glassColors.isDark) {
            Brush.linearGradient(
                listOf(
                    Color(0xFF005C4B), // WhatsApp Dark Emerald
                    Color(0xFF025144)
                )
            )
        } else {
            Brush.linearGradient(
                listOf(
                    Color(0xFFD9FDD3), // WhatsApp Light Mint
                    Color(0xFFC7F9BE)
                )
            )
        }
    } else {
        Brush.linearGradient(
            listOf(
                glassColors.surfaceElevated,
                glassColors.surface
            )
        )
    }

    val textColor = if (isOutgoing) {
        if (glassColors.isDark) Color.White else Color(0xFF064E3B)
    } else {
        glassColors.textPrimary
    }

    val metaColor = if (isOutgoing) {
        if (glassColors.isDark) Color.White.copy(alpha = 0.75f) else Color(0xFF064E3B).copy(alpha = 0.70f)
    } else {
        glassColors.textSecondary
    }

    val bubbleBorderBrush = if (isOutgoing) {
        Brush.linearGradient(
            listOf(
                if (glassColors.isDark) Color.White.copy(alpha = 0.35f) else LinkoraColors.Emerald.copy(alpha = 0.35f),
                Color.Transparent
            )
        )
    } else {
        Brush.linearGradient(
            listOf(
                glassColors.border,
                glassColors.border.copy(alpha = 0.2f)
            )
        )
    }

    val density = LocalDensity.current
    val thresholdPx = with(density) { 40.dp.toPx() }
    val maxDragPx = with(density) { 68.dp.toPx() }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .pointerInput(message.messageId) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (offsetX.value >= thresholdPx) {
                            onSwipeReply()
                        }
                        hasTriggeredHaptic = false
                        coroutineScope.launch {
                            offsetX.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = Spring.DampingRatioMediumBouncy,
                                    stiffness = Spring.StiffnessMedium
                                )
                            )
                        }
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        // Allow dragging only to the right (positive direction)
                        val newOffset = (offsetX.value + dragAmount * 0.7f).coerceIn(0f, maxDragPx)
                        coroutineScope.launch { offsetX.snapTo(newOffset) }
                        if (newOffset >= thresholdPx && !hasTriggeredHaptic) {
                            hasTriggeredHaptic = true
                        }
                    }
                )
            }
    ) {
        // Swipe-to-reply curved arrow revealed behind the bubble
        if (offsetX.value > 6f) {
            val replyIconScale = (offsetX.value / thresholdPx).coerceIn(0.4f, 1f)
            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .padding(start = 10.dp)
                    .size(34.dp)
                    .scale(replyIconScale)
                    .clip(CircleShape)
                    .background(glassColors.surfaceElevated, CircleShape)
                    .border(1.dp, glassColors.border, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Reply,
                    contentDescription = "Reply",
                    tint = LinkoraColors.Emerald,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Message Bubble Container with animated swipe offset
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .offset { IntOffset(offsetX.value.roundToInt(), 0) },
            horizontalAlignment = if (isOutgoing) Alignment.End else Alignment.Start
        ) {
            // Floating Emoji Reaction and Context Actions Popup (WhatsApp iOS style)
            AnimatedVisibility(
                visible = isReactionPickerOpen,
                enter = scaleIn(spring(Spring.DampingRatioMediumBouncy)) + fadeIn(),
                exit = scaleOut() + fadeOut()
            ) {
                EmojiReactionPopup(
                    isStarred = message.isStarred,
                    isPinned = message.isPinned,
                    canEdit = onEditClick != null,
                    onSelectReaction = onSelectReaction,
                    onToggleStar = onToggleStar,
                    onTogglePin = onTogglePin,
                    onEdit = onEditClick,
                    onForward = onForward,
                    onReply = onSwipeReply,
                    onCopy = onCopyMessage,
                    onDelete = onDeleteClick,
                    modifier = Modifier.padding(bottom = 6.dp)
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth(0.82f)
                    .then(
                        if (isOutgoing) Modifier.wrapContentWidth(Alignment.End)
                        else Modifier.wrapContentWidth(Alignment.Start)
                    )
            ) {
                Box(
                    modifier = Modifier
                        .shadow(
                            elevation = 4.dp,
                            shape = bubbleShape,
                            ambientColor = if (isOutgoing) Color(0x3310B981) else Color(0x22000000),
                            spotColor = if (isOutgoing) Color(0x4410B981) else Color(0x22000000)
                        )
                        .clip(bubbleShape)
                        .background(bubbleBg)
                        .drawWithContent {
                            drawContent()
                            // Specular top light sheen
                            drawLine(
                                brush = Brush.horizontalGradient(
                                    listOf(
                                        Color.Transparent,
                                        Color.White.copy(alpha = if (glassColors.isDark) 0.50f else 0.85f),
                                        Color.Transparent
                                    )
                                ),
                                start = Offset(14.dp.toPx(), 0.5f),
                                end = Offset(size.width - 14.dp.toPx(), 0.5f),
                                strokeWidth = 1.dp.toPx()
                            )
                        }
                        .border(
                            width = 1.dp,
                            brush = bubbleBorderBrush,
                            shape = bubbleShape
                        )
                        .pointerInput(message.messageId) {
                            detectTapGestures(
                                onLongPress = { onLongPress() }
                            )
                        }
                        .padding(horizontal = 14.dp, vertical = 8.dp)
                ) {
                    Column {
                        when (message.messageType) {
                            MessageType.IMAGE -> {
                                ImageMessageBubble(
                                    filePath = message.mediaPath,
                                    content = message.content,
                                    textColor = textColor,
                                    onClick = {
                                        message.mediaPath?.let { path ->
                                            openMediaFile(context, path, message.mimeType ?: "image/*")
                                        }
                                    }
                                )
                            }
                            MessageType.VIDEO -> {
                                VideoMessageBubble(
                                    filePath = message.mediaPath,
                                    fileSize = message.mediaSize,
                                    content = message.content,
                                    textColor = textColor,
                                    onClick = {
                                        message.mediaPath?.let { path ->
                                            openMediaFile(context, path, message.mimeType ?: "video/*")
                                        }
                                    }
                                )
                            }
                            MessageType.AUDIO -> {
                                AudioMessageBubble(
                                    filePath = message.mediaPath,
                                    fileSize = message.mediaSize,
                                    content = message.content,
                                    textColor = textColor
                                )
                            }
                            MessageType.DOCUMENT -> {
                                DocumentMessageBubble(
                                    filePath = message.mediaPath,
                                    fileSize = message.mediaSize,
                                    fileName = message.content,
                                    mimeType = message.mimeType,
                                    textColor = textColor,
                                    onClick = {
                                        if (message.mediaPath != null && File(message.mediaPath).exists() && onOpenDocument != null) {
                                            onOpenDocument(message.mediaPath, message.content, message.mimeType)
                                        } else {
                                            message.mediaPath?.let { path ->
                                                openMediaFile(context, path, message.mimeType ?: "*/*")
                                            }
                                        }
                                    }
                                )
                            }
                            MessageType.TEXT -> {
                                if (message.content.startsWith("Replying to: \"") && message.content.contains("\"\n")) {
                                    val quoteEnd = message.content.indexOf("\"\n")
                                    val quotedSnippet = message.content.substring("Replying to: \"".length, quoteEnd)
                                    val replyBody = message.content.substring(quoteEnd + 2)

                                    Column {
                                        // Liquid Glass Quoted Message Preview Box (Feature #11)
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(
                                                    if (isOutgoing) Color.Black.copy(alpha = 0.20f)
                                                    else glassColors.surfaceElevated.copy(alpha = 0.70f)
                                                )
                                                .border(
                                                    1.dp,
                                                    LinkoraColors.Emerald.copy(alpha = 0.5f),
                                                    RoundedCornerShape(10.dp)
                                                )
                                                .clickable {
                                                    onJumpToQuotedMessage?.invoke(quotedSnippet)
                                                }
                                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Box(
                                                    modifier = Modifier
                                                        .width(3.5.dp)
                                                        .height(28.dp)
                                                        .clip(RoundedCornerShape(2.dp))
                                                        .background(LinkoraColors.Emerald)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Column {
                                                    Text(
                                                        text = "Replying to message",
                                                        fontSize = 11.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        color = LinkoraColors.Emerald
                                                    )
                                                    Text(
                                                        text = quotedSnippet,
                                                        fontSize = 12.sp,
                                                        color = textColor.copy(alpha = 0.85f),
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                            }
                                        }

                                        Spacer(modifier = Modifier.height(6.dp))

                                        Text(
                                            text = replyBody,
                                            style = LinkoraTypography.bodyLarge,
                                            color = textColor,
                                            fontSize = 15.sp,
                                            lineHeight = 21.sp
                                        )
                                    }
                                } else {
                                    Text(
                                        text = message.content,
                                        style = LinkoraTypography.bodyLarge,
                                        color = textColor,
                                        fontSize = 15.sp,
                                        lineHeight = 21.sp
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(3.dp))

                        Row(
                            modifier = Modifier.align(Alignment.End),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            if (message.hopCount > 0) {
                                val hopLabel = if (message.hopCount == 1) "1 hop" else "${message.hopCount} hops"
                                Text(
                                    text = hopLabel,
                                    style = LinkoraTypography.labelSmall,
                                    color = metaColor,
                                    fontSize = 10.sp
                                )
                            }

                            if (message.isStarred) {
                                Icon(
                                    imageVector = Icons.Rounded.Star,
                                    contentDescription = "Starred",
                                    tint = Color(0xFFFBBF24),
                                    modifier = Modifier.size(11.dp)
                                )
                            }

                            if (message.isPinned) {
                                Icon(
                                    imageVector = Icons.Rounded.PushPin,
                                    contentDescription = "Pinned",
                                    tint = LinkoraColors.Cyan,
                                    modifier = Modifier.size(11.dp)
                                )
                            }

                            if (message.isEdited) {
                                Text(
                                    text = "edited •",
                                    style = LinkoraTypography.labelSmall,
                                    color = metaColor.copy(alpha = 0.75f),
                                    fontSize = 9.sp,
                                    fontStyle = androidx.compose.ui.text.font.FontStyle.Italic
                                )
                            }

                            Text(
                                text = timeString,
                                style = LinkoraTypography.labelSmall,
                                color = metaColor,
                                fontSize = 10.sp
                            )

                            if (isOutgoing) {
                                when (message.deliveryState) {
                                    DeliveryState.QUEUED -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Schedule,
                                            contentDescription = "Queued",
                                            tint = metaColor,
                                            modifier = Modifier.size(12.dp)
                                        )
                                    }
                                    DeliveryState.RELAYED -> {
                                        Icon(
                                            imageVector = Icons.Rounded.Check,
                                            contentDescription = "Relayed",
                                            tint = metaColor.copy(alpha = 0.65f),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                    DeliveryState.DELIVERED -> {
                                        Icon(
                                            imageVector = Icons.Rounded.DoneAll,
                                            contentDescription = "Delivered",
                                            tint = metaColor.copy(alpha = 0.65f),
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    DeliveryState.READ -> {
                                        Icon(
                                            imageVector = Icons.Rounded.DoneAll,
                                            contentDescription = "Read",
                                            tint = if (glassColors.isDark) Color(0xFF38BDF8) else LinkoraColors.Emerald,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                    DeliveryState.FAILED,
                                    DeliveryState.EXPIRED -> {
                                        Icon(
                                            imageVector = Icons.Rounded.ErrorOutline,
                                            contentDescription = "Failed",
                                            tint = Color(0xFFEF4444),
                                            modifier = Modifier.size(13.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                // Attached Reaction Badge at corner (Feature #18)
                if (!reaction.isNullOrEmpty()) {
                    Box(
                        modifier = Modifier
                            .align(if (isOutgoing) Alignment.BottomStart else Alignment.BottomEnd)
                            .offset(y = 10.dp)
                            .shadow(2.dp, CircleShape)
                            .clip(CircleShape)
                            .background(glassColors.surface, CircleShape)
                            .border(1.dp, glassColors.border, CircleShape)
                            .clickable {
                                onSelectReaction(reaction)
                            }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = reaction,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}

/**
 * Floating iOS-style Emoji Reaction and Message Context Actions Bar.
 * Quick reactions: ❤️, 👍, 😂, 😮, 😢, 🙏
 * Message actions: Reply, Copy, Delete SMS
 */
@Composable
private fun EmojiReactionPopup(
    isStarred: Boolean,
    isPinned: Boolean,
    canEdit: Boolean = false,
    onSelectReaction: (String) -> Unit,
    onToggleStar: () -> Unit,
    onTogglePin: () -> Unit,
    onEdit: (() -> Unit)? = null,
    onForward: () -> Unit,
    onReply: () -> Unit,
    onCopy: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val reactions = listOf("❤️", "👍", "😂", "😮", "😢", "🙏")

    GlassCard(
        modifier = modifier,
        shape = RoundedCornerShape(20.dp),
        cornerRadius = 20.dp,
        elevation = 12.dp,
        isElevated = true
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            // 1. Emoji Reactions Row
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                reactions.forEach { emoji ->
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

                    Text(
                        text = emoji,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .scale(scale)
                            .clickable(
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = { onSelectReaction(emoji) }
                            )
                            .padding(horizontal = 4.dp, vertical = 2.dp)
                    )
                }
            }

            // Subtle divider
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(0.8.dp)
                    .background(glassColors.border.copy(alpha = 0.5f))
            )

            // 2. Action Buttons Row (Star, Pin, Edit, Forward, Reply, Copy, Delete)
            Row(
                modifier = Modifier.horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Star Action
                ContextActionButton(
                    icon = if (isStarred) Icons.Rounded.Star else Icons.Rounded.StarOutline,
                    label = if (isStarred) "Unstar" else "Star",
                    tint = if (isStarred) Color(0xFFFBBF24) else glassColors.textPrimary,
                    onClick = onToggleStar
                )

                // Pin Action
                ContextActionButton(
                    icon = Icons.Rounded.PushPin,
                    label = if (isPinned) "Unpin" else "Pin",
                    tint = if (isPinned) LinkoraColors.Cyan else glassColors.textPrimary,
                    onClick = onTogglePin
                )

                // Edit Action (Feature #13)
                if (canEdit && onEdit != null) {
                    ContextActionButton(
                        icon = Icons.Rounded.Edit,
                        label = "Edit",
                        tint = LinkoraColors.Cyan,
                        onClick = onEdit
                    )
                }

                // Forward Action
                ContextActionButton(
                    icon = Icons.AutoMirrored.Rounded.Forward,
                    label = "Forward",
                    tint = glassColors.textPrimary,
                    onClick = onForward
                )

                // Reply Action
                ContextActionButton(
                    icon = Icons.AutoMirrored.Rounded.Reply,
                    label = "Reply",
                    tint = glassColors.textPrimary,
                    onClick = onReply
                )

                // Copy Action
                ContextActionButton(
                    icon = Icons.Rounded.ContentCopy,
                    label = "Copy",
                    tint = glassColors.textPrimary,
                    onClick = onCopy
                )

                // Delete Action
                ContextActionButton(
                    icon = Icons.Rounded.Delete,
                    label = "Delete",
                    tint = Color(0xFFEF4444),
                    onClick = onDelete
                )
            }
        }
    }
}

@Composable
private fun ContextActionButton(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    tint: Color,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(10.dp))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(15.dp)
        )
        Text(
            text = label,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            color = tint
        )
    }
}

/**
 * Replying Quote Banner above the Composer bar.
 */
@Composable
private fun ReplyingBanner(
    senderName: String,
    quotedText: String,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 4.dp)
            .clip(RoundedCornerShape(18.dp))
            .background(glassColors.surfaceElevated, RoundedCornerShape(18.dp))
            .border(1.dp, glassColors.border, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Emerald Accent Bar
            Box(
                modifier = Modifier
                    .width(3.5.dp)
                    .height(34.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(LinkoraColors.Emerald)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Replying to $senderName",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LinkoraColors.Emerald
                )
                Text(
                    text = quotedText,
                    fontSize = 13.sp,
                    color = glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(26.dp)
                    .clip(CircleShape)
                    .background(glassColors.surface, CircleShape)
                    .clickable(onClick = onDismiss),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cancel reply",
                    tint = glassColors.iconTint,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Voice Recording Mode with animated pulsing red dot and real-time audio waveforms.
 */
@Composable
private fun VoiceRecordingBar(
    recordingSeconds: Int,
    onCancel: () -> Unit,
    onSend: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "voiceWaveform")

    // Pulsing recording dot
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "recordingDot"
    )

    // Animated waveform bars heights
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "waveOscillation"
    )

    val timeFormatted = remember(recordingSeconds) {
        String.format(Locale.getDefault(), "%02d:%02d", recordingSeconds / 60, recordingSeconds % 60)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 8.dp)
            .clip(CircleShape)
            .background(glassColors.surfaceElevated, CircleShape)
            .border(1.dp, LinkoraColors.Emerald.copy(alpha = 0.5f), CircleShape)
            .padding(horizontal = 14.dp, vertical = 7.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Discard / Delete Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFEF4444).copy(alpha = 0.18f), CircleShape)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Delete,
                    contentDescription = "Discard Recording",
                    tint = Color(0xFFEF4444),
                    modifier = Modifier.size(18.dp)
                )
            }

            // Blinking Red Recording Dot & Timer
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444).copy(alpha = dotAlpha), CircleShape)
                )

                Text(
                    text = timeFormatted,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary
                )
            }

            // Live oscillating audio waveform bars
            Row(
                horizontalArrangement = Arrangement.spacedBy(3.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                val barCount = 12
                for (i in 0 until barCount) {
                    val barHeight = (8 + 14 * kotlin.math.sin(wavePhase + i * 0.55).toFloat().coerceAtLeast(0f)).dp
                    Box(
                        modifier = Modifier
                            .width(3.dp)
                            .height(barHeight)
                            .clip(RoundedCornerShape(1.5.dp))
                            .background(LinkoraColors.Emerald)
                    )
                }
            }

            // Send Voice Note Button
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.linearGradient(
                            listOf(
                                Color(0xFF10B981),
                                Color(0xFF059669)
                            )
                        ),
                        CircleShape
                    )
                    .clickable(onClick = onSend),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.Send,
                    contentDescription = "Send Voice Message",
                    tint = Color.White,
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

/**
 * Standard Liquid Glass Message Composer Bar with text input, send button, and mic icon.
 */
@Composable
private fun MessageComposer(
    text: String,
    onTextChange: (String) -> Unit,
    onAttachClick: () -> Unit,
    isEmojiPickerOpen: Boolean = false,
    onEmojiClick: () -> Unit,
    onStartVoiceRecord: () -> Unit,
    onSend: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 8.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CircleShape)
                .background(glassColors.surfaceElevated, CircleShape)
                .drawWithContent {
                    drawContent()
                    drawLine(
                        brush = Brush.horizontalGradient(
                            listOf(
                                Color.Transparent,
                                Color.White.copy(alpha = if (glassColors.isDark) 0.60f else 0.90f),
                                Color.Transparent
                            )
                        ),
                        start = Offset(24.dp.toPx(), 0.5f),
                        end = Offset(size.width - 24.dp.toPx(), 0.5f),
                        strokeWidth = 1.dp.toPx()
                    )
                }
                .border(1.dp, glassColors.border, CircleShape)
                .padding(horizontal = 14.dp, vertical = 6.dp),
            contentAlignment = Alignment.CenterStart
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Paperclip Attachment Button
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(glassColors.surface, CircleShape)
                        .border(1.dp, glassColors.border, CircleShape)
                        .clickable(onClick = onAttachClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AttachFile,
                        contentDescription = "Attach media or document",
                        tint = LinkoraColors.Emerald,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(6.dp))

                // Emoji & Stickers Button (Feature #9 & #10)
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(
                            if (isEmojiPickerOpen) LinkoraColors.Emerald.copy(alpha = 0.22f)
                            else glassColors.surface,
                            CircleShape
                        )
                        .border(
                            1.dp,
                            if (isEmojiPickerOpen) LinkoraColors.Emerald.copy(alpha = 0.7f)
                            else glassColors.border,
                            CircleShape
                        )
                        .clickable(onClick = onEmojiClick),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.EmojiEmotions,
                        contentDescription = "Emoji and Stickers",
                        tint = if (isEmojiPickerOpen) LinkoraColors.Emerald else glassColors.iconTint,
                        modifier = Modifier.size(19.dp)
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Box(modifier = Modifier.weight(1f)) {
                    if (text.isEmpty()) {
                        Text(
                            text = "Send encrypted message...",
                            style = LinkoraTypography.bodyMedium,
                            color = glassColors.textSecondary,
                            fontSize = 14.sp
                        )
                    }

                    BasicTextField(
                        value = text,
                        onValueChange = onTextChange,
                        textStyle = LinkoraTypography.bodyMedium.copy(
                            color = glassColors.textPrimary,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(LinkoraColors.Emerald),
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))

                // If text is empty: Show Microphone for Voice Recording; If text entered: Show Send
                if (text.isBlank()) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(glassColors.surface, CircleShape)
                            .border(1.dp, glassColors.border, CircleShape)
                            .clickable(onClick = onStartVoiceRecord),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Mic,
                            contentDescription = "Record Voice Note",
                            tint = LinkoraColors.Emerald,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                } else {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        Color(0xFF10B981),
                                        Color(0xFF059669)
                                    )
                                ),
                                CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.5f), CircleShape)
                            .clickable(onClick = onSend),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Rounded.Send,
                            contentDescription = "Send",
                            tint = Color.White,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Notice card displayed when a chat has no messages yet.
 */
@Composable
private fun EmptyConversationNotice(titleName: String, isGroup: Boolean) {
    val glassColors = LocalGlassColors.current

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = LinkoraSpacing.space32),
        contentAlignment = Alignment.Center
    ) {
        GlassCard(
            modifier = Modifier.padding(horizontal = 16.dp),
            shape = RoundedCornerShape(26.dp),
            cornerRadius = 26.dp
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(20.dp)
            ) {
                Text(
                    text = if (isGroup) "Group: $titleName" else "End-to-End Encrypted Chat",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Messages are pairwise-encrypted with ECDH + AES-GCM and hopped securely across Linkora mesh nodes without internet.",
                    fontSize = 13.sp,
                    color = glassColors.textSecondary,
                    lineHeight = 18.sp,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center
                )
            }
        }
    }
}

/**
 * Bottom Sheet / popup displaying rich media attachment options: Photos, Videos, Audio, Documents.
 */
@Composable
private fun AttachmentOptionsSheet(
    onPickImage: () -> Unit,
    onPickVideo: () -> Unit,
    onPickAudio: () -> Unit,
    onPickDocument: () -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 6.dp),
        shape = RoundedCornerShape(22.dp),
        cornerRadius = 22.dp,
        elevation = LinkoraElevation.elevationFloating,
        isElevated = true
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Share Encrypted File",
                    style = LinkoraTypography.titleSmall,
                    color = glassColors.textPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(glassColors.surface, CircleShape)
                        .clickable(onClick = onDismiss),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = glassColors.iconTint,
                        modifier = Modifier.size(15.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                AttachmentOptionItem(
                    icon = Icons.Rounded.Image,
                    title = "Photo",
                    color = Color(0xFF10B981),
                    onClick = onPickImage
                )
                AttachmentOptionItem(
                    icon = Icons.Rounded.VideoLibrary,
                    title = "Video",
                    color = Color(0xFF8B5CF6),
                    onClick = onPickVideo
                )
                AttachmentOptionItem(
                    icon = Icons.Rounded.AudioFile,
                    title = "Audio",
                    color = Color(0xFFF59E0B),
                    onClick = onPickAudio
                )
                AttachmentOptionItem(
                    icon = Icons.Rounded.Description,
                    title = "Document",
                    color = Color(0xFF3B82F6),
                    onClick = onPickDocument
                )
            }
        }
    }
}

@Composable
private fun AttachmentOptionItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    color: Color,
    onClick: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(color.copy(alpha = 0.15f), CircleShape)
                .border(1.dp, color.copy(alpha = 0.40f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = title,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = title,
            style = LinkoraTypography.labelSmall,
            color = glassColors.textPrimary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
private fun ImageMessageBubble(
    filePath: String?,
    content: String,
    textColor: Color,
    onClick: () -> Unit
) {
    val bitmap = remember(filePath) {
        if (filePath != null && File(filePath).exists()) {
            try {
                BitmapFactory.decodeFile(filePath)?.asImageBitmap()
            } catch (e: Exception) {
                null
            }
        } else null
    }

    Column {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.Black.copy(alpha = 0.25f))
                .clickable(onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            if (bitmap != null) {
                androidx.compose.foundation.Image(
                    bitmap = bitmap,
                    contentDescription = "Image attachment",
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        imageVector = Icons.Rounded.Image,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.6f),
                        modifier = Modifier.size(36.dp)
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (filePath == null) "Transferring image..." else "Tap to view image",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 12.sp
                    )
                }
            }
        }

        if (content.isNotBlank() && !content.startsWith("file_") && !content.startsWith("Transferring")) {
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = content,
                color = textColor,
                fontSize = 14.sp
            )
        }
    }
}

@Composable
private fun VideoMessageBubble(
    filePath: String?,
    fileSize: Long,
    content: String,
    textColor: Color,
    onClick: () -> Unit
) {
    val formattedSize = remember(fileSize) {
        if (fileSize > 1024 * 1024) String.format(Locale.getDefault(), "%.1f MB", fileSize / (1024f * 1024f))
        else String.format(Locale.getDefault(), "%d KB", fileSize / 1024)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFF0F172A).copy(alpha = 0.45f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF8B5CF6), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.PlayArrow,
                    contentDescription = "Play Video",
                    tint = Color.White,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (content.isNotBlank()) content else "Encrypted Video",
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = "Video • $formattedSize",
                    fontSize = 12.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
        }
    }
}

@Composable
private fun AudioMessageBubble(
    filePath: String?,
    fileSize: Long,
    content: String,
    textColor: Color
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var isPlaying by remember { mutableStateOf(false) }
    var mediaPlayer by remember { mutableStateOf<MediaPlayer?>(null) }

    val infiniteTransition = rememberInfiniteTransition(label = "audioWaveformAnim")
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(1100, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "playbackWavePhase"
    )

    DisposableEffect(filePath) {
        onDispose {
            mediaPlayer?.release()
            mediaPlayer = null
        }
    }

    val togglePlay = {
        if (filePath != null && File(filePath).exists()) {
            if (isPlaying) {
                mediaPlayer?.pause()
                isPlaying = false
            } else {
                try {
                    if (mediaPlayer == null) {
                        mediaPlayer = MediaPlayer().apply {
                            setDataSource(filePath)
                            prepare()
                            setOnCompletionListener {
                                isPlaying = false
                            }
                        }
                    }
                    mediaPlayer?.start()
                    isPlaying = true
                } catch (e: Exception) {
                    Toast.makeText(context, "Error playing audio", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(context, "Audio file still transferring", Toast.LENGTH_SHORT).show()
        }
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.15f))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(LinkoraColors.Emerald, CircleShape)
                .clickable { togglePlay() },
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (isPlaying) Icons.Rounded.Pause else Icons.Rounded.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
            )
        }
        Spacer(modifier = Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = if (content.isNotBlank()) content else "Voice Note / Audio",
                color = textColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (i in 0 until 18) {
                    val h = if (isPlaying) {
                        (6 + 14 * kotlin.math.sin(wavePhase + i * 0.45).toFloat().coerceAtLeast(0.1f)).dp
                    } else {
                        (6 + (i * 7 % 14)).dp
                    }
                    Box(
                        modifier = Modifier
                            .width(2.5.dp)
                            .height(h)
                            .clip(RoundedCornerShape(1.dp))
                            .background(if (isPlaying) LinkoraColors.Emerald else textColor.copy(alpha = 0.4f))
                    )
                }
            }
        }
    }
}

@Composable
private fun DocumentMessageBubble(
    filePath: String?,
    fileSize: Long,
    fileName: String,
    mimeType: String?,
    textColor: Color,
    onClick: () -> Unit
) {
    val isPdf = mimeType?.contains("pdf", ignoreCase = true) == true || fileName.endsWith(".pdf", ignoreCase = true)
    val badgeColor = if (isPdf) Color(0xFFEF4444) else Color(0xFF3B82F6)
    val formattedSize = remember(fileSize) {
        if (fileSize > 1024 * 1024) String.format(Locale.getDefault(), "%.1f MB", fileSize / (1024f * 1024f))
        else String.format(Locale.getDefault(), "%d KB", fileSize / 1024)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(Color.Black.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.15f), RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(10.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(42.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Description,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = fileName,
                    color = textColor,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = if (isPdf) "PDF Document • $formattedSize" else "Document • $formattedSize",
                    fontSize = 11.sp,
                    color = textColor.copy(alpha = 0.7f)
                )
            }
            Icon(
                imageVector = Icons.AutoMirrored.Rounded.OpenInNew,
                contentDescription = "Open file",
                tint = LinkoraColors.Emerald,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

private fun openMediaFile(context: Context, filePath: String, mimeType: String?) {
    try {
        val file = File(filePath)
        if (!file.exists()) {
            Toast.makeText(context, "File is not on local disk", Toast.LENGTH_SHORT).show()
            return
        }
        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            file
        )
        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, mimeType ?: "*/*")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Open file with"))
    } catch (e: Exception) {
        Toast.makeText(context, "No app available to open this file", Toast.LENGTH_SHORT).show()
    }
}

private fun processSelectedUri(
    context: Context,
    uri: Uri,
    type: MessageType,
    caption: String,
    onSendMedia: ((MessageType, File, String, String, String) -> Unit)?
) {
    try {
        val resolver = context.contentResolver
        var displayName = "${type.name.lowercase()}_${System.currentTimeMillis()}"

        resolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst() && nameIdx != -1) {
                val name = cursor.getString(nameIdx)
                if (!name.isNullOrBlank()) displayName = name
            }
        }

        val mimeType = resolver.getType(uri) ?: when (type) {
            MessageType.IMAGE -> "image/jpeg"
            MessageType.VIDEO -> "video/mp4"
            MessageType.AUDIO -> "audio/mp4"
            MessageType.DOCUMENT -> "application/pdf"
            else -> "*/*"
        }

        val cacheDir = File(context.cacheDir, "attachments")
        cacheDir.mkdirs()
        val cacheFile = File(cacheDir, displayName)
        resolver.openInputStream(uri)?.use { input ->
            cacheFile.outputStream().use { output ->
                input.copyTo(output)
            }
        }

        onSendMedia?.invoke(type, cacheFile, displayName, mimeType, caption)
    } catch (e: Exception) {
        Toast.makeText(context, "Error selecting file: ${e.message}", Toast.LENGTH_SHORT).show()
    }
}

/**
 * iOS Liquid Glass Banner displayed when editing an existing sent message (Feature #13).
 */
@Composable
private fun EditingBanner(
    originalText: String,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 4.dp),
        shape = RoundedCornerShape(16.dp),
        cornerRadius = 16.dp,
        elevation = LinkoraElevation.elevationSubtle
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Rounded.Edit,
                contentDescription = null,
                tint = LinkoraColors.Cyan,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Editing message",
                    style = LinkoraTypography.labelSmall,
                    color = LinkoraColors.Cyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = originalText,
                    style = LinkoraTypography.bodySmall,
                    color = glassColors.textSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(glassColors.surface)
                    .clickable(onClick = onCancel),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Rounded.Close,
                    contentDescription = "Cancel edit",
                    tint = glassColors.textSecondary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

/**
 * Animated Liquid Glass Typing Indicator Bubble with 3 pulsing neon dots (Feature #19).
 */
@Composable
private fun TypingIndicatorBubble(
    peerName: String,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "dots")

    val dot1Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot1"
    )
    val dot2Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 140, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot2"
    )
    val dot3Offset by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -5f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, delayMillis = 280, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot3"
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(glassColors.surface)
            .border(1.dp, glassColors.border, RoundedCornerShape(18.dp))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        Text(
            text = "$peerName is typing",
            style = LinkoraTypography.labelSmall,
            color = glassColors.textSecondary,
            fontSize = 11.sp
        )
        Spacer(modifier = Modifier.width(2.dp))
        Box(
            modifier = Modifier
                .offset(y = dot1Offset.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(LinkoraColors.Cyan)
        )
        Box(
            modifier = Modifier
                .offset(y = dot2Offset.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(LinkoraColors.Cyan)
        )
        Box(
            modifier = Modifier
                .offset(y = dot3Offset.dp)
                .size(5.dp)
                .clip(CircleShape)
                .background(LinkoraColors.Cyan)
        )
    }
}
