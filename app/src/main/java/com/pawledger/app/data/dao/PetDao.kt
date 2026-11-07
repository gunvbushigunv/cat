package com.pawledger.app.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.pawledger.app.data.entity.GameState
import com.pawledger.app.data.entity.OwnedFurniture
import com.pawledger.app.data.entity.OwnedSkin
import com.pawledger.app.data.entity.Pet
import com.pawledger.app.data.entity.PetSpecies
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {

    // ---- 伙伴 ----
    @Insert
    suspend fun insertPet(p: Pet): Long

    @Update
    suspend fun updatePet(p: Pet)

    @Query("SELECT * FROM pets ORDER BY id")
    fun observePets(): Flow<List<Pet>>

    @Query("SELECT * FROM pets ORDER BY id")
    suspend fun getAllPets(): List<Pet>

    @Query("SELECT * FROM pets WHERE species = :species LIMIT 1")
    suspend fun getPet(species: PetSpecies): Pet?

    @Query("SELECT * FROM pets WHERE is_companion = 1 LIMIT 1")
    suspend fun getCompanion(): Pet?

    @Query("SELECT * FROM pets WHERE is_traveling = 1 LIMIT 1")
    suspend fun getTravelingPet(): Pet?

    @Query("UPDATE pets SET is_companion = 0")
    suspend fun clearCompanion()

    @Query("UPDATE pets SET is_traveling = 0")
    suspend fun clearTraveling()

    // ---- 已解锁皮肤 ----
    @Query("SELECT skin_key FROM owned_skins")
    fun observeOwnedSkins(): Flow<List<String>>

    @Query("SELECT skin_key FROM owned_skins")
    suspend fun getOwnedSkins(): List<String>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwnedSkin(s: OwnedSkin)

    // ---- 全局游戏状态 ----
    @Query("SELECT * FROM game_state WHERE id = 1")
    fun observeGameState(): Flow<GameState?>

    @Query("SELECT * FROM game_state WHERE id = 1")
    suspend fun getGameState(): GameState?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertGameState(g: GameState)

    // ---- 已拥有家具 ----
    @Query("SELECT * FROM owned_furniture")
    fun observeOwnedFurniture(): Flow<List<OwnedFurniture>>

    @Query("SELECT * FROM owned_furniture")
    suspend fun getOwnedFurniture(): List<OwnedFurniture>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFurniture(f: OwnedFurniture)
}
