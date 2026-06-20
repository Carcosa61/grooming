package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.RebookingReminderDao
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import com.petgrooming.manager.domain.repository.RebookingRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RebookingRepositoryImpl @Inject constructor(
    private val rebookingReminderDao: RebookingReminderDao
) : RebookingRepository {

    override fun getAllReminders(): Flow<List<RebookingReminderEntity>> =
        rebookingReminderDao.getAllReminders()

    override fun getPetsDueSoon(today: LocalDate): Flow<List<RebookingReminderEntity>> {
        val sevenDaysLater = today.plusDays(7)
        return rebookingReminderDao.getPetsDueSoon(today, sevenDaysLater)
    }

    override fun getPetsDueToday(today: LocalDate): Flow<List<RebookingReminderEntity>> =
        rebookingReminderDao.getPetsDueToday(today)

    override fun getPetsOverdue(today: LocalDate): Flow<List<RebookingReminderEntity>> {
        val fourteenDaysAgo = today.minusDays(14)
        return rebookingReminderDao.getPetsOverdue(fourteenDaysAgo)
    }

    override suspend fun getReminderByPetId(petId: Long): RebookingReminderEntity? =
        rebookingReminderDao.getReminderByPetId(petId)

    override suspend fun insertReminder(reminder: RebookingReminderEntity): Long =
        rebookingReminderDao.insertReminder(reminder)

    override suspend fun updateReminder(reminder: RebookingReminderEntity) =
        rebookingReminderDao.updateReminder(reminder)

    override suspend fun deleteReminder(reminder: RebookingReminderEntity) =
        rebookingReminderDao.deleteReminder(reminder)

    override suspend fun deleteReminderByPetId(petId: Long) =
        rebookingReminderDao.deleteReminderByPetId(petId)
}
