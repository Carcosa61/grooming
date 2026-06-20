package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.CustomBreedEntity
import com.petgrooming.manager.data.local.entity.PetType
import kotlinx.coroutines.flow.Flow

interface CustomBreedRepository {
    fun getAllCustomBreeds(): Flow<List<CustomBreedEntity>>
    fun getCustomBreedsByType(petType: PetType): Flow<List<CustomBreedEntity>>
    suspend fun getBreedNamesByType(petType: PetType): List<String>
    suspend fun insertBreed(petType: PetType, breedName: String): Long
    suspend fun deleteBreed(breed: CustomBreedEntity)
}
