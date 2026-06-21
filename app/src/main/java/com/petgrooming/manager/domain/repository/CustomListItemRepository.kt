package com.petgrooming.manager.domain.repository

interface CustomListItemRepository {
    suspend fun getValues(category: String): List<String>
    suspend fun insertValue(category: String, value: String): Long

    companion object {
        const val CATEGORY_ALLERGY = "ALLERGY"
        const val CATEGORY_MEDICATION = "MEDICATION"
        const val CATEGORY_BEHAVIOR = "BEHAVIOR"
    }
}
