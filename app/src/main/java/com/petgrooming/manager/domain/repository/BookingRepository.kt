package com.petgrooming.manager.domain.repository

import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.BookingStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

interface BookingRepository {
    fun getAllBookings(): Flow<List<BookingEntity>>
    fun getBookingsByDate(date: LocalDate): Flow<List<BookingEntity>>
    fun getBookingsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<BookingEntity>>
    fun getBookingsByPet(petId: Long): Flow<List<BookingEntity>>
    fun getBookingsByStatus(status: BookingStatus): Flow<List<BookingEntity>>
    suspend fun getBookingById(id: Long): BookingEntity?
    suspend fun getLastVisitDates(): Map<Long, LocalDate>
    suspend fun insertBooking(booking: BookingEntity): Long
    suspend fun updateBooking(booking: BookingEntity)
    suspend fun updateBookingStatus(id: Long, status: BookingStatus)
    suspend fun deleteBooking(booking: BookingEntity)
}
