package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 旅行记录：endTime 为 null 表示「尚未收取」，returnAt 为预计归来的时间。
 */
@Entity(tableName = "travel_records")
data class TravelRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val species: PetSpecies,
    @ColumnInfo(name = "destination_key") val destinationKey: String,
    @ColumnInfo(name = "start_time") val startTime: Long,
    /** 预计归来时间 */
    @ColumnInfo(name = "return_at") val returnAt: Long,
    /** 为 null 表示尚未收取 */
    @ColumnInfo(name = "end_time") val endTime: Long? = null,
    @ColumnInfo(name = "reward_points") val rewardPoints: Int = 0,
    @ColumnInfo(name = "reward_fish") val rewardFish: Int = 0,
    val note: String? = null
)

/**
 * 明信片 / 照片，收进旅行相册。
 */
@Entity(tableName = "postcards")
data class Postcard(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val species: PetSpecies,
    @ColumnInfo(name = "destination_key") val destinationKey: String,
    val title: String,
    /** emoji 或图片引用 */
    val image: String,
    @ColumnInfo(name = "collected_at") val collectedAt: Long
)

/**
 * 旅行日记（小猫视角短句）。
 */
@Entity(tableName = "diary_entries")
data class DiaryEntry(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val species: PetSpecies,
    val content: String,
    @ColumnInfo(name = "created_at") val createdAt: Long
)
