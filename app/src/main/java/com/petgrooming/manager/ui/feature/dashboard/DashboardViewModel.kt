package com.petgrooming.manager.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import com.petgrooming.manager.domain.repository.BookingRepository
import com.petgrooming.manager.domain.repository.RebookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class DashboardUiState(
    val isLoading: Boolean = true,
    val todaysBookings: List<BookingEntity> = emptyList(),
    val dueSoonCount: Int = 0,
    val dueTodayCount: Int = 0,
    val overdueCount: Int = 0,
    val dueSoonPets: List<RebookingReminderEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val rebookingRepository: RebookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val today = LocalDate.now()

                combine(
                    bookingRepository.getBookingsByDate(today),
                    rebookingRepository.getPetsDueSoon(today),
                    rebookingRepository.getPetsDueToday(today),
                    rebookingRepository.getPetsOverdue(today)
                ) { todaysBookings, dueSoon, dueToday, overdue ->
                    DashboardUiState(
                        isLoading = false,
                        todaysBookings = todaysBookings,
                        dueSoonCount = dueSoon.size,
                        dueTodayCount = dueToday.size,
                        overdueCount = overdue.size,
                        dueSoonPets = dueSoon
                    )
                }.collect { state ->
                    _uiState.value = state
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
