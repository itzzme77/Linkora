package com.example.linkora.ui.screens.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.CallMade
import androidx.compose.material.icons.automirrored.rounded.CallMissed
import androidx.compose.material.icons.automirrored.rounded.CallReceived
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.PhoneInTalk
import androidx.compose.material.icons.rounded.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.model.CallDirection
import com.example.linkora.model.CallLogEntry
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.components.GlassIconButton
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraRadius
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * CallsScreen: iOS Liquid Glass Call Center & Call Logs for Linkora (Feature #26).
 * Displays persistent offline voice call history (incoming, outgoing, missed),
 * duration, timestamps, and one-tap encrypted redialing.
 */
@Composable
fun CallsScreen(
    callLogs: List<CallLogEntry>,
    onCallClick: (CallLogEntry) -> Unit,
    onClearLogs: () -> Unit,
    onVoiceCallClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .padding(horizontal = LinkoraSpacing.space16)
    ) {
        // Top Header Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = LinkoraSpacing.space16),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "Calls",
                    style = LinkoraTypography.titleLarge,
                    color = glassColors.textPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 28.sp
                )
                Text(
                    text = "End-to-End Encrypted Offline Voice",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary
                )
            }

            Row(horizontalArrangement = Arrangement.spacedBy(LinkoraSpacing.space8)) {
                if (callLogs.isNotEmpty()) {
                    GlassIconButton(
                        icon = Icons.Rounded.DeleteOutline,
                        contentDescription = "Clear History",
                        onClick = onClearLogs,
                        buttonSize = 36.dp,
                        iconSize = 18.dp
                    )
                }
                GlassIconButton(
                    icon = Icons.Rounded.Call,
                    contentDescription = "New Call",
                    onClick = onVoiceCallClick,
                    buttonSize = 36.dp,
                    iconSize = 18.dp
                )
            }
        }

        // End-to-End Security Banner
        GlassCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = LinkoraSpacing.space12),
            shape = RoundedCornerShape(LinkoraRadius.radius12),
            cornerRadius = LinkoraRadius.radius12
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = LinkoraSpacing.space12, vertical = LinkoraSpacing.space8),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Rounded.Security,
                    contentDescription = null,
                    tint = LinkoraColors.Emerald,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(LinkoraSpacing.space8))
                Text(
                    text = "Voice calls are decentralized P2P & encrypted via ECDH P-256",
                    style = LinkoraTypography.labelSmall,
                    color = glassColors.textSecondary,
                    fontSize = 11.sp
                )
            }
        }

        // Call History List or Empty State
        if (callLogs.isEmpty()) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(LinkoraSpacing.space32)
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    listOf(
                                        LinkoraColors.Violet.copy(alpha = 0.25f),
                                        LinkoraColors.Cyan.copy(alpha = 0.12f)
                                    )
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PhoneInTalk,
                            contentDescription = null,
                            tint = LinkoraColors.Cyan,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(LinkoraSpacing.space16))

                    Text(
                        text = "No Recent Calls",
                        style = LinkoraTypography.titleMedium,
                        color = glassColors.textPrimary,
                        fontWeight = FontWeight.SemiBold
                    )

                    Spacer(modifier = Modifier.height(LinkoraSpacing.space4))

                    Text(
                        text = "Tap the call button above or visit a contact's profile to make your first peer-to-peer voice call.",
                        style = LinkoraTypography.bodySmall,
                        color = glassColors.textSecondary,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                        fontSize = 12.sp
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 90.dp),
                verticalArrangement = Arrangement.spacedBy(LinkoraSpacing.space8)
            ) {
                items(callLogs, key = { it.callId }) { log ->
                    CallLogItemCard(
                        log = log,
                        onRedial = { onCallClick(log) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CallLogItemCard(
    log: CallLogEntry,
    onRedial: () -> Unit
) {
    val glassColors = LocalGlassColors.current

    val timeFormatted = remember(log.timestamp) {
        val sdf = SimpleDateFormat("MMM d, h:mm a", Locale.getDefault())
        sdf.format(Date(log.timestamp))
    }

    val (directionIcon, directionColor, statusText) = when (log.direction) {
        CallDirection.INCOMING -> Triple(
            Icons.AutoMirrored.Rounded.CallReceived,
            LinkoraColors.EmeraldLight,
            if (log.durationSeconds > 0) formatDuration(log.durationSeconds) else "Answered"
        )
        CallDirection.OUTGOING -> Triple(
            Icons.AutoMirrored.Rounded.CallMade,
            LinkoraColors.Cyan,
            if (log.durationSeconds > 0) formatDuration(log.durationSeconds) else "Outgoing"
        )
        CallDirection.MISSED -> Triple(
            Icons.AutoMirrored.Rounded.CallMissed,
            Color(0xFFEF4444),
            log.endReason ?: "Missed"
        )
    }

    GlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onRedial),
        shape = RoundedCornerShape(LinkoraRadius.radius16),
        cornerRadius = LinkoraRadius.radius16
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = LinkoraSpacing.space16, vertical = LinkoraSpacing.space8),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Direction Badge Avatar
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(directionColor.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = directionIcon,
                    contentDescription = log.direction.name,
                    tint = directionColor,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(LinkoraSpacing.space16))

            // Caller Info & Duration
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = log.peerName,
                    style = LinkoraTypography.bodyLarge,
                    color = if (log.direction == CallDirection.MISSED) Color(0xFFEF4444) else glassColors.textPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = statusText,
                        style = LinkoraTypography.bodySmall,
                        color = glassColors.textSecondary,
                        fontSize = 12.sp
                    )
                    Text(
                        text = " • $timeFormatted",
                        style = LinkoraTypography.bodySmall,
                        color = glassColors.textSecondary.copy(alpha = 0.7f),
                        fontSize = 11.sp
                    )
                }
            }

            // Quick Redial Button
            GlassIconButton(
                icon = Icons.Rounded.Call,
                contentDescription = "Redial",
                onClick = onRedial,
                buttonSize = 36.dp,
                iconSize = 18.dp
            )
        }
    }
}

private fun formatDuration(seconds: Int): String {
    val m = seconds / 60
    val s = seconds % 60
    return if (m > 0) "${m}m ${s}s" else "${s}s"
}
