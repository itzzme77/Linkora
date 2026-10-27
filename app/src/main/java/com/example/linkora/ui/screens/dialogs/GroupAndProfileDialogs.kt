package com.example.linkora.ui.screens.dialogs

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Check
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.GroupAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.linkora.model.Contact
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors

/**
 * CreateGroupDialog: Create an encrypted mesh group chat by picking contacts.
 */
@Composable
fun CreateGroupDialog(
    availableContacts: List<Contact>,
    onCreateGroup: (groupName: String, selectedMembers: List<Contact>) -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    var groupName by remember { mutableStateOf("") }
    val selectedContactIds = remember { mutableStateOf(setOf<String>()) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space8),
            shape = RoundedCornerShape(28.dp),
            cornerRadius = 28.dp,
            isElevated = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.GroupAdd,
                            contentDescription = null,
                            tint = com.example.linkora.ui.theme.LinkoraColors.Emerald,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                        Text(
                            text = "New Group Chat",
                            style = LinkoraTypography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = glassColors.textPrimary
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

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                // Group Name Input
                OutlinedTextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name", color = glassColors.textSecondary) },
                    placeholder = { Text("e.g. Field Team Alpha", color = glassColors.textTertiary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.example.linkora.ui.theme.LinkoraColors.Emerald,
                        unfocusedBorderColor = glassColors.border,
                        focusedTextColor = glassColors.textPrimary,
                        unfocusedTextColor = glassColors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                Text(
                    text = "Select Members (${selectedContactIds.value.size} selected):",
                    style = LinkoraTypography.labelLarge,
                    color = glassColors.textSecondary,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

                // Contacts Selection List
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 220.dp),
                    verticalArrangement = Arrangement.spacedBy(LinkoraSpacing.space4)
                ) {
                    if (availableContacts.isEmpty()) {
                        item {
                            Text(
                                text = "No contacts saved yet. Add a friend first!",
                                style = LinkoraTypography.bodyMedium,
                                color = glassColors.textTertiary,
                                modifier = Modifier.padding(LinkoraSpacing.space12)
                            )
                        }
                    } else {
                        items(availableContacts, key = { it.identityId }) { contact ->
                            val isSelected = selectedContactIds.value.contains(contact.identityId)

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(LinkoraRadius.radius12))
                                    .clickable {
                                        val current = selectedContactIds.value.toMutableSet()
                                        if (isSelected) current.remove(contact.identityId)
                                        else current.add(contact.identityId)
                                        selectedContactIds.value = current
                                    }
                                    .padding(horizontal = LinkoraSpacing.space8, vertical = LinkoraSpacing.space4),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                GlassAvatar(name = contact.username, size = 38.dp)

                                Spacer(modifier = Modifier.width(LinkoraSpacing.space12))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = contact.username,
                                        style = LinkoraTypography.titleMedium,
                                        color = glassColors.textPrimary
                                    )
                                    Text(
                                        text = contact.identityId.take(16) + "...",
                                        style = LinkoraTypography.labelSmall,
                                        color = glassColors.textTertiary
                                    )
                                }

                                Checkbox(
                                    checked = isSelected,
                                    onCheckedChange = { checked ->
                                        val current = selectedContactIds.value.toMutableSet()
                                        if (checked) current.add(contact.identityId)
                                        else current.remove(contact.identityId)
                                        selectedContactIds.value = current
                                    },
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = com.example.linkora.ui.theme.LinkoraColors.Emerald,
                                        checkmarkColor = Color.White
                                    )
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(LinkoraSpacing.space20))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinkoraSpacing.space12)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = glassColors.textSecondary)
                    }

                    Button(
                        onClick = {
                            if (groupName.isNotBlank()) {
                                val members = availableContacts.filter {
                                    selectedContactIds.value.contains(it.identityId)
                                }
                                onCreateGroup(groupName.trim(), members)
                                onDismiss()
                            }
                        },
                        enabled = groupName.isNotBlank(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        com.example.linkora.ui.theme.LinkoraColors.Emerald,
                                        Color(0xFF059669)
                                    )
                                ),
                                CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Text("Create", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

/**
 * EditProfileDialog: Simple self-user creation & display name editing.
 */
@Composable
fun EditProfileDialog(
    currentName: String,
    onSaveName: (newName: String) -> Unit,
    onDismiss: () -> Unit
) {
    val glassColors = LocalGlassColors.current
    var nameInput by remember { mutableStateOf(currentName) }

    Dialog(onDismissRequest = onDismiss) {
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space8),
            shape = RoundedCornerShape(28.dp),
            cornerRadius = 28.dp,
            isElevated = true
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                Text(
                    text = "Edit Profile Name",
                    style = LinkoraTypography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = glassColors.textPrimary
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space8))

                Text(
                    text = "This display name will be announced to nearby mesh peers.",
                    style = LinkoraTypography.bodyMedium,
                    color = glassColors.textSecondary
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                OutlinedTextField(
                    value = nameInput,
                    onValueChange = { nameInput = it },
                    label = { Text("Display Name", color = glassColors.textSecondary) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = com.example.linkora.ui.theme.LinkoraColors.Emerald,
                        unfocusedBorderColor = glassColors.border,
                        focusedTextColor = glassColors.textPrimary,
                        unfocusedTextColor = glassColors.textPrimary
                    )
                )

                Spacer(modifier = Modifier.height(LinkoraSpacing.space20))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(LinkoraSpacing.space12)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = CircleShape,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel", color = glassColors.textSecondary)
                    }

                    Button(
                        onClick = {
                            if (nameInput.isNotBlank()) {
                                onSaveName(nameInput.trim())
                                onDismiss()
                            }
                        },
                        enabled = nameInput.isNotBlank(),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues(),
                        modifier = Modifier
                            .weight(1f)
                            .height(44.dp)
                            .clip(CircleShape)
                            .background(
                                androidx.compose.ui.graphics.Brush.linearGradient(
                                    listOf(
                                        com.example.linkora.ui.theme.LinkoraColors.Emerald,
                                        Color(0xFF059669)
                                    )
                                ),
                                CircleShape
                            )
                            .border(1.dp, Color.White.copy(alpha = 0.35f), CircleShape)
                    ) {
                        Text("Save", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}
