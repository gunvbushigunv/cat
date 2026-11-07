package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pawledger.app.data.entity.Account
import com.pawledger.app.data.entity.Budget
import com.pawledger.app.data.entity.Category
import com.pawledger.app.data.entity.RecurringBill
import com.pawledger.app.data.entity.Transaction
import kotlinx.coroutines.flow.Flow

@Dao
interface LedgerDao {

    // ---- 流水 ----
    @Insert
    suspend fun insertTransaction(t: Transaction): Long

    @Update
    suspend fun updateTransaction(t: Transaction)

    @Delete
    suspend fun deleteTransaction(t: Transaction)

    @Query("SELECT * FROM transactions ORDER BY timestamp DESC")
    fun observeAllTransactions(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE timestamp >= :start AND timestamp < :end ORDER BY timestamp DESC")
    fun observeTransactionsBetween(start: Long, end: Long): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE is_draft = 1 ORDER BY timestamp DESC")
    fun observeDrafts(): Flow<List<Transaction>>

    @Query("SELECT * FROM transactions WHERE id = :id")
    suspend fun getTransaction(id: Long): Transaction?

    @Query("SELECT * FROM transactions")
    suspend fun getAllTransactions(): List<Transaction>

    @Query("SELECT COUNT(*) FROM transactions")
    suspend fun countTransactions(): Int

    // ---- 分类 ----
    @Insert
    suspend fun insertCategory(c: Category): Long

    @Update
    suspend fun updateCategory(c: Category)

    @Delete
    suspend fun deleteCategory(c: Category)

    @Query("SELECT * FROM categories ORDER BY id")
    fun observeCategories(): Flow<List<Category>>

    @Query("SELECT * FROM categories ORDER BY id")
    suspend fun getAllCategories(): List<Category>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategory(id: Long): Category?

    // ---- 账户 ----
    @Insert
    suspend fun insertAccount(a: Account): Long

    @Update
    suspend fun updateAccount(a: Account)

    @Delete
    suspend fun deleteAccount(a: Account)

    @Query("SELECT * FROM accounts ORDER BY id")
    fun observeAccounts(): Flow<List<Account>>

    @Query("SELECT * FROM accounts ORDER BY id")
    suspend fun getAllAccounts(): List<Account>

    // ---- 预算 ----
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertBudget(b: Budget)

    @Query("SELECT * FROM budgets WHERE month = :month")
    fun observeBudget(month: String): Flow<Budget?>

    @Query("SELECT * FROM budgets WHERE month = :month")
    suspend fun getBudget(month: String): Budget?

    // ---- 周期账单 ----
    @Insert
    suspend fun insertRecurringBill(r: RecurringBill): Long

    @Update
    suspend fun updateRecurringBill(r: RecurringBill)

    @Delete
    suspend fun deleteRecurringBill(r: RecurringBill)

    @Query("SELECT * FROM recurring_bills ORDER BY due_day")
    fun observeRecurringBills(): Flow<List<RecurringBill>>

    @Query("SELECT * FROM recurring_bills WHERE enabled = 1")
    suspend fun getEnabledRecurringBills(): List<RecurringBill>
}
