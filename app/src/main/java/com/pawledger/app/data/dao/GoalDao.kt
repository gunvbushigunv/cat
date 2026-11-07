package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pawledger.app.data.entity.Goal
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {

    @Insert
    suspend fun insertGoal(g: Goal): Long

    @Update
    suspend fun updateGoal(g: Goal)

    @Delete
    suspend fun deleteGoal(g: Goal)

    @Query("SELECT * FROM goals ORDER BY start_date DESC")
    fun observeGoals(): Flow<List<Goal>>

    @Query("SELECT * FROM goals ORDER BY start_date DESC")
    suspend fun getAllGoals(): List<Goal>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoal(id: Long): Goal?

    @Query("SELECT * FROM goals WHERE is_completed = 0")
    suspend fun getActiveGoals(): List<Goal>
}
