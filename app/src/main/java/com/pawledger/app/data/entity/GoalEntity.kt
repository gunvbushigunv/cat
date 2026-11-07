package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 存钱目标（日 / 周 / 月 / 长期 / 自定义）。
 */
@Entity(tableName = "goals")
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    @ColumnInfo(name = "target_amount") val targetAmount: Double,
    val period: GoalPeriod,
    @ColumnInfo(name = "start_date") val startDate: Long,
    /** 自定义目标截止日期；长期目标为 null 表示进行中 */
    @ColumnInfo(name = "end_date") val endDate: Long? = null,
    @ColumnInfo(name = "is_completed") val isCompleted: Boolean = false,
    @ColumnInfo(name = "completed_at") val completedAt: Long? = null,
    /** 总目标达成后的 10%–20% 积分是否已发放 */
    @ColumnInfo(name = "bonus_awarded") val bonusAwarded: Boolean = false,
    /** 周期性目标最近一次发奖的周期标识（日/周/月），避免同周期重复发奖 */
    @ColumnInfo(name = "last_awarded_key") val lastAwardedKey: String = ""
)
