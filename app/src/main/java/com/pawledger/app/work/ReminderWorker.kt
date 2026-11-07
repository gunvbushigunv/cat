package com.pawledger.app.work

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.pawledger.app.PawLedgerApp
import com.pawledger.app.service.Notifier
import com.pawledger.app.util.Dates
import kotlinx.coroutines.flow.first
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * 周期账单 + 日程 提醒。
 */
class ReminderWorker(appContext: Context, params: WorkerParameters) : CoroutineWorker(appContext, params) {

    override suspend fun doWork(): Result {
        val container = (applicationContext as PawLedgerApp).container
        val today = LocalDate.now()

        // 周期账单
        if (container.preferences.reminderBill.first()) {
            val bills = container.appRepository.getEnabledRecurringBills()
            val startOfDay = Dates.startOfDay()
            for (b in bills) {
                if (b.dueDay == today.dayOfMonth && b.lastReminded < startOfDay) {
                    Notifier.post(
                        applicationContext, 2100 + b.id.toInt(),
                        "账单提醒", "「${b.name}」今天到期（¥%.2f）".format(b.amount)
                    )
                    container.appRepository.updateRecurringBill(b.copy(lastReminded = System.currentTimeMillis()))
                }
            }
        }

        // 日程
        val schedules = container.appRepository.getAllSchedules()
        val now = System.currentTimeMillis()
        for (s in schedules) {
            if (s.completed || s.reminded) continue
            val remindAt = s.datetime - s.remindBefore * 60_000L
            if (now >= remindAt) {
                Notifier.post(applicationContext, 2200 + s.id.toInt(), "日程提醒", s.title)
                if (s.repeatRule == "NONE") {
                    container.appRepository.updateSchedule(s.copy(reminded = true))
                } else {
                    container.appRepository.updateSchedule(
                        s.copy(datetime = nextOccurrence(s.datetime, s.repeatRule), reminded = false)
                    )
                }
            }
        }
        return Result.success()
    }

    private fun nextOccurrence(epoch: Long, rule: String): Long {
        val dt = Instant.ofEpochMilli(epoch).atZone(ZoneId.systemDefault())
        val next = when (rule) {
            "DAILY" -> dt.plusDays(1)
            "WEEKLY" -> dt.plusWeeks(1)
            "MONTHLY" -> dt.plusMonths(1)
            else -> dt.plusDays(1)
        }
        return next.toInstant().toEpochMilli()
    }
}
