package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petgrooming.manager.data.local.entity.OwnerEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface OwnerDao {
    @Query("SELECT * FROM owners ORDER BY name ASC")
    fun getAllOwners(): Flow<List<OwnerEntity>>

    @Query("SELECT * FROM owners WHERE id = :id")
    suspend fun getOwnerById(id: Long): OwnerEntity?

    @Query("SELECT * FROM owners WHERE name LIKE '%' || :query || '%' OR mobileNumber LIKE '%' || :query || '%'")
    fun searchOwners(query: String): Flow<List<OwnerEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOwner(owner: OwnerEntity): Long

    @Update
    suspend fun updateOwner(owner: OwnerEntity)

    @Delete
    suspend fun deleteOwner(owner: OwnerEntity)
}
