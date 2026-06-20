package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petgrooming.manager.data.local.entity.PetEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PetDao {
    @Query("SELECT * FROM pets ORDER BY name ASC")
    fun getAllPets(): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE id = :id")
    suspend fun getPetById(id: Long): PetEntity?

    @Query("SELECT * FROM pets WHERE ownerId = :ownerId")
    fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>>

    @Query("SELECT * FROM pets WHERE name LIKE '%' || :query || '%'")
    fun searchPets(query: String): Flow<List<PetEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPet(pet: PetEntity): Long

    @Update
    suspend fun updatePet(pet: PetEntity)

    @Delete
    suspend fun deletePet(pet: PetEntity)
}
