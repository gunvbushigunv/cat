package com.pawledger.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawledger.app.PawLedgerApp
import com.pawledger.app.service.Notifier
import kotlinx.coroutines.flow.first

/**
 * 每日结算：不超预算奖励、断记饿肚子、超支提醒。
 */
class DailyWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PawLedgerApp).container
        container.gameRepository.settleDaily()

        val stats = container.appRepository.computeMonthStats()
        if (stats.budget > 0 && stats.expense > stats.budget) {
            if (container.preferences.reminderOverspend.first()) {
                Notifier.post(
                    applicationContext, 2001,
                    "超支提醒", "这个月已经超出预算啦，一起省一点好不好～"
                )
            }
        }
        return Result.success()
    }
}
