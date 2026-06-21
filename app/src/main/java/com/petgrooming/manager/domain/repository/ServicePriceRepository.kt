package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.ServicePriceEntity
import com.petgrooming.manager.data.local.entity.ServiceType
import kotlinx.coroutines.flow.Flow

interface ServicePriceRepository {
    fun getAllPrices(): Flow<List<ServicePriceEntity>>
    suspend fun getPrice(serviceType: ServiceType): Double?
    suspend fun setPrice(serviceType: ServiceType, price: Double)
}
