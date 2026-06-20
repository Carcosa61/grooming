package com.petgrooming.manager.ui.feature.bookings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class BookingsUiState(
    val isLoading: Boolean = true,
    val bookings: List<BookingEntity> = emptyList(),
    val selectedDate: LocalDate = LocalDate.now(),
    val error: String? = null
)

@HiltViewModel
class BookingsViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BookingsUiState())
    val uiState: StateFlow<BookingsUiState> = _uiState.asStateFlow()

    init {
        loadBookingsForDate(LocalDate.now())
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
                        bookings = bookings
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

    fun loadBookingsForWeek(startDate: LocalDate) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val endDate = startDate.plusDays(6)
                bookingRepository.getBookingsBetweenDates(startDate, endDate).collect { bookings ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        bookings = bookings
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

    fun updateBookingStatus(bookingId: Long, newStatus: BookingStatus) {
        viewModelScope.launch {
            try {
                bookingRepository.updateBookingStatus(bookingId, newStatus)
            } catch (e: Exception) {
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
