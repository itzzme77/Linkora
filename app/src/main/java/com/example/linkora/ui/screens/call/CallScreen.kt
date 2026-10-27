package com.example.linkora.ui.screens.call

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.VolumeDown
import androidx.compose.material.icons.automirrored.rounded.VolumeUp
import androidx.compose.material.icons.rounded.Call
import androidx.compose.material.icons.rounded.CallEnd
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Mic
import androidx.compose.material.icons.rounded.MicOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.linkora.call.ActiveCall
import com.example.linkora.call.CallState
import com.example.linkora.ui.components.GlassAvatar
import com.example.linkora.ui.components.GlassCard
import com.example.linkora.ui.theme.LinkoraColors
import com.example.linkora.ui.theme.LinkoraElevation
import com.example.linkora.ui.theme.LinkoraSpacing
import com.example.linkora.ui.theme.LinkoraTypography
import com.example.linkora.ui.theme.LocalGlassColors
import java.util.Locale
import kotlin.math.sin

/**
 * Premium WhatsApp/iOS-styled In-Call Screen for Linkora P2P Encrypted Offline Voice Calls.
 */
@Composable
fun CallScreen(
    call: ActiveCall,
    onAcceptCall: () -> Unit,
    onRejectCall: () -> Unit,
    onEndCall: () -> Unit,
    onToggleMute: () -> Unit,
    onToggleSpeaker: () -> Unit,
    modifier: Modifier = Modifier
) {
    val glassColors = LocalGlassColors.current
    val infiniteTransition = rememberInfiniteTransition(label = "pulseRing")

    // Ripple pulse animation for avatar
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleScale"
    )
    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.5f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rippleAlpha"
    )

    // Wave phase animation for live audio soundwave
    val wavePhase by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 6.28f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val durationText = String.format(
        Locale.getDefault(),
        "%02d:%02d",
        call.durationSeconds / 60,
        call.durationSeconds % 60
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    listOf(
                        Color(0xFF0F172A).copy(alpha = 0.96f),
                        Color(0xFF020617).copy(alpha = 0.98f)
                    )
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // 1. Header: Encryption status badge
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.10f), CircleShape)
                    .border(1.dp, Color.White.copy(alpha = 0.18f), CircleShape)
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Icon(
                    imageVector = Icons.Rounded.Lock,
                    contentDescription = "Encrypted",
                    tint = LinkoraColors.Emerald,
                    modifier = Modifier.size(13.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Offline P2P Encrypted Voice",
                    fontSize = 11.sp,
                    color = Color.White.copy(alpha = 0.85f),
                    fontWeight = FontWeight.Medium
                )
            }

            // 2. Center: Avatar with pulsing rings, Name, Call State & Live Waveform
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier.size(170.dp)
                ) {
                    // Outer pulsing ring when ringing or connected
                    if (call.state == CallState.OUTGOING_CALLING || call.state == CallState.INCOMING_RINGING || call.state == CallState.CONNECTED) {
                        Box(
                            modifier = Modifier
                                .size(130.dp)
                                .scale(rippleScale)
                                .clip(CircleShape)
                                .background(LinkoraColors.Emerald.copy(alpha = rippleAlpha), CircleShape)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(110.dp)
                            .shadow(16.dp, CircleShape, spotColor = LinkoraColors.Emerald)
                            .clip(CircleShape)
                            .border(3.dp, LinkoraColors.Emerald.copy(alpha = 0.8f), CircleShape)
                    ) {
                        GlassAvatar(name = call.peerName, size = 110.dp)
                    }
                }

                Spacer(modifier = Modifier.height(18.dp))

                Text(
                    text = call.peerName,
                    style = LinkoraTypography.headlineLarge,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 26.sp
                )

                Spacer(modifier = Modifier.height(6.dp))

                val stateText = when (call.state) {
                    CallState.OUTGOING_CALLING -> "Calling peer over mesh..."
                    CallState.INCOMING_RINGING -> "Incoming Voice Call..."
                    CallState.CONNECTED -> durationText
                    CallState.ENDED -> call.endReason ?: "Call Ended"
                    CallState.IDLE -> ""
                }

                Text(
                    text = stateText,
                    fontSize = if (call.state == CallState.CONNECTED) 17.sp else 14.sp,
                    color = if (call.state == CallState.CONNECTED) LinkoraColors.Emerald else Color.White.copy(alpha = 0.70f),
                    fontWeight = if (call.state == CallState.CONNECTED) FontWeight.Bold else FontWeight.Normal
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Real-time audio waveform animation
                if (call.state == CallState.CONNECTED) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .height(44.dp)
                            .padding(horizontal = 16.dp)
                    ) {
                        val barCount = 18
                        val baseAmp = call.audioAmplitude.coerceIn(0.15f, 1f)
                        for (i in 0 until barCount) {
                            val waveHeight = (8 + 28 * (sin(wavePhase + i * 0.45).toFloat().coerceAtLeast(0f) * baseAmp)).dp
                            Box(
                                modifier = Modifier
                                    .width(3.5.dp)
                                    .height(waveHeight)
                                    .clip(RoundedCornerShape(2.dp))
                                    .background(
                                        Brush.verticalGradient(
                                            listOf(
                                                Color(0xFF34D399),
                                                Color(0xFF059669)
                                            )
                                        )
                                    )
                            )
                        }
                    }
                }
            }

            // 3. Bottom Controls: Mute, Speaker, Accept / End Call
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (call.state == CallState.INCOMING_RINGING) {
                    // Incoming call: Decline (Red) and Accept (Green) buttons
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 32.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Decline Call
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .shadow(12.dp, CircleShape, spotColor = Color(0xFFEF4444))
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444), CircleShape)
                                    .clickable(onClick = onRejectCall),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CallEnd,
                                    contentDescription = "Decline Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Decline", color = Color.White, fontSize = 13.sp)
                        }

                        // Accept Call
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .shadow(12.dp, CircleShape, spotColor = Color(0xFF10B981))
                                    .clip(CircleShape)
                                    .background(Color(0xFF10B981), CircleShape)
                                    .clickable(onClick = onAcceptCall),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.Call,
                                    contentDescription = "Accept Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("Accept", color = Color.White, fontSize = 13.sp)
                        }
                    }
                } else {
                    // Active or Outgoing Call Controls: Mute, End, Speaker
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 24.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Mute Button
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (call.isMuted) Color.White.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable(onClick = onToggleMute),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (call.isMuted) Icons.Rounded.MicOff else Icons.Rounded.Mic,
                                    contentDescription = if (call.isMuted) "Unmute" else "Mute",
                                    tint = if (call.isMuted) Color(0xFFEF4444) else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (call.isMuted) "Muted" else "Mute",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }

                        // End Call Button (Big Red)
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(68.dp)
                                    .shadow(14.dp, CircleShape, spotColor = Color(0xFFEF4444))
                                    .clip(CircleShape)
                                    .background(Color(0xFFEF4444), CircleShape)
                                    .clickable(onClick = onEndCall),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Rounded.CallEnd,
                                    contentDescription = "End Call",
                                    tint = Color.White,
                                    modifier = Modifier.size(30.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "End",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }

                        // Speakerphone Toggle
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (call.isSpeakerOn) LinkoraColors.Emerald.copy(alpha = 0.35f) else Color.White.copy(alpha = 0.15f),
                                        CircleShape
                                    )
                                    .border(1.dp, Color.White.copy(alpha = 0.25f), CircleShape)
                                    .clickable(onClick = onToggleSpeaker),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = if (call.isSpeakerOn) Icons.AutoMirrored.Rounded.VolumeUp else Icons.AutoMirrored.Rounded.VolumeDown,
                                    contentDescription = "Speaker",
                                    tint = if (call.isSpeakerOn) LinkoraColors.Emerald else Color.White,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = if (call.isSpeakerOn) "Speaker" else "Earpiece",
                                color = Color.White.copy(alpha = 0.75f),
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Top notification banner for incoming calls when user is navigating other screens.
 */
@Composable
fun IncomingCallNotificationBanner(
    call: ActiveCall,
    onAccept: () -> Unit,
    onDecline: () -> Unit,
    modifier: Modifier = Modifier
) {
    GlassCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = LinkoraSpacing.space16, vertical = 8.dp),
        shape = RoundedCornerShape(22.dp),
        cornerRadius = 22.dp,
        elevation = LinkoraElevation.elevationFloating,
        isElevated = true
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                GlassAvatar(name = call.peerName, size = 44.dp)
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = call.peerName,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = "Incoming Encrypted Voice Call...",
                        fontSize = 12.sp,
                        color = LinkoraColors.Emerald
                    )
                }
            }

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                // Decline
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFEF4444), CircleShape)
                        .clickable(onClick = onDecline),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.CallEnd,
                        contentDescription = "Decline",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }

                // Accept
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF10B981), CircleShape)
                        .clickable(onClick = onAccept),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Call,
                        contentDescription = "Accept",
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}
