package com.example.linkora.ui.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Forward
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.linkora.model.ChatMessage
import com.example.linkora.model.Contact
import com.example.linkora.model.GroupChat
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * ForwardMessageDialog: iOS Liquid Glass dialog allowing users to forward
 * an encrypted message or media chunk to any peer contact or group.
 */
@Composable
fun ForwardMessageDialog(
    message: ChatMessage,
    contacts: List<Contact>,
    groups: List<GroupChat>,
    onDismiss: () -> Unit,
    onForward: (recipientId: String, isGroup: Boolean) -> Unit
) {
    val glassColors = LocalGlassColors.current
    var searchQuery by remember { mutableStateOf("") }

    val filteredContacts = remember(searchQuery, contacts) {
        if (searchQuery.isBlank()) contacts
        else contacts.filter { it.username.contains(searchQuery, ignoreCase = true) }
    }

    val filteredGroups = remember(searchQuery, groups) {
        if (searchQuery.isBlank()) groups
        else groups.filter { it.name.contains(searchQuery, ignoreCase = true) }
    }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(LinkoraSpacing.space8),
            shape = RoundedCornerShape(LinkoraRadius.radius24),
            cornerRadius = LinkoraRadius.radius24
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(LinkoraSpacing.space16)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(LinkoraColors.Violet.copy(alpha = 0.2f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Rounded.Forward,
                                contentDescription = null,
                                tint = LinkoraColors.Cyan,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                        Text(
                            text = "Forward Message",
                            style = LinkoraTypography.titleMedium,
                            color = glassColors.textPrimary,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    GlassIconButton(
                        icon = Icons.Rounded.Close,
                        contentDescription = "Close",
                        onClick = onDismiss,
                        buttonSize = 32.dp,
                        iconSize = 16.dp
                    )
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                // Message Quote Preview
                GlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(LinkoraRadius.radius12),
                    cornerRadius = LinkoraRadius.radius12
                ) {
                    Column(modifier = Modifier.padding(LinkoraSpacing.space8)) {
                        Text(
                            text = "Forwarding:",
                            style = LinkoraTypography.labelSmall,
                            color = LinkoraColors.Cyan,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = if (message.content.isNotBlank()) message.content else "[${message.messageType.name}]",
                            style = LinkoraTypography.bodySmall,
                            color = glassColors.textPrimary,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                // Search Box
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search contacts or groups...", color = glassColors.textTertiary, fontSize = 13.sp) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(LinkoraRadius.radius16),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = LinkoraColors.Emerald,
                        unfocusedBorderColor = glassColors.border,
                        focusedTextColor = glassColors.textPrimary,
                        unfocusedTextColor = glassColors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space12))

                // Recipient List
                LazyColumn(
                    modifier = Modifier.height(240.dp),
                    verticalArrangement = Arrangement.spacedBy(LinkoraSpacing.space4)
                ) {
                    if (filteredGroups.isNotEmpty()) {
                        item {
                            Text(
                                text = "GROUPS",
                                style = LinkoraTypography.labelSmall,
                                color = glassColors.textSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = LinkoraSpacing.space4)
                            )
                        }
                        items(filteredGroups, key = { "group_${it.groupId}" }) { group ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(LinkoraRadius.radius12))
                                    .clickable { onForward(group.groupId, true) }
                                    .padding(vertical = LinkoraSpacing.space8, horizontal = LinkoraSpacing.space4),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(LinkoraColors.Emerald.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Groups,
                                        contentDescription = null,
                                        tint = LinkoraColors.EmeraldLight,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = group.name,
                                        style = LinkoraTypography.bodyMedium,
                                        color = glassColors.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = "${group.memberIds.size} members",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Send",
                                    tint = LinkoraColors.Cyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }

                    if (filteredContacts.isNotEmpty()) {
                        item {
                            Text(
                                text = "CONTACTS",
                                style = LinkoraTypography.labelSmall,
                                color = glassColors.textSecondary,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(vertical = LinkoraSpacing.space4)
                            )
                        }
                        items(filteredContacts, key = { "contact_${it.identityId}" }) { contact ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(LinkoraRadius.radius12))
                                    .clickable { onForward(contact.identityId, false) }
                                    .padding(vertical = LinkoraSpacing.space8, horizontal = LinkoraSpacing.space4),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(LinkoraColors.Violet.copy(alpha = 0.2f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Rounded.Person,
                                        contentDescription = null,
                                        tint = LinkoraColors.Cyan,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.username,
                                        style = LinkoraTypography.bodyMedium,
                                        color = glassColors.textPrimary,
                                        fontWeight = FontWeight.Medium
                                    )
                                    Text(
                                        text = contact.identityId.take(16) + "...",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textSecondary,
                                        fontSize = 11.sp
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.Send,
                                    contentDescription = "Send",
                                    tint = LinkoraColors.Cyan,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
