package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.OwnerEntity
import kotlinx.coroutines.flow.Flow

interface OwnerRepository {
    fun getAllOwners(): Flow<List<OwnerEntity>>
    fun searchOwners(query: String): Flow<List<OwnerEntity>>
    suspend fun getOwnerById(id: Long): OwnerEntity?
    suspend fun insertOwner(owner: OwnerEntity): Long
    suspend fun updateOwner(owner: OwnerEntity)
    suspend fun deleteOwner(owner: OwnerEntity)
}
