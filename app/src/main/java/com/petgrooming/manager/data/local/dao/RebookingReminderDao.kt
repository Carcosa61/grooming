package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface RebookingReminderDao {
    @Query("SELECT * FROM rebooking_reminders ORDER BY dueDate ASC")
    fun getAllReminders(): Flow<List<RebookingReminderEntity>>

    @Query("SELECT * FROM rebooking_reminders WHERE petId = :petId")
    suspend fun getReminderByPetId(petId: Long): RebookingReminderEntity?

    // Due within 7 days (due soon)
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate BETWEEN :today AND :sevenDaysLater AND reminder7DaySent = 0")
    fun getPetsDueSoon(today: LocalDate, sevenDaysLater: LocalDate): Flow<List<RebookingReminderEntity>>

    // Due today
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate = :today AND reminderDueDateSent = 0")
    fun getPetsDueToday(today: LocalDate): Flow<List<RebookingReminderEntity>>

    // Overdue (14+ days)
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate <= :fourteenDaysAgo AND reminder14DayOverdueSent = 0")
    fun getPetsOverdue(fourteenDaysAgo: LocalDate): Flow<List<RebookingReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: RebookingReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: RebookingReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: RebookingReminderEntity)

    @Query("DELETE FROM rebooking_reminders WHERE petId = :petId")
    suspend fun deleteReminderByPetId(petId: Long)
}
