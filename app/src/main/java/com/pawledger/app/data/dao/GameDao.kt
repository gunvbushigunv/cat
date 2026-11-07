package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.pawledger.app.data.entity.CheckIn
import com.pawledger.app.data.entity.PointsLedger
import kotlinx.coroutines.flow.Flow

@Dao
interface GameDao {

    @Insert
    suspend fun insertLedger(l: PointsLedger): Long

    @Query("SELECT * FROM points_ledger ORDER BY timestamp DESC LIMIT 100")
    fun observeLedger(): Flow<List<PointsLedger>>

    @Insert
    suspend fun insertCheckIn(c: CheckIn)

    @Query("SELECT * FROM checkins WHERE date = :date")
    suspend fun getCheckIn(date: String): CheckIn?

    @Query("SELECT * FROM checkins ORDER BY date DESC LIMIT 1")
    suspend fun getLatestCheckIn(): CheckIn?
}
