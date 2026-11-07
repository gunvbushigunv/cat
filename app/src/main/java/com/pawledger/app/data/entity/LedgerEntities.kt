package com.pawledger.app.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 记账流水（含退款关联、转账、还款、待分类草稿）。
 */
@Entity(tableName = "transactions")
data class Transaction(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: TransactionType,
    val amount: Double,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    @ColumnInfo(name = "account_id") val accountId: Long,
    @ColumnInfo(name = "to_account_id") val toAccountId: Long? = null,
    val merchant: String? = null,
    val note: String? = null,
    /** 退款关联的原支出流水 id */
    @ColumnInfo(name = "related_id") val relatedId: Long? = null,
    /** 待分类草稿 */
    @ColumnInfo(name = "is_draft") val isDraft: Boolean = false,
    val timestamp: Long
)

/**
 * 分类（内置 + 自定义 + emoji 图标 + 商户关键词）。
 */
@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** emoji 图标 */
    val icon: String,
    /** ARGB 颜色值 */
    val color: Long,
    @ColumnInfo(name = "is_custom") val isCustom: Boolean = false,
    /** 商户关键词，逗号分隔 */
    val keywords: String? = null
)

/**
 * 账户（支付宝 / 微信 / 银行卡 / 现金等）。
 */
@Entity(tableName = "accounts")
data class Account(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    /** ALIPAY / WECHAT / BANK / CASH / OTHER */
    val type: String,
    val color: Long
)

/**
 * 月度预算，主键为 "yyyy-MM"。
 */
@Entity(tableName = "budgets")
data class Budget(
    @PrimaryKey val month: String,
    val total: Double
)

/**
 * 周期账单（房租 / 会员 / 话费等）。
 */
@Entity(tableName = "recurring_bills")
data class RecurringBill(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val amount: Double,
    @ColumnInfo(name = "category_id") val categoryId: Long? = null,
    /** MONTHLY / WEEKLY / YEARLY */
    val frequency: String = "MONTHLY",
    /** 每月（或每周）的到期日 */
    @ColumnInfo(name = "due_day") val dueDay: Int = 1,
    val enabled: Boolean = true,
    @ColumnInfo(name = "last_reminded") val lastReminded: Long = 0L
)
