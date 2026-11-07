package com.pawledger.app.service

import android.app.Notification
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification

/**
 * 通知监听服务：监听支付宝 / 微信支付通知，识别后触发悬浮记账弹窗。
 */
class PaymentListenerService : NotificationListenerService() {

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        val notif = sbn?.notification ?: return
        val extras = notif.extras
        val title = extras.getCharSequence(Notification.EXTRA_TITLE)?.toString()
        val content = extras.getCharSequence(Notification.EXTRA_TEXT)?.toString()
        val pkg = sbn.packageName
        val parsed = PaymentDetector.process(pkg, title, content)
        if (parsed != null) {
            PaymentOverlayService.show(this, parsed)
        }
    }

    override fun onListenerConnected() {
        // 无需额外处理
    }

    override fun onListenerDisconnected() {
        // 无需额外处理
    }
}
