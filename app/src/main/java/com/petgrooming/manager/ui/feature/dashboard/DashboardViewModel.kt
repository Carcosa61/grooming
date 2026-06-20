package com.petgrooming.manager.ui.feature.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import com.petgrooming.manager.domain.repository.BookingRepository
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import com.petgrooming.manager.domain.repository.RebookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

data class BookingWithDetails(
    val id: Long,
    val petId: Long,
    val petName: String,
    val ownerName: String,
    val appointmentTime: LocalTime,
    val serviceType: com.petgrooming.manager.data.local.entity.ServiceType,
    val status: com.petgrooming.manager.data.local.entity.BookingStatus
)

data class DashboardUiState(
    val isLoading: Boolean = true,
    val todaysBookings: List<BookingWithDetails> = emptyList(),
    val dueSoonCount: Int = 0,
    val dueTodayCount: Int = 0,
    val overdueCount: Int = 0,
    val dueSoonPets: List<RebookingReminderEntity> = emptyList(),
    val error: String? = null
)

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val bookingRepository: BookingRepository,
    private val rebookingRepository: RebookingRepository,
    private val petRepository: PetRepository,
    private val ownerRepository: OwnerRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()
    
    private var collectionJob: Job? = null

    init {
        loadDashboardData()
    }

    fun loadDashboardData() {
        // Cancel any existing collection and start fresh
        collectionJob?.cancel()
        collectionJob = viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val today = LocalDate.now()

                combine(
                    bookingRepository.getBookingsByDate(today),
                    rebookingRepository.getPetsDueSoon(today),
                    rebookingRepository.getPetsDueToday(today),
                    rebookingRepository.getPetsOverdue(today)
                ) { todaysBookings, dueSoon, dueToday, overdue ->
                    // Map bookings to BookingWithDetails
                    val bookingsWithDetails = todaysBookings.map { booking ->
                        mapBookingToDetails(booking)
                    }
                    
                    DashboardUiState(
                        isLoading = false,
                        todaysBookings = bookingsWithDetails,
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

    private suspend fun mapBookingToDetails(booking: BookingEntity): BookingWithDetails {
        val pet = petRepository.getPetById(booking.petId)
        val owner = pet?.let { ownerRepository.getOwnerById(it.ownerId) }
        
        return BookingWithDetails(
            id = booking.id,
            petId = booking.petId,
            petName = pet?.name ?: "Unknown",
            ownerName = owner?.name ?: "Unknown",
            appointmentTime = booking.appointmentTime,
            serviceType = booking.serviceType,
            status = booking.status
        )
    }
}
