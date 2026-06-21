package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.petgrooming.manager.data.local.entity.ServicePriceEntity
import com.petgrooming.manager.data.local.entity.ServiceType
import kotlinx.coroutines.flow.Flow

@Dao
interface ServicePriceDao {
    @Query("SELECT * FROM service_prices")
    fun getAllPrices(): Flow<List<ServicePriceEntity>>

    @Query("SELECT price FROM service_prices WHERE serviceType = :serviceType")
    suspend fun getPrice(serviceType: ServiceType): Double?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(price: ServicePriceEntity)
}
