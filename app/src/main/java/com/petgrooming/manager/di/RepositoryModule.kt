package com.petgrooming.manager.di

import com.petgrooming.manager.data.repository.BookingRepositoryImpl
import com.petgrooming.manager.data.repository.OwnerRepositoryImpl
import com.petgrooming.manager.data.repository.PetRepositoryImpl
import com.petgrooming.manager.data.repository.RebookingRepositoryImpl
import com.petgrooming.manager.domain.repository.BookingRepository
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import com.petgrooming.manager.domain.repository.RebookingRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindOwnerRepository(
        ownerRepositoryImpl: OwnerRepositoryImpl
    ): OwnerRepository

    @Binds
    @Singleton
    abstract fun bindPetRepository(
        petRepositoryImpl: PetRepositoryImpl
    ): PetRepository

    @Binds
    @Singleton
    abstract fun bindBookingRepository(
        bookingRepositoryImpl: BookingRepositoryImpl
    ): BookingRepository

    @Binds
    @Singleton
    abstract fun bindRebookingRepository(
        rebookingRepositoryImpl: RebookingRepositoryImpl
    ): RebookingRepository
}
