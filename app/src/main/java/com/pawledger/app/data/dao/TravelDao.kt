package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.pawledger.app.data.entity.DiaryEntry
import com.pawledger.app.data.entity.Postcard
import com.pawledger.app.data.entity.PetSpecies
import com.pawledger.app.data.entity.TravelRecord
import kotlinx.coroutines.flow.Flow

@Dao
interface TravelDao {

    // ---- 旅行记录 ----
    @Insert
    suspend fun insertTravel(t: TravelRecord): Long

    @Update
    suspend fun updateTravel(t: TravelRecord)

    @Query("SELECT * FROM travel_records WHERE end_time IS NULL")
    suspend fun getOngoingTravels(): List<TravelRecord>

    @Query("SELECT * FROM travel_records WHERE end_time IS NULL ORDER BY start_time DESC")
    fun observeOngoingTravels(): Flow<List<TravelRecord>>

    @Query("SELECT * FROM travel_records WHERE end_time IS NOT NULL ORDER BY end_time DESC")
    fun observeReturnedTravels(): Flow<List<TravelRecord>>

    @Query("SELECT * FROM travel_records WHERE id = :id")
    suspend fun getTravel(id: Long): TravelRecord?

    // ---- 明信片 ----
    @Insert
    suspend fun insertPostcard(p: Postcard): Long

    @Query("SELECT * FROM postcards WHERE species = :species ORDER BY collected_at DESC")
    fun observePostcards(species: PetSpecies): Flow<List<Postcard>>

    @Query("SELECT * FROM postcards ORDER BY collected_at DESC")
    fun observeAllPostcards(): Flow<List<Postcard>>

    @Query("SELECT COUNT(*) FROM postcards WHERE species = :species AND destination_key = :key")
    suspend fun hasPostcard(species: PetSpecies, key: String): Int

    // ---- 旅行日记 ----
    @Insert
    suspend fun insertDiary(d: DiaryEntry): Long

    @Query("SELECT * FROM diary_entries WHERE species = :species ORDER BY created_at DESC")
    fun observeDiary(species: PetSpecies): Flow<List<DiaryEntry>>

    @Query("SELECT * FROM diary_entries ORDER BY created_at DESC")
    fun observeAllDiary(): Flow<List<DiaryEntry>>
}
