package com.petgrooming.manager.ui.feature.bookings

import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import com.petgrooming.manager.domain.repository.BookingRepository
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import com.petgrooming.manager.domain.repository.RebookingRepository
import com.petgrooming.manager.ui.feature.dashboard.BookingWithDetails
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

private const val TAG = "BookingsViewModel"

data class BookingsUiState(
    val isLoading: Boolean = true,
    val bookings: List<BookingWithDetails> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val error: String? = null
)

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val rebookingRepository: RebookingRepository,
    private val petRepository: PetRepository,
    private val ownerRepository: OwnerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    init {
        val initialDate = savedStateHandle.get<String>("date")
            ?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
            ?: LocalDate.now()
        loadBookingsForDate(initialDate)
    }

    fun loadBookingsForDate(date: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                selectedDate = date,
                error = null
            )

            try {
                bookingRepository.getBookingsByDate(date).collect { bookings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = bookings.map { mapToDetails(it) }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun goToPreviousDay() {
        val previousDay = _uiState.value.selectedDate.minusDays(1)
        loadBookingsForDate(previousDay)
    }

    fun goToNextDay() {
        val nextDay = _uiState.value.selectedDate.plusDays(1)
        loadBookingsForDate(nextDay)
    }

    fun loadBookingsForWeek(startDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val endDate = startDate.plusDays(6)
                bookingRepository.getBookingsBetweenDates(startDate, endDate).collect { bookings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = bookings.map { mapToDetails(it) }
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    private suspend fun mapToDetails(booking: BookingEntity): BookingWithDetails {
        val pet = petRepository.getPetById(booking.petId)
        val owner = pet?.let { ownerRepository.getOwnerById(it.ownerId) }
        return BookingWithDetails(
            id = booking.id,
            petId = booking.petId,
            petName = pet?.name ?: "Unknown",
            ownerName = owner?.name ?: "Unknown",
            appointmentTime = booking.appointmentTime,
            serviceType = booking.serviceType,
            status = booking.status,
            photoUri = pet?.photoUri
        )
    }

    fun updateBookingStatus(bookingId: Long, newStatus: BookingStatus) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "updateBookingStatus: bookingId=$bookingId, newStatus=$newStatus")
                bookingRepository.updateBookingStatus(bookingId, newStatus)
                
                // When booking is completed, update or create rebooking reminder
                if (newStatus == BookingStatus.COMPLETED) {
                    Log.d(TAG, "Booking completed, creating/updating rebooking reminder")
                    val booking = bookingRepository.getBookingById(bookingId)
                    booking?.let {
                        Log.d(TAG, "Booking found: petId=${it.petId}, appointmentDate=${it.appointmentDate}")
                        val existingReminder = rebookingRepository.getReminderByPetId(it.petId)
                        val intervalWeeks = existingReminder?.intervalWeeks ?: 8
                        val newDueDate = it.appointmentDate.plusWeeks(intervalWeeks.toLong())
                        Log.d(TAG, "Calculated newDueDate=$newDueDate (intervalWeeks=$intervalWeeks)")
                        
                        if (existingReminder != null) {
                            // Update existing reminder
                            Log.d(TAG, "Updating existing reminder id=${existingReminder.id}")
                            rebookingRepository.updateReminder(
                                existingReminder.copy(
                                    lastGroomDate = it.appointmentDate,
                                    dueDate = newDueDate,
                                    reminder7DaySent = false,
                                    reminderDueDateSent = false,
                                    reminder14DayOverdueSent = false
                                )
                            )
                            Log.d(TAG, "Reminder updated successfully")
                        } else {
                            // Create new reminder
                            Log.d(TAG, "Creating new reminder for petId=${it.petId}")
                            rebookingRepository.insertReminder(
                                RebookingReminderEntity(
                                    petId = it.petId,
                                    lastGroomDate = it.appointmentDate,
                                    dueDate = newDueDate,
                                    intervalWeeks = intervalWeeks
                                )
                            )
                            Log.d(TAG, "New reminder created successfully")
                        }
                    } ?: Log.w(TAG, "Booking not found for id=$bookingId")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating booking status", e)
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun deleteBooking(booking: BookingEntity) {
        viewModelScope.launch {
            try {
                bookingRepository.deleteBooking(booking)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
