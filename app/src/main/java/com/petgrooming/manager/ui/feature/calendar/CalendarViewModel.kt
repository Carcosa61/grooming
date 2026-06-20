package com.petgrooming.manager.ui.feature.calendar

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.domain.repository.BookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth
import javax.inject.Inject

private const val TAG = "CalendarViewModel"

data class CalendarUiState(
    val isLoading: Boolean = false,
    val datesWithBookings: Set<LocalDate> = emptySet(),
    val currentMonth: YearMonth = YearMonth.now(),
    val error: String? = null
)

@HiltViewModel
class CalendarViewModel @Inject constructor(
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CalendarUiState())
    val uiState: StateFlow<CalendarUiState> = _uiState.asStateFlow()

    init {
        Log.d(TAG, "CalendarViewModel init")
        loadBookingsForMonth(YearMonth.now())
    }

    fun loadBookingsForMonth(yearMonth: YearMonth) {
        viewModelScope.launch {
            Log.d(TAG, "Loading bookings for month: $yearMonth")
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                currentMonth = yearMonth,
                error = null
            )

            try {
                val startDate = yearMonth.atDay(1)
                val endDate = yearMonth.atEndOfMonth()
                Log.d(TAG, "Date range: $startDate to $endDate")

                bookingRepository.getBookingsBetweenDates(startDate, endDate).collect { bookings ->
                    val datesWithBookings = bookings.map { it.appointmentDate }.toSet()
                    Log.d(TAG, "Found ${bookings.size} bookings, dates: $datesWithBookings")
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        datesWithBookings = datesWithBookings
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error loading bookings", e)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }

    fun goToPreviousMonth() {
        val previousMonth = _uiState.value.currentMonth.minusMonths(1)
        loadBookingsForMonth(previousMonth)
    }

    fun goToNextMonth() {
        val nextMonth = _uiState.value.currentMonth.plusMonths(1)
        loadBookingsForMonth(nextMonth)
    }
}
