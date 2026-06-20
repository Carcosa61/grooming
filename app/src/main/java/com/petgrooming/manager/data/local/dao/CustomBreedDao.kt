package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petgrooming.manager.data.local.entity.CustomBreedEntity
import com.petgrooming.manager.data.local.entity.PetType
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomBreedDao {
    @Query("SELECT * FROM custom_breeds ORDER BY breedName ASC")
    fun getAllCustomBreeds(): Flow<List<CustomBreedEntity>>

    @Query("SELECT * FROM custom_breeds WHERE petType = :petType ORDER BY breedName ASC")
    fun getCustomBreedsByType(petType: PetType): Flow<List<CustomBreedEntity>>

    @Query("SELECT breedName FROM custom_breeds WHERE petType = :petType ORDER BY breedName ASC")
    suspend fun getBreedNamesByType(petType: PetType): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertBreed(breed: CustomBreedEntity): Long

    @Delete
    suspend fun deleteBreed(breed: CustomBreedEntity)

    @Query("DELETE FROM custom_breeds WHERE id = :id")
    suspend fun deleteBreedById(id: Long)
}
