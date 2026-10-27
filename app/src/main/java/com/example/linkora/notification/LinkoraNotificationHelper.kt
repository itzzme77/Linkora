package com.example.linkora.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.linkora.R
import com.example.linkora.model.ChatMessage
import com.example.linkora.ui.MainActivity

/**
 * Handles Android system notifications for newly received messages and SMS.
 */
object LinkoraNotificationHelper {

    const val CHANNEL_ID = "linkora_messages_channel"
    const val CHANNEL_NAME = "Linkora New Messages & SMS"
    const val EXTRA_CONVERSATION_ID = "extra_conversation_id"

    fun initNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
                description = "Notifies when a new SMS or encrypted mesh message is received"
                enableVibration(true)
                setShowBadge(true)
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    fun showNewMessageNotification(
        context: Context,
        senderName: String,
        message: ChatMessage
    ) {
        if (!NotificationPreferences.isMessageNotificationsEnabled(context)) {
            return
        }

        // On Android 13+, check POST_NOTIFICATIONS permission
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val hasPermission = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!hasPermission) return
        }

        initNotificationChannel(context)

        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_CONVERSATION_ID, message.conversationId)
        }

        val pendingIntentFlags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        } else {
            PendingIntent.FLAG_UPDATE_CURRENT
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            message.conversationId.hashCode(),
            intent,
            pendingIntentFlags
        )

        val isVibrate = NotificationPreferences.isVibrationSoundEnabled(context)

        val builder = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_nav_chats)
            .setContentTitle("💬 SMS: $senderName")
            .setContentText(message.content)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message.content))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_MESSAGE)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        if (isVibrate) {
            builder.setVibrate(longArrayOf(0, 250, 150, 250))
        }

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (message.conversationId.hashCode() and 0x7FFFFFFF)
        notificationManager.notify(notificationId, builder.build())
    }

    fun cancelNotification(context: Context, conversationId: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val notificationId = (conversationId.hashCode() and 0x7FFFFFFF)
        notificationManager.cancel(notificationId)
    }
}
