package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 伙伴实例（已解锁物种 + 当前皮肤 + 等级 / 经验 / 心情）。
 */
@Entity(tableName = "pets")
data class Pet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val species: PetSpecies,
    val name: String,
    @ColumnInfo(name = "current_skin") val currentSkin: String,
    val level: Int = 1,
    val exp: Int = 0,
    val mood: PetMood = PetMood.NORMAL,
    val unlocked: Boolean = false,
    @ColumnInfo(name = "is_companion") val isCompanion: Boolean = false,
    @ColumnInfo(name = "is_traveling") val isTraveling: Boolean = false
)

/**
 * 已解锁的皮肤（skinKey 引用静态目录）。
 */
@Entity(tableName = "owned_skins")
data class OwnedSkin(
    @PrimaryKey @ColumnInfo(name = "skin_key") val skinKey: String
)

/**
 * 全局游戏状态（单行，id 固定为 1）。
 */
@Entity(tableName = "game_state")
data class GameState(
    @PrimaryKey val id: Int = 1,
    val points: Int = 300,
    @ColumnInfo(name = "fish_snacks") val fishSnacks: Int = 10,
    @ColumnInfo(name = "initial_granted") val initialGranted: Boolean = false,
    @ColumnInfo(name = "last_bookkeeping_day") val lastBookkeepingDay: String = "",
    @ColumnInfo(name = "bookkeeping_streak") val bookkeepingStreak: Int = 0,
    @ColumnInfo(name = "last_settle_day") val lastSettleDay: String = ""
)

/**
 * 已拥有的家具（placed 表示是否装点在小屋中）。
 */
@Entity(tableName = "owned_furniture")
data class OwnedFurniture(
    @PrimaryKey @ColumnInfo(name = "furniture_key") val furnitureKey: String,
    val placed: Boolean = false
)
