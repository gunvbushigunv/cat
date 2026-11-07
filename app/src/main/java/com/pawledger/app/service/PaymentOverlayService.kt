package com.pawledger.app.service

import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.pawledger.app.PawLedgerApp
import com.pawledger.app.R
import com.pawledger.app.data.entity.Transaction
import com.pawledger.app.data.entity.TransactionType
import com.pawledger.app.domain.catalog.Catalog
import com.pawledger.app.ui.overlay.PaymentPopupCard
import com.pawledger.app.ui.theme.PawLedgerTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * 悬浮支付弹窗前台服务：在屏幕底部显示识别到的支付信息，供一键记账。
 */
class PaymentOverlayService : Service() {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var windowManager: WindowManager? = null
    private var overlayView: View? = null
    private var hideJob: Job? = null
    private lateinit var container: com.pawledger.app.di.AppContainer
    private var source: String = "支付"
    private var amount: Double = 0.0

    companion object {
        private const val NOTIF_ID = 1001
        private const val AUTO_HIDE_MS = 8000L

        fun show(context: Context, parsed: ParsedPayment) {
            if (!PermissionUtils.canDrawOverlays(context)) return
            val intent = Intent(context, PaymentOverlayService::class.java).apply {
                putExtra("amount", parsed.amount)
                putExtra("source", parsed.source)
                putExtra("title", parsed.title)
                putExtra("content", parsed.content)
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }

    override fun onCreate() {
        super.onCreate()
        container = (application as PawLedgerApp).container
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        Notifier.ensureChannels(this)
        val notif = NotificationCompat.Builder(this, Notifier.CHANNEL_GENERAL)
            .setSmallIcon(R.drawable.ic_stat_paw)
            .setContentTitle("萌宠记账")
            .setContentText("正在识别一笔支付…")
            .setOngoing(true)
            .build()
        ServiceCompat.startForeground(
            this, NOTIF_ID, notif,
            ServiceCompat.FOREGROUND_SERVICE_TYPE_SPECIAL_USE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val amt = intent?.getDoubleExtra("amount", 0.0) ?: 0.0
        if (amt <= 0) {
            stopSelf()
            return START_NOT_STICKY
        }
        amount = amt
        source = intent.getStringExtra("source") ?: "支付"
        showPopup()
        return START_NOT_STICKY
    }

    private fun showPopup() {
        if (overlayView != null) return
        scope.launch {
            val categories = container.appRepository.getCategories()
            val stats = container.appRepository.computeMonthStats()
            val categoryStats = container.appRepository.computeCategoryStats()
            val companion = container.gameRepository.getCompanion()
            val emoji = if (companion != null) {
                Catalog.skin(companion.currentSkin).emoji
            } else {
                "🐱"
            }
            addOverlay(amount, source, categories, stats, categoryStats, emoji)
        }
    }

    private fun addOverlay(
        amt: Double,
        src: String,
        categories: List<com.pawledger.app.data.entity.Category>,
        stats: com.pawledger.app.domain.MonthStats,
        categoryStats: List<com.pawledger.app.domain.CategoryStat>,
        emoji: String
    ) {
        if (overlayView != null) return
        val wm = windowManager ?: return
        val composeView = ComposeView(this).apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnDetachedFromWindow)
            setContent {
                PawLedgerTheme {
                    PaymentPopupCard(
                        amount = amt,
                        source = src,
                        categories = categories,
                        stats = stats,
                        categoryStats = categoryStats,
                        companionEmoji = emoji,
                        onSave = { categoryId -> onSave(categoryId) },
                        onDismiss = { onDismiss() }
                    )
                }
            }
        }
        val params = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.BOTTOM
            y = (16 * resources.displayMetrics.density).toInt()
        }
        try {
            wm.addView(composeView, params)
            overlayView = composeView
        } catch (e: Exception) {
            stopSelf()
            return
        }
        scheduleAutoHide()
    }

    private fun scheduleAutoHide() {
        hideJob = scope.launch {
            delay(AUTO_HIDE_MS)
            recordDraft()
        }
    }

    private fun onSave(categoryId: Long?) {
        hideJob?.cancel()
        scope.launch {
            val accountId = container.appRepository.getAccounts().firstOrNull()?.id ?: 0L
            container.appRepository.addTransaction(
                Transaction(
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    categoryId = categoryId,
                    accountId = accountId,
                    merchant = source,
                    note = null,
                    timestamp = System.currentTimeMillis()
                )
            )
            removeOverlay()
            stopSelf()
        }
    }

    private fun recordDraft() {
        scope.launch {
            val accountId = container.appRepository.getAccounts().firstOrNull()?.id ?: 0L
            container.appRepository.insertDraft(
                Transaction(
                    type = TransactionType.EXPENSE,
                    amount = amount,
                    categoryId = null,
                    accountId = accountId,
                    merchant = source,
                    note = null,
                    isDraft = true,
                    timestamp = System.currentTimeMillis()
                )
            )
            removeOverlay()
            stopSelf()
        }
    }

    private fun onDismiss() {
        hideJob?.cancel()
        scope.launch {
            removeOverlay()
            stopSelf()
        }
    }

    private fun removeOverlay() {
        val view = overlayView ?: return
        overlayView = null
        runCatching { windowManager?.removeView(view) }
    }

    override fun onDestroy() {
        removeOverlay()
        scope.cancel()
        super.onDestroy()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}
