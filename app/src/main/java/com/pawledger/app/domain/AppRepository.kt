package com.pawledger.app.domain

import com.pawledger.app.data.AppDatabase
import com.pawledger.app.data.PreferencesManager
import com.pawledger.app.data.entity.Account
import com.pawledger.app.data.entity.Budget
import com.pawledger.app.data.entity.Category
import com.pawledger.app.data.entity.Goal
import com.pawledger.app.data.entity.GoalPeriod
import com.pawledger.app.data.entity.Memo
import com.pawledger.app.data.entity.MerchantRule
import com.pawledger.app.data.entity.NotificationLog
import com.pawledger.app.data.entity.RecurringBill
import com.pawledger.app.data.entity.Schedule
import com.pawledger.app.data.entity.Transaction
import com.pawledger.app.data.entity.TransactionType
import com.pawledger.app.service.Notifier
import com.pawledger.app.util.Dates
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import kotlin.random.Random

data class MonthStats(
    val budget: Double,
    val expense: Double,
    val income: Double,
    val remaining: Double,
    val remainingDays: Int,
    val safeDaily: Double
)

data class CategoryStat(
    val categoryId: Long,
    val name: String,
    val icon: String,
    val color: Long,
    val amount: Double
)

data class DailyStat(
    val day: Int,
    val amount: Double
)

data class GoalProgress(
    val goal: Goal,
    val progress: Double
)

/**
 * 记账 / 预算 / 目标 / 账单 / 日程 / 备忘录 业务逻辑。
 */
