package com.petgrooming.manager.di

import android.content.Context
import androidx.room.Room
import com.petgrooming.manager.data.local.PetGroomingDatabase
import com.petgrooming.manager.data.local.dao.BookingDao
import com.petgrooming.manager.data.local.dao.CustomBreedDao
import com.petgrooming.manager.data.local.dao.CustomColorDao
import com.petgrooming.manager.data.local.dao.CustomListItemDao
import com.petgrooming.manager.data.local.dao.OwnerDao
import com.petgrooming.manager.data.local.dao.PetDao
import com.petgrooming.manager.data.local.dao.RebookingReminderDao
import com.petgrooming.manager.data.local.dao.ServicePriceDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(
        @ApplicationContext context: Context
    ): PetGroomingDatabase {
        return Room.databaseBuilder(
            context,
            PetGroomingDatabase::class.java,
            PetGroomingDatabase.DATABASE_NAME
        )
            .addMigrations(
                PetGroomingDatabase.MIGRATION_1_2,
                PetGroomingDatabase.MIGRATION_2_3,
                PetGroomingDatabase.MIGRATION_3_4,
                PetGroomingDatabase.MIGRATION_4_5,
                PetGroomingDatabase.MIGRATION_5_6
            )
            .build()
    }

    @Provides
    fun provideOwnerDao(database: PetGroomingDatabase): OwnerDao {
        return database.ownerDao()
    }

    @Provides
    fun providePetDao(database: PetGroomingDatabase): PetDao {
        return database.petDao()
    }

    @Provides
    fun provideBookingDao(database: PetGroomingDatabase): BookingDao {
        return database.bookingDao()
    }

    @Provides
    fun provideRebookingReminderDao(database: PetGroomingDatabase): RebookingReminderDao {
        return database.rebookingReminderDao()
    }

    @Provides
    fun provideCustomBreedDao(database: PetGroomingDatabase): CustomBreedDao {
        return database.customBreedDao()
    }

    @Provides
    fun provideCustomColorDao(database: PetGroomingDatabase): CustomColorDao {
        return database.customColorDao()
    }

    @Provides
    fun provideCustomListItemDao(database: PetGroomingDatabase): CustomListItemDao {
        return database.customListItemDao()
    }

    @Provides
    fun provideServicePriceDao(database: PetGroomingDatabase): ServicePriceDao {
        return database.servicePriceDao()
    }
}
