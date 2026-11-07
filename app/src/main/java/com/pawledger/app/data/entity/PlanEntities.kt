package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 日程。
 */
@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    /** 触发时间（epoch millis） */
    val datetime: Long,
    /** NONE / DAILY / WEEKLY / MONTHLY */
    @ColumnInfo(name = "repeat_rule") val repeatRule: String = "NONE",
    /** 提前提醒分钟数 */
    @ColumnInfo(name = "remind_before") val remindBefore: Int = 0,
    val completed: Boolean = false,
    /** 本周期是否已提醒过 */
    val reminded: Boolean = false
)

/**
 * 备忘录（支持置顶 / 搜索 / 转日程）。
 */
@Entity(tableName = "memos")
data class Memo(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val content: String,
    val pinned: Boolean = false,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long
)
