package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 已识别的通知原文。
 */
@Entity(tableName = "notification_logs")
data class NotificationLog(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    @ColumnInfo(name = "package_name") val packageName: String,
    val title: String?,
    val content: String?,
    val amount: Double? = null,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    @ColumnInfo(name = "is_draft") val isDraft: Boolean = false,
    val timestamp: Long
)

/**
 * 商户 / 关键词 → 分类 映射规则（用户纠正后自动套用）。
 */
@Entity(tableName = "merchant_rules")
data class MerchantRule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val keyword: String,
    @ColumnInfo(name = "category_id") val categoryId: Long
)