class AppRepository(
    private val db: AppDatabase,
    private val prefs: PreferencesManager,
    private val gameRepository: GameRepository,
    private val context: android.content.Context
) {

    private val ledgerDao = db.ledgerDao()
    private val goalDao = db.goalDao()
    private val noticeDao = db.noticeDao()
    private val planDao = db.planDao()

    // ================= 流水 =================
    fun observeTransactions(): Flow<List<Transaction>> = ledgerDao.observeAllTransactions()
    fun observeMonthTransactions(): Flow<List<Transaction>> =
        ledgerDao.observeTransactionsBetween(Dates.startOfMonth(), Dates.endOfMonth())
    fun observeDrafts(): Flow<List<Transaction>> = ledgerDao.observeDrafts()

    suspend fun addTransaction(t: Transaction): Long {
        val id = ledgerDao.insertTransaction(t)
        val saved = t.copy(id = id)
        if (saved.type in setOf(TransactionType.EXPENSE, TransactionType.INCOME, TransactionType.SAVE, TransactionType.REFUND)) {
            gameRepository.onBookkeepingRecorded()
        }
        if (saved.type == TransactionType.EXPENSE) {
            checkOverspend()
        }
        checkGoals()
        return id
    }

    suspend fun updateTransaction(t: Transaction) {
        ledgerDao.updateTransaction(t)
        checkGoals()
    }

    suspend fun deleteTransaction(t: Transaction) = ledgerDao.deleteTransaction(t)

    /** 插入待分类草稿（不触发记账奖励，补分类后再计）。 */
    suspend fun insertDraft(t: Transaction): Long = ledgerDao.insertTransaction(t)

    /** 补全草稿分类：标记已分类并触发奖励。 */
    suspend fun categorizeDraft(id: Long, categoryId: Long, merchant: String?) {
        val t = ledgerDao.getTransaction(id) ?: return
        ledgerDao.updateTransaction(t.copy(categoryId = categoryId, isDraft = false, merchant = merchant))
        gameRepository.onBookkeepingRecorded()
        checkOverspend()
        checkGoals()
    }

    suspend fun getAllTransactions(): List<Transaction> = ledgerDao.getAllTransactions()

    suspend fun getTransaction(id: Long): Transaction? = ledgerDao.getTransaction(id)

    private suspend fun checkOverspend() {
        val stats = computeMonthStats()
        if (stats.budget > 0 && stats.expense > stats.budget) {
            val companion = gameRepository.getCompanion()
            if (companion != null) gameRepository.dropMood(companion.species)
        }
    }

    // ================= 分类 =================
    fun observeCategories(): Flow<List<Category>> = ledgerDao.observeCategories()
    suspend fun getCategories(): List<Category> = ledgerDao.getAllCategories()
    suspend fun addCategory(c: Category): Long = ledgerDao.insertCategory(c)
    suspend fun updateCategory(c: Category) = ledgerDao.updateCategory(c)
    suspend fun deleteCategory(c: Category) = ledgerDao.deleteCategory(c)

    // ================= 账户 =================
    fun observeAccounts(): Flow<List<Account>> = ledgerDao.observeAccounts()
    suspend fun getAccounts(): List<Account> = ledgerDao.getAllAccounts()
    suspend fun addAccount(a: Account): Long = ledgerDao.insertAccount(a)
    suspend fun updateAccount(a: Account) = ledgerDao.updateAccount(a)
    suspend fun deleteAccount(a: Account) = ledgerDao.deleteAccount(a)

    // ================= 预算 =================
    suspend fun upsertBudget(b: Budget) = ledgerDao.upsertBudget(b)
    fun observeBudget(): Flow<Budget?> = ledgerDao.observeBudget(Dates.monthKey())

    // ================= 周期账单 =================
    fun observeRecurringBills(): Flow<List<RecurringBill>> = ledgerDao.observeRecurringBills()
    suspend fun addRecurringBill(r: RecurringBill): Long = ledgerDao.insertRecurringBill(r)
    suspend fun updateRecurringBill(r: RecurringBill) = ledgerDao.updateRecurringBill(r)
    suspend fun deleteRecurringBill(r: RecurringBill) = ledgerDao.deleteRecurringBill(r)
    suspend fun getEnabledRecurringBills(): List<RecurringBill> = ledgerDao.getEnabledRecurringBills()

    // ================= 目标 =================
    fun observeGoals(): Flow<List<Goal>> = goalDao.observeGoals()
    suspend fun addGoal(g: Goal): Long = goalDao.insertGoal(g)
    suspend fun updateGoal(g: Goal) = goalDao.updateGoal(g)
    suspend fun deleteGoal(g: Goal) = goalDao.deleteGoal(g)
    suspend fun getGoals(): List<Goal> = goalDao.getAllGoals()

    suspend fun computeGoalProgress(): List<GoalProgress> {
        val txs = ledgerDao.getAllTransactions()
        return goalDao.getAllGoals().map { g ->
            val (start, end) = periodRange(g)
            val net = netSavings(txs, start, end)
            GoalProgress(g, net)
        }
    }

    private fun periodRange(g: Goal): Pair<Long, Long> {
        val now = System.currentTimeMillis()
        return when (g.period) {
            GoalPeriod.DAILY -> Dates.startOfDay(now) to Dates.startOfDay(now) + 86_400_000L
            GoalPeriod.WEEKLY -> Dates.startOfWeek(now) to Dates.endOfWeek(now)
            GoalPeriod.MONTHLY -> Dates.startOfMonth(now) to Dates.endOfMonth(now)
            GoalPeriod.LONG_TERM -> g.startDate to now
            GoalPeriod.CUSTOM -> g.startDate to (g.endDate ?: now)
        }
    }

    private suspend fun checkGoals() {
        val txs = ledgerDao.getAllTransactions()
        val goals = goalDao.getActiveGoals()
        for (g in goals) {
            when (g.period) {
                GoalPeriod.DAILY -> {
                    val key = Dates.dayKey()
                    if (g.lastAwardedKey == key) continue
                    val net = netSavings(txs, Dates.startOfDay(), Dates.startOfDay() + 86_400_000L)
                    if (net >= g.targetAmount) {
                        gameRepository.addPoints(GameRepository.DAILY_GOAL_POINTS, "达成日目标·${g.name}")
                        notifyGoalAchieved(g.name)
                        goalDao.updateGoal(g.copy(lastAwardedKey = key))
                    }
                }
                GoalPeriod.WEEKLY -> {
                    val key = Dates.dayKey(Dates.startOfWeek())
                    if (g.lastAwardedKey == key) continue
                    val net = netSavings(txs, Dates.startOfWeek(), Dates.endOfWeek())
                    if (net >= g.targetAmount) {
                        gameRepository.addPoints(GameRepository.WEEKLY_GOAL_POINTS, "达成周目标·${g.name}")
                        goalDao.updateGoal(g.copy(lastAwardedKey = key))
                    }
                }
                GoalPeriod.MONTHLY -> {
                    val key = Dates.monthKey()
                    if (g.lastAwardedKey == key) continue
                    val net = netSavings(txs, Dates.startOfMonth(), Dates.endOfMonth())
                    if (net >= g.targetAmount) {
                        gameRepository.addPoints(GameRepository.MONTHLY_GOAL_POINTS, "达成月目标·${g.name}")
                        goalDao.updateGoal(g.copy(lastAwardedKey = key))
                    }
                }
                GoalPeriod.LONG_TERM, GoalPeriod.CUSTOM -> {
                    if (g.bonusAwarded) continue
                    val end = g.endDate ?: System.currentTimeMillis()
                    val net = netSavings(txs, g.startDate, end)
                    if (net >= g.targetAmount) {
                        // 总目标达成：按目标金额的 10%–20% 额外奖励
                        val pct = 10 + Random.nextInt(11)
                        val bonus = (g.targetAmount * pct / 100).toInt().coerceAtLeast(1)
                        gameRepository.addPoints(bonus, "达成总目标·${g.name}（${pct}% 加成）")
                        notifyGoalAchieved(g.name)
                        goalDao.updateGoal(
                            g.copy(isCompleted = true, completedAt = System.currentTimeMillis(), bonusAwarded = true)
                        )
                    }
                }
            }
        }
    }

    // ================= 通知与商户映射 =================
    fun observeNotificationLogs(): Flow<List<NotificationLog>> = noticeDao.observeNotificationLogs()
    suspend fun addNotificationLog(n: NotificationLog): Long = noticeDao.insertNotificationLog(n)
    suspend fun updateNotificationLog(n: NotificationLog) = noticeDao.updateNotificationLog(n)

    suspend fun rememberMerchantMapping(keyword: String, categoryId: Long) {
        if (keyword.isBlank()) return
        noticeDao.insertMerchantRule(MerchantRule(keyword = keyword, categoryId = categoryId))
    }

    /** 根据商户文本推荐分类：先查映射规则，再查分类关键词。 */
    suspend fun suggestCategory(merchantText: String): Category? {
        if (merchantText.isBlank()) return null
        noticeDao.findMerchantRule(merchantText)?.let { rule ->
            return ledgerDao.getCategory(rule.categoryId)
        }
        val categories = ledgerDao.getAllCategories()
        for (c in categories) {
            val keywords = c.keywords?.split(",")?.map { it.trim() }?.filter { it.isNotEmpty() }
            if (keywords != null && keywords.any { merchantText.contains(it) }) return c
        }
        return null
    }

    // ================= 日程 =================
    fun observeSchedules(): Flow<List<Schedule>> = planDao.observeSchedules()
    suspend fun addSchedule(s: Schedule): Long = planDao.insertSchedule(s)
    suspend fun updateSchedule(s: Schedule) = planDao.updateSchedule(s)
    suspend fun deleteSchedule(s: Schedule) = planDao.deleteSchedule(s)
    suspend fun getAllSchedules(): List<Schedule> = planDao.getAllSchedules()

    // ================= 备忘录 =================
    fun observeMemos(): Flow<List<Memo>> = planDao.observeMemos()
    fun searchMemos(q: String): Flow<List<Memo>> = planDao.searchMemos(q)
    suspend fun addMemo(m: Memo): Long = planDao.insertMemo(m)
    suspend fun updateMemo(m: Memo) = planDao.updateMemo(m)
    suspend fun deleteMemo(m: Memo) = planDao.deleteMemo(m)

    // ================= 统计 =================
    suspend fun computeMonthStats(): MonthStats {
        val txs = ledgerDao.getAllTransactions()
        val start = Dates.startOfMonth()
        val end = Dates.endOfMonth()
        val monthTxs = txs.filter { it.timestamp in start until end }
        val expense = monthTxs.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
            monthTxs.filter { it.type == TransactionType.REFUND }.sumOf { it.amount }
        val income = monthTxs.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val budget = ledgerDao.getBudget(Dates.monthKey())?.total ?: 0.0
        val remaining = budget - expense
        val days = Dates.remainingDaysInMonth()
        return MonthStats(
            budget = budget,
            expense = expense,
            income = income,
            remaining = remaining,
            remainingDays = days,
            safeDaily = if (days > 0) (remaining / days).coerceAtLeast(0.0) else 0.0
        )
    }

    suspend fun computeCategoryStats(): List<CategoryStat> {
        val txs = ledgerDao.getAllTransactions()
        val start = Dates.startOfMonth()
        val end = Dates.endOfMonth()
        val categories = ledgerDao.getAllCategories().associateBy { it.id }
        val grouped = txs
            .filter { it.timestamp in start until end && it.type == TransactionType.EXPENSE && it.categoryId != null }
            .groupBy { it.categoryId!! }
        return grouped.map { (cid, list) ->
            val c = categories[cid]
            CategoryStat(
                categoryId = cid,
                name = c?.name ?: "未分类",
                icon = c?.icon ?: "❓",
                color = c?.color ?: 0xFFB0A896,
                amount = list.sumOf { it.amount }
            )
        }.sortedByDescending { it.amount }
    }

    suspend fun computeDailyTrend(): List<DailyStat> {
        val txs = ledgerDao.getAllTransactions()
        val start = Dates.startOfMonth()
        val end = Dates.endOfMonth()
        val zone = ZoneId.systemDefault()
        val grouped = txs
            .filter { it.timestamp in start until end && it.type == TransactionType.EXPENSE }
            .groupBy { LocalDate.ofInstant(java.time.Instant.ofEpochMilli(it.timestamp), zone).dayOfMonth }
        val daysInMonth = LocalDate.ofInstant(java.time.Instant.ofEpochMilli(start), zone).lengthOfMonth()
        return (1..daysInMonth).map { day ->
            DailyStat(day = day, amount = grouped[day]?.sumOf { it.amount } ?: 0.0)
        }
    }

    private fun netSavings(txs: List<Transaction>, start: Long, end: Long): Double {
        val inRange = txs.filter { it.timestamp in start until end }
        val income = inRange.filter { it.type == TransactionType.INCOME }.sumOf { it.amount }
        val expense = inRange.filter { it.type == TransactionType.EXPENSE }.sumOf { it.amount } -
            inRange.filter { it.type == TransactionType.REFUND }.sumOf { it.amount }
        return income - expense
    }

    private suspend fun notifyGoalAchieved(name: String) {
        if (prefs.reminderGoal.first()) {
            Notifier.post(context, 3001, "目标达成 🎉", "恭喜达成目标「$name」！")
        }
    }

    // ================= 导出 / 备份 =================
    suspend fun exportJson(): String {
        val root = JSONObject()
        root.put("version", 1)
        root.put("exported_at", System.currentTimeMillis())

        val txArr = JSONArray()
        ledgerDao.getAllTransactions().forEach {
            txArr.put(JSONObject().apply {
                put("type", it.type.name)
                put("amount", it.amount)
                put("category_id", it.categoryId ?: JSONObject.NULL)
                put("account_id", it.accountId)
                put("to_account_id", it.toAccountId ?: JSONObject.NULL)
                put("merchant", it.merchant ?: JSONObject.NULL)
                put("note", it.note ?: JSONObject.NULL)
                put("related_id", it.relatedId ?: JSONObject.NULL)
                put("is_draft", it.isDraft)
                put("timestamp", it.timestamp)
            })
        }
        root.put("transactions", txArr)

        val catArr = JSONArray()
        ledgerDao.getAllCategories().forEach {
            catArr.put(JSONObject().apply {
                put("name", it.name); put("icon", it.icon); put("color", it.color)
                put("is_custom", it.isCustom); put("keywords", it.keywords ?: JSONObject.NULL)
            })
        }
        root.put("categories", catArr)

        val accArr = JSONArray()
        ledgerDao.getAllAccounts().forEach {
            accArr.put(JSONObject().apply {
                put("name", it.name); put("type", it.type); put("color", it.color)
            })
        }
        root.put("accounts", accArr)

        val goalArr = JSONArray()
        goalDao.getAllGoals().forEach {
            goalArr.put(JSONObject().apply {
                put("name", it.name); put("target_amount", it.targetAmount)
                put("period", it.period.name); put("start_date", it.startDate)
                put("end_date", it.endDate ?: JSONObject.NULL)
            })
        }
        root.put("goals", goalArr)

        val memoArr = JSONArray()
        planDao.getAllMemos().forEach {
            memoArr.put(JSONObject().apply {
                put("content", it.content); put("pinned", it.pinned)
                put("created_at", it.createdAt); put("updated_at", it.updatedAt)
            })
        }
        root.put("memos", memoArr)

        return root.toString(2)
    }

    suspend fun exportCsv(): String {
        val sb = StringBuilder("type,amount,category_id,account_id,merchant,note,is_draft,timestamp\n")
        ledgerDao.getAllTransactions().forEach {
            sb.append(it.type.name).append(',')
                .append(it.amount).append(',')
                .append(it.categoryId ?: "").append(',')
                .append(it.accountId).append(',')
                .append((it.merchant ?: "").replace(",", "，")).append(',')
                .append((it.note ?: "").replace(",", "，")).append(',')
                .append(it.isDraft).append(',')
                .append(it.timestamp).append('\n')
        }
        return sb.toString()
    }

    suspend fun importJson(json: String): Int {
        val root = JSONObject(json)
        var count = 0
        val txs = root.optJSONArray("transactions")
        if (txs != null) {
            for (i in 0 until txs.length()) {
                val o = txs.getJSONObject(i)
                val type = runCatching { TransactionType.valueOf(o.getString("type")) }.getOrDefault(TransactionType.EXPENSE)
                ledgerDao.insertTransaction(
                    Transaction(
                        type = type,
                        amount = o.getDouble("amount"),
                        categoryId = if (o.isNull("category_id")) null else o.getLong("category_id"),
                        accountId = o.getLong("account_id"),
                        toAccountId = if (o.isNull("to_account_id")) null else o.getLong("to_account_id"),
                        merchant = if (o.isNull("merchant")) null else o.getString("merchant"),
                        note = if (o.isNull("note")) null else o.getString("note"),
                        relatedId = if (o.isNull("related_id")) null else o.getLong("related_id"),
                        isDraft = o.optBoolean("is_draft", false),
                        timestamp = o.getLong("timestamp")
                    )
                )
                count++
            }
        }
        return count
    }
}
