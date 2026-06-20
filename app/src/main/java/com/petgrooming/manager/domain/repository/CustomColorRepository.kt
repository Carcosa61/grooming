package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.CustomColorEntity
import kotlinx.coroutines.flow.Flow

interface CustomColorRepository {
    fun getAllCustomColors(): Flow<List<CustomColorEntity>>
    suspend fun getAllColorNames(): List<String>
    suspend fun insertColor(colorName: String): Long
    suspend fun deleteColor(color: CustomColorEntity)
}
