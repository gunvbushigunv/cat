package com.pawledger.app.work

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * 后台任务调度：每日结算 / 周期提醒 / 旅行检查。
 */
object WorkScheduler {

    fun scheduleAll(context: Context) {
        val wm = WorkManager.getInstance(context)

        val daily = PeriodicWorkRequestBuilder<DailyWorker>(1, TimeUnit.DAYS).build()
        wm.enqueueUniquePeriodicWork("daily_settle", ExistingPeriodicWorkPolicy.KEEP, daily)

        val reminder = PeriodicWorkRequestBuilder<ReminderWorker>(4, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork("reminders", ExistingPeriodicWorkPolicy.KEEP, reminder)

        val travel = PeriodicWorkRequestBuilder<TravelWorker>(1, TimeUnit.HOURS).build()
        wm.enqueueUniquePeriodicWork("travel_check", ExistingPeriodicWorkPolicy.KEEP, travel)
    }
}
