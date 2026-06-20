package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.OwnerDao
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.domain.repository.OwnerRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class OwnerRepositoryImpl @Inject constructor(
    private val ownerDao: OwnerDao
) : OwnerRepository {

    override fun getAllOwners(): Flow<List<OwnerEntity>> =
        ownerDao.getAllOwners()

    override fun searchOwners(query: String): Flow<List<OwnerEntity>> =
        ownerDao.searchOwners(query)

    override suspend fun getOwnerById(id: Long): OwnerEntity? =
        ownerDao.getOwnerById(id)

    override suspend fun insertOwner(owner: OwnerEntity): Long =
        ownerDao.insertOwner(owner)

    override suspend fun updateOwner(owner: OwnerEntity) =
        ownerDao.updateOwner(owner.copy(updatedAt = System.currentTimeMillis()))

    override suspend fun deleteOwner(owner: OwnerEntity) =
        ownerDao.deleteOwner(owner)
}
