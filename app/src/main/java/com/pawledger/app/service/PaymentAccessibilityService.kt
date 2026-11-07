package com.pawledger.app.service

import android.accessibilityservice.AccessibilityService
import android.view.accessibility.AccessibilityEvent

/**
 * 无障碍服务：作为通知监听的补充手段（非必选阻塞项）。
 * 仅在支付宝 / 微信界面内容变化时做一次关键词 + 金额提取。
 */
class PaymentAccessibilityService : AccessibilityService() {

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        val pkg = event.packageName?.toString() ?: return
        if (pkg != PaymentParser.ALIPAY && pkg != PaymentParser.WECHAT) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED &&
            event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
        ) return
        val text = event.text?.joinToString(" ") { it.toString() } ?: return
        if (text.isBlank()) return
        val parsed = PaymentDetector.process(pkg, text, null)
        if (parsed != null) {
            PaymentOverlayService.show(this, parsed)
        }
    }

    override fun onInterrupt() {
        // 无需额外处理
    }
}
