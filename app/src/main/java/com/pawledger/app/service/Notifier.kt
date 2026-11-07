package com.pawledger.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.pawledger.app.R

/**
 * 通知工具：建通道 + 发通知。
 */
object Notifier {

    const val CHANNEL_GENERAL = "pawledger_general"
    const val CHANNEL_REMINDER = "pawledger_reminder"

    fun ensureChannels(context: Context) {
        val nm = context.getSystemService(NotificationManager::class.java)
        val general = NotificationChannel(CHANNEL_GENERAL, "通用提醒", NotificationManager.IMPORTANCE_DEFAULT)
        val reminder = NotificationChannel(CHANNEL_REMINDER, "账单与目标提醒", NotificationManager.IMPORTANCE_HIGH)
        nm.createNotificationChannel(general)
        nm.createNotificationChannel(reminder)
    }

    fun post(context: Context, id: Int, title: String, text: String, channel: String = CHANNEL_GENERAL) {
        if (!NotificationManagerCompat.from(context).areNotificationsEnabled()) return
        ensureChannels(context)
        val notif = NotificationCompat.Builder(context, channel)
            .setSmallIcon(R.drawable.ic_stat_paw)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(id, notif)
    }
}
