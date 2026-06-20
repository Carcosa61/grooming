package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.CustomColorDao
import com.petgrooming.manager.data.local.entity.CustomColorEntity
import com.petgrooming.manager.domain.repository.CustomColorRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomColorRepositoryImpl @Inject constructor(
    private val customColorDao: CustomColorDao
) : CustomColorRepository {

    override fun getAllCustomColors(): Flow<List<CustomColorEntity>> =
        customColorDao.getAllCustomColors()

    override suspend fun getAllColorNames(): List<String> =
        customColorDao.getAllColorNames()

    override suspend fun insertColor(colorName: String): Long =
        customColorDao.insertColor(CustomColorEntity(colorName = colorName))

    override suspend fun deleteColor(color: CustomColorEntity) =
        customColorDao.deleteColor(color)
}
