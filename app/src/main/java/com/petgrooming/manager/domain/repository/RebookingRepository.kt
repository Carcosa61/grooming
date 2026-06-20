package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface RebookingRepository {
    fun getAllReminders(): Flow<List<RebookingReminderEntity>>
    fun getPetsDueSoon(today: LocalDate): Flow<List<RebookingReminderEntity>>
    fun getPetsDueToday(today: LocalDate): Flow<List<RebookingReminderEntity>>
    fun getPetsOverdue(today: LocalDate): Flow<List<RebookingReminderEntity>>
    suspend fun getReminderByPetId(petId: Long): RebookingReminderEntity?
    suspend fun insertReminder(reminder: RebookingReminderEntity): Long
    suspend fun updateReminder(reminder: RebookingReminderEntity)
    suspend fun deleteReminder(reminder: RebookingReminderEntity)
    suspend fun deleteReminderByPetId(petId: Long)
}
