package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.CustomListItemDao
import com.petgrooming.manager.data.local.entity.CustomListItemEntity
import com.petgrooming.manager.domain.repository.CustomListItemRepository
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomListItemRepositoryImpl @Inject constructor(
    private val customListItemDao: CustomListItemDao
) : CustomListItemRepository {

    override suspend fun getValues(category: String): List<String> =
        customListItemDao.getValuesByCategory(category)

    override suspend fun insertValue(category: String, value: String): Long =
        customListItemDao.insertItem(CustomListItemEntity(category = category, value = value))
}
