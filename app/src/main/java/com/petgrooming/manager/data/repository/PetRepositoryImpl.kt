package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.OwnerDao
import com.petgrooming.manager.data.local.dao.PetDao
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.domain.repository.PetRepository
import com.petgrooming.manager.ui.feature.bookings.PetWithOwner
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PetRepositoryImpl @Inject constructor(
    private val petDao: PetDao,
    private val ownerDao: OwnerDao
) : PetRepository {

    override fun getAllPets(): Flow<List<PetEntity>> =
        petDao.getAllPets()

    override fun getAllPetsWithOwners(): Flow<List<PetWithOwner>> =
        combine(petDao.getAllPets(), ownerDao.getAllOwners()) { pets, owners ->
            val ownerMap = owners.associateBy { it.id }
            pets.mapNotNull { pet ->
                ownerMap[pet.ownerId]?.let { owner ->
                    PetWithOwner(
                        pet = pet,
                        ownerName = owner.name,
                        ownerPhone = owner.mobileNumber
                    )
                }
            }
        }

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
