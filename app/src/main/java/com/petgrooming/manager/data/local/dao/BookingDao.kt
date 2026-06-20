package com.petgrooming.manager.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.BookingStatus
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate

@Dao
interface BookingDao {
    @Query("SELECT * FROM bookings ORDER BY appointmentDate DESC, appointmentTime DESC")
    fun getAllBookings(): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE id = :id")
    suspend fun getBookingById(id: Long): BookingEntity?

    @Query("SELECT * FROM bookings WHERE appointmentDate = :date ORDER BY appointmentTime ASC")
    fun getBookingsByDate(date: LocalDate): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE appointmentDate BETWEEN :startDate AND :endDate ORDER BY appointmentDate ASC, appointmentTime ASC")
    fun getBookingsBetweenDates(startDate: LocalDate, endDate: LocalDate): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE petId = :petId ORDER BY appointmentDate DESC")
    fun getBookingsByPet(petId: Long): Flow<List<BookingEntity>>

    @Query("SELECT * FROM bookings WHERE status = :status ORDER BY appointmentDate ASC")
    fun getBookingsByStatus(status: BookingStatus): Flow<List<BookingEntity>>

    @Query("UPDATE bookings SET status = :status, updatedAt = :updatedAt WHERE id = :id")
    suspend fun updateBookingStatus(id: Long, status: BookingStatus, updatedAt: Long = System.currentTimeMillis())

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertBooking(booking: BookingEntity): Long

    @Update
    suspend fun updateBooking(booking: BookingEntity)

    @Delete
    suspend fun deleteBooking(booking: BookingEntity)
}
