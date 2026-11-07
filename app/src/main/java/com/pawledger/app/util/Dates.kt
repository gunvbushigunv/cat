package com.pawledger.app.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

/**
 * 时间工具（minSdk 26，可直接使用 java.time）。
 */
object Dates {

    private fun zone(): ZoneId = ZoneId.systemDefault()

    private fun localDate(epoch: Long): LocalDate =
        Instant.ofEpochMilli(epoch).atZone(zone()).toLocalDate()

    /** "yyyy-MM" */
    fun monthKey(epoch: Long = System.currentTimeMillis()): String {
        val d = localDate(epoch)
        return "%04d-%02d".format(d.year, d.monthValue)
    }

    /** "yyyy-MM-dd" */
    fun dayKey(epoch: Long = System.currentTimeMillis()): String = localDate(epoch).toString()

    fun startOfDay(epoch: Long = System.currentTimeMillis()): Long =
        localDate(epoch).atStartOfDay(zone()).toInstant().toEpochMilli()

    fun startOfMonth(epoch: Long = System.currentTimeMillis()): Long =
        localDate(epoch).withDayOfMonth(1).atStartOfDay(zone()).toInstant().toEpochMilli()

    fun endOfMonth(epoch: Long = System.currentTimeMillis()): Long {
        val next = localDate(epoch).withDayOfMonth(1).plusMonths(1)
        return next.atStartOfDay(zone()).toInstant().toEpochMilli()
    }

    fun startOfWeek(epoch: Long = System.currentTimeMillis()): Long =
        localDate(epoch).with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
            .atStartOfDay(zone()).toInstant().toEpochMilli()

    fun endOfWeek(epoch: Long = System.currentTimeMillis()): Long {
        val end = localDate(epoch).with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1)
        return end.atStartOfDay(zone()).toInstant().toEpochMilli()
    }

    /** 本月剩余天数（含今天） */
    fun remainingDaysInMonth(epoch: Long = System.currentTimeMillis()): Int {
        val d = localDate(epoch)
        return d.lengthOfMonth() - d.dayOfMonth + 1
    }

    fun formatMoney(v: Double): String = "¥%.2f".format(v)
}
