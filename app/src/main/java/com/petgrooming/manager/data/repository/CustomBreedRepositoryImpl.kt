package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.CustomBreedDao
import com.petgrooming.manager.data.local.entity.CustomBreedEntity
import com.petgrooming.manager.data.local.entity.PetType
import com.petgrooming.manager.domain.repository.CustomBreedRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CustomBreedRepositoryImpl @Inject constructor(
    private val customBreedDao: CustomBreedDao
) : CustomBreedRepository {

    override fun getAllCustomBreeds(): Flow<List<CustomBreedEntity>> =
        customBreedDao.getAllCustomBreeds()

    override fun getCustomBreedsByType(petType: PetType): Flow<List<CustomBreedEntity>> =
        customBreedDao.getCustomBreedsByType(petType)

    override suspend fun getBreedNamesByType(petType: PetType): List<String> =
        customBreedDao.getBreedNamesByType(petType)

    override suspend fun insertBreed(petType: PetType, breedName: String): Long =
        customBreedDao.insertBreed(
            CustomBreedEntity(
                petType = petType,
                breedName = breedName.trim()
            )
        )

    override suspend fun deleteBreed(breed: CustomBreedEntity) =
        customBreedDao.deleteBreed(breed)
}
