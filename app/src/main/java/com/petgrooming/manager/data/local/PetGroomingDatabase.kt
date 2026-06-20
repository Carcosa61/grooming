package com.petgrooming.manager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.petgrooming.manager.data.local.dao.BookingDao
import com.petgrooming.manager.data.local.dao.OwnerDao
import com.petgrooming.manager.data.local.dao.PetDao
import com.petgrooming.manager.data.local.dao.RebookingReminderDao
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity

@Database(
    entities = [
        OwnerEntity::class,
        PetEntity::class,
        BookingEntity::class,
        RebookingReminderEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PetGroomingDatabase : RoomDatabase() {
    abstract fun ownerDao(): OwnerDao
    abstract fun petDao(): PetDao
    abstract fun bookingDao(): BookingDao
    abstract fun rebookingReminderDao(): RebookingReminderDao

    companion object {
        const val DATABASE_NAME = "pet_grooming.db"
    }
}
