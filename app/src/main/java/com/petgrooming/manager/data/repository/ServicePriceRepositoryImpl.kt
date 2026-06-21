package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.ServicePriceDao
import com.petgrooming.manager.data.local.entity.ServicePriceEntity
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.domain.repository.ServicePriceRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ServicePriceRepositoryImpl @Inject constructor(
    private val servicePriceDao: ServicePriceDao
) : ServicePriceRepository {

    override fun getAllPrices(): Flow<List<ServicePriceEntity>> =
        servicePriceDao.getAllPrices()

    override suspend fun getPrice(serviceType: ServiceType): Double? =
        servicePriceDao.getPrice(serviceType)

    override suspend fun setPrice(serviceType: ServiceType, price: Double) =
        servicePriceDao.upsert(ServicePriceEntity(serviceType = serviceType, price = price))
}
