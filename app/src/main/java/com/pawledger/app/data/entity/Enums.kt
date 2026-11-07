package com.pawledger.app.data.entity

/**
 * 交易类型：支出 / 收入 / 退款 / 转账 / 还款 / 存钱。
 */
enum class TransactionType { EXPENSE, INCOME, REFUND, TRANSFER, REPAY, SAVE }

/**
 * 存钱目标周期。
 */
enum class GoalPeriod { DAILY, WEEKLY, MONTHLY, LONG_TERM, CUSTOM }

/**
 * 伙伴物种。
 */
enum class PetSpecies { CAT, DOG, FOX }

/**
 * 伙伴心情。
 */
enum class PetMood { HAPPY, NORMAL, HUNGRY, SAD }
