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

    // Due within 7 days (due soon) - for dashboard display (shows all)
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate > :today AND dueDate <= :sevenDaysLater")
    fun getPetsDueSoon(today: LocalDate, sevenDaysLater: LocalDate): Flow<List<RebookingReminderEntity>>

    // Due today - for dashboard display (shows all)
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate = :today")
    fun getPetsDueToday(today: LocalDate): Flow<List<RebookingReminderEntity>>

    // Overdue (any past due date) - for dashboard display (shows all)
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate < :today")
    fun getPetsOverdue(today: LocalDate): Flow<List<RebookingReminderEntity>>
    
    // For notifications - only get unsent reminders
    @Query("SELECT * FROM rebooking_reminders WHERE dueDate > :today AND dueDate <= :sevenDaysLater AND reminder7DaySent = 0")
    fun getPetsDueSoonForNotification(today: LocalDate, sevenDaysLater: LocalDate): Flow<List<RebookingReminderEntity>>

    @Query("SELECT * FROM rebooking_reminders WHERE dueDate = :today AND reminderDueDateSent = 0")
    fun getPetsDueTodayForNotification(today: LocalDate): Flow<List<RebookingReminderEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertReminder(reminder: RebookingReminderEntity): Long

    @Update
    suspend fun updateReminder(reminder: RebookingReminderEntity)

    @Delete
    suspend fun deleteReminder(reminder: RebookingReminderEntity)

    @Query("DELETE FROM rebooking_reminders WHERE petId = :petId")
    suspend fun deleteReminderByPetId(petId: Long)
}
