package com.petgrooming.manager.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.petgrooming.manager.data.local.dao.BookingDao
import com.petgrooming.manager.data.local.dao.CustomBreedDao
import com.petgrooming.manager.data.local.dao.CustomColorDao
import com.petgrooming.manager.data.local.dao.CustomListItemDao
import com.petgrooming.manager.data.local.dao.OwnerDao
import com.petgrooming.manager.data.local.dao.PetDao
import com.petgrooming.manager.data.local.dao.RebookingReminderDao
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.CustomBreedEntity
import com.petgrooming.manager.data.local.entity.CustomColorEntity
import com.petgrooming.manager.data.local.entity.CustomListItemEntity
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity

@Database(
    entities = [
        OwnerEntity::class,
        PetEntity::class,
        BookingEntity::class,
        RebookingReminderEntity::class,
        CustomBreedEntity::class,
        CustomColorEntity::class,
        CustomListItemEntity::class
    ],
    version = 5,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class PetGroomingDatabase : RoomDatabase() {
    abstract fun ownerDao(): OwnerDao
    abstract fun petDao(): PetDao
    abstract fun bookingDao(): BookingDao
    abstract fun rebookingReminderDao(): RebookingReminderDao
    abstract fun customBreedDao(): CustomBreedDao
    abstract fun customColorDao(): CustomColorDao
    abstract fun customListItemDao(): CustomListItemDao

    companion object {
        const val DATABASE_NAME = "pet_grooming.db"

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Add petType column with default value 'DOG'
                db.execSQL("ALTER TABLE pets ADD COLUMN petType TEXT NOT NULL DEFAULT 'DOG'")
                // Add notes column
                db.execSQL("ALTER TABLE pets ADD COLUMN notes TEXT")
            }
        }

        val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create custom_breeds table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_breeds (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        petType TEXT NOT NULL,
                        breedName TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                // Create unique index on petType + breedName
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_custom_breeds_petType_breedName ON custom_breeds (petType, breedName)")
            }
        }
        
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create custom_colors table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_colors (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        colorName TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                // Create unique index on colorName
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_custom_colors_colorName ON custom_colors (colorName)")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Normalize removed booking statuses to SCHEDULED to avoid enum parse crashes
                db.execSQL("UPDATE bookings SET status = 'SCHEDULED' WHERE status IN ('CHECKED_IN', 'GROOMING', 'READY_FOR_COLLECTION')")
                // Create custom_list_items table (allergies, medications, behavior notes)
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS custom_list_items (
                        id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                        category TEXT NOT NULL,
                        value TEXT NOT NULL,
                        createdAt INTEGER NOT NULL
                    )
                """.trimIndent())
                db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS index_custom_list_items_category_value ON custom_list_items (category, value)")
            }
        }
    }
}
