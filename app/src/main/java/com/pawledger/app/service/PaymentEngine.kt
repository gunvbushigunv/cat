package com.pawledger.app.service

import java.util.concurrent.ConcurrentHashMap

/**
 * 解析结果。
 */
data class ParsedPayment(
    val packageName: String,
    val source: String,
    val title: String?,
    val content: String?,
    val amount: Double
)

/**
 * 支付宝 / 微信支付通知解析。
 */
object PaymentParser {

    const val ALIPAY = "com.eg.android.AlipayGphone"
    const val WECHAT = "com.tencent.mm"

    private val amountRegexes = listOf(
        Regex("¥\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("￥\\s*(\\d+(?:\\.\\d{1,2})?)"),
        Regex("(\\d+(?:\\.\\d{1,2})?)\\s*元"),
        Regex("\\$\\s*(\\d+(?:\\.\\d{1,2})?)")
    )

    private val paymentKeywords = listOf(
        "支付成功", "付款成功", "交易成功", "已付款", "扣款", "消费成功",
        "转账", "收款", "到账", "支出", "付款", "成功支付", "已支付", "消费", "收款成功"
    )

    fun parse(packageName: String, title: String?, content: String?): ParsedPayment? {
        if (packageName != ALIPAY && packageName != WECHAT) return null
        val text = listOfNotNull(title, content).joinToString(" ")
        val hasKeyword = paymentKeywords.any { text.contains(it) }
        if (!hasKeyword) return null
        val amount = extractAmount(text) ?: return null
        val source = if (packageName == ALIPAY) "支付宝" else "微信"
        return ParsedPayment(packageName, source, title, content, amount)
    }

    fun extractAmount(text: String): Double? {
        for (r in amountRegexes) {
            val m = r.find(text)
            if (m != null) {
                val v = m.groupValues[1].toDoubleOrNull()
                if (v != null && v > 0) return v
            }
        }
        return null
    }
}

/**
 * 去重引擎：同一笔支付在 5 秒窗口内只触发一次（复合 key）。
 */
object PaymentDetector {

    private const val WINDOW_MS = 5000L
    private val lastSeen = ConcurrentHashMap<String, Long>()

    fun process(packageName: String, title: String?, content: String?): ParsedPayment? {
        val parsed = PaymentParser.parse(packageName, title, content) ?: return null
        val key = "$packageName|${parsed.amount}|${title.orEmpty()}"
        val now = System.currentTimeMillis()
        val last = lastSeen[key]
        if (last != null && now - last < WINDOW_MS) return null
        lastSeen[key] = now
        return parsed
    }
}
