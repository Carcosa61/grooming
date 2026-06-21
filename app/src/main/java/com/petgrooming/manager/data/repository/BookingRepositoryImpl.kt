package com.petgrooming.manager.data.repository

import com.petgrooming.manager.data.local.dao.BookingDao
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.domain.repository.BookingRepository
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BookingRepositoryImpl @Inject constructor(
    private val bookingDao: BookingDao
) : BookingRepository {

    override fun getAllBookings(): Flow<List<BookingEntity>> =
        bookingDao.getAllBookings()

    override fun getBookingsByDate(date: LocalDate): Flow<List<BookingEntity>> =
        bookingDao.getBookingsByDate(date)

    override fun getBookingsBetweenDates(
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<BookingEntity>> =
        bookingDao.getBookingsBetweenDates(startDate, endDate)

    override fun getBookingsByPet(petId: Long): Flow<List<BookingEntity>> =
        bookingDao.getBookingsByPet(petId)

    override fun getBookingsByStatus(status: BookingStatus): Flow<List<BookingEntity>> =
        bookingDao.getBookingsByStatus(status)

    override suspend fun getBookingById(id: Long): BookingEntity? =
        bookingDao.getBookingById(id)

    override suspend fun getLastVisitDates(): Map<Long, LocalDate> =
        bookingDao.getLastVisitDates()
            .mapNotNull { row -> row.lastVisit?.let { row.petId to it } }
            .toMap()

    override suspend fun insertBooking(booking: BookingEntity): Long =
        bookingDao.insertBooking(booking)

    override suspend fun updateBooking(booking: BookingEntity) =
        bookingDao.updateBooking(booking.copy(updatedAt = System.currentTimeMillis()))

    override suspend fun updateBookingStatus(id: Long, status: BookingStatus) =
        bookingDao.updateBookingStatus(id, status)

    override suspend fun deleteBooking(booking: BookingEntity) =
        bookingDao.deleteBooking(booking)
}
