package com.petgrooming.manager.di

import android.content.Context
import androidx.room.Room
import com.petgrooming.manager.data.local.PetGroomingDatabase
import com.petgrooming.manager.data.local.dao.BookingDao
import com.petgrooming.manager.data.local.dao.OwnerDao
import com.petgrooming.manager.data.local.dao.PetDao
import com.petgrooming.manager.data.local.dao.RebookingReminderDao
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
        ).build()
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
}
