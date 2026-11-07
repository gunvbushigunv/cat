package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pawledger.app.data.entity.Memo
import com.pawledger.app.data.entity.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface PlanDao {

    // ---- 日程 ----
    @Insert
    suspend fun insertSchedule(s: Schedule): Long

    @Update
    suspend fun updateSchedule(s: Schedule)

    @Delete
    suspend fun deleteSchedule(s: Schedule)

    @Query("SELECT * FROM schedules ORDER BY datetime")
    fun observeSchedules(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules ORDER BY datetime")
    suspend fun getAllSchedules(): List<Schedule>

    @Query("UPDATE schedules SET reminded = 1 WHERE id = :id")
    suspend fun markReminded(id: Long)

    // ---- 备忘录 ----
    @Insert
    suspend fun insertMemo(m: Memo): Long

    @Update
    suspend fun updateMemo(m: Memo)

    @Delete
    suspend fun deleteMemo(m: Memo)

    @Query("SELECT * FROM memos ORDER BY pinned DESC, updated_at DESC")
    fun observeMemos(): Flow<List<Memo>>

    @Query("SELECT * FROM memos WHERE content LIKE '%' || :query || '%' ORDER BY pinned DESC, updated_at DESC")
    fun searchMemos(query: String): Flow<List<Memo>>

    @Query("SELECT * FROM memos ORDER BY pinned DESC, updated_at DESC")
    suspend fun getAllMemos(): List<Memo>
}
