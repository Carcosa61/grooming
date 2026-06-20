package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.PetDao
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.domain.repository.PetRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao
) : PetRepository {

    override fun getAllPets(): Flow<List<PetEntity>> =
        petDao.getAllPets()

    override fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>> =
        petDao.getPetsByOwner(ownerId)

    override fun searchPets(query: String): Flow<List<PetEntity>> =
        petDao.searchPets(query)

    override suspend fun getPetById(id: Long): PetEntity? =
        petDao.getPetById(id)

    override suspend fun insertPet(pet: PetEntity): Long =
        petDao.insertPet(pet)

    override suspend fun updatePet(pet: PetEntity) =
        petDao.updatePet(pet.copy(updatedAt = System.currentTimeMillis()))

    override suspend fun deletePet(pet: PetEntity) =
        petDao.deletePet(pet)
}
