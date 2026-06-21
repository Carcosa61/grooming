package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petgrooming.manager.data.local.entity.CustomListItemEntity

@Dao
interface CustomListItemDao {
    @Query("SELECT value FROM custom_list_items WHERE category = :category ORDER BY value ASC")
    suspend fun getValuesByCategory(category: String): List<String>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertItem(item: CustomListItemEntity): Long
}
