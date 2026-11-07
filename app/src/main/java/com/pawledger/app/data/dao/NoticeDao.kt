package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pawledger.app.data.entity.MerchantRule
import com.pawledger.app.data.entity.NotificationLog
import kotlinx.coroutines.flow.Flow

@Dao
interface NoticeDao {

    @Insert
    suspend fun insertNotificationLog(n: NotificationLog): Long

    @Update
    suspend fun updateNotificationLog(n: NotificationLog)

    @Query("SELECT * FROM notification_logs ORDER BY timestamp DESC")
    fun observeNotificationLogs(): Flow<List<NotificationLog>>

    @Query("SELECT * FROM notification_logs WHERE id = :id")
    suspend fun getNotificationLog(id: Long): NotificationLog?

    @Insert
    suspend fun insertMerchantRule(m: MerchantRule): Long

    @Query("SELECT * FROM merchant_rules")
    suspend fun getAllMerchantRules(): List<MerchantRule>

    @Query("SELECT * FROM merchant_rules WHERE :text LIKE '%' || keyword || '%' LIMIT 1")
    suspend fun findMerchantRule(text: String): MerchantRule?
}
