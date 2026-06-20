package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.ui.feature.bookings.PetWithOwner
import kotlinx.coroutines.flow.Flow

interface PetRepository {
    fun getAllPets(): Flow<List<PetEntity>>
    fun getAllPetsWithOwners(): Flow<List<PetWithOwner>>
    fun getPetsByOwner(ownerId: Long): Flow<List<PetEntity>>
    fun searchPets(query: String): Flow<List<PetEntity>>
    suspend fun getPetById(id: Long): PetEntity?
    suspend fun insertPet(pet: PetEntity): Long
    suspend fun updatePet(pet: PetEntity)
    suspend fun deletePet(pet: PetEntity)
}
