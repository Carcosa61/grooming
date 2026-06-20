package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petgrooming.manager.data.local.entity.CustomColorEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CustomColorDao {
    @Query("SELECT * FROM custom_colors ORDER BY colorName ASC")
    fun getAllCustomColors(): Flow<List<CustomColorEntity>>

    @Query("SELECT colorName FROM custom_colors ORDER BY colorName ASC")
    suspend fun getAllColorNames(): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertColor(color: CustomColorEntity): Long

    @Delete
    suspend fun deleteColor(color: CustomColorEntity)

    @Query("DELETE FROM custom_colors WHERE id = :id")
    suspend fun deleteColorById(id: Long)
}
