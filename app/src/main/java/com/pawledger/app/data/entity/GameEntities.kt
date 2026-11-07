package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 积分流水。
 */
@Entity(tableName = "points_ledger")
data class PointsLedger(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    /** 变化量，正为获得、负为消耗 */
    val delta: Int,
    val reason: String,
    val timestamp: Long
)

/**
 * 每日签到，主键为 "yyyy-MM-dd"。
 */
@Entity(tableName = "checkins")
data class CheckIn(
    @PrimaryKey val date: String,
    val streak: Int
)
