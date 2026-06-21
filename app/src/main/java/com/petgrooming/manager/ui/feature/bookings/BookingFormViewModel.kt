package com.petgrooming.manager.ui.feature.bookings

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.BookingEntity
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.data.local.entity.RebookingReminderEntity
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.data.util.ImageStorage
import com.petgrooming.manager.domain.repository.BookingRepository
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import com.petgrooming.manager.domain.repository.RebookingRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject

private const val TAG = "BookingFormViewModel"

data class BookingFormState(
    val id: Long = 0,
    val petId: Long = 0,
    val appointmentDate: LocalDate = LocalDate.now(),
    val appointmentTime: LocalTime = LocalTime.of(9, 0),
    val serviceType: ServiceType = ServiceType.FULL_GROOM,
    val status: BookingStatus = BookingStatus.SCHEDULED,
    val originalStatus: BookingStatus = BookingStatus.SCHEDULED,
    val notes: String = "",
    val beforePhotoUri: String? = null,
    val afterPhotoUri: String? = null,
    val isBeforePhotoProcessing: Boolean = false,
    val isAfterPhotoProcessing: Boolean = false,
    val pets: List<PetWithOwner> = emptyList(),
    val owners: List<OwnerEntity> = emptyList(),
    val selectedOwner: OwnerEntity? = null,
    val availablePets: List<PetWithOwner> = emptyList(),
    val selectedPet: PetWithOwner? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val savedBookingId: Long = 0,
    val error: String? = null,
    val petError: String? = null,
    val dateError: String? = null
)

data class PetWithOwner(
    val pet: PetEntity,
    val ownerName: String,
    val ownerPhone: String
)

@HiltViewModel
class BookingFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val bookingRepository: BookingRepository,
    private val petRepository: PetRepository,
    private val ownerRepository: OwnerRepository,
    private val rebookingRepository: RebookingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val bookingId: Long = savedStateHandle.get<Long>("bookingId") ?: 0
    private val preselectedPetId: Long = savedStateHandle.get<Long>("petId") ?: 0
    private val preselectedDate: String? = savedStateHandle.get<String>("date")

    private val _uiState = MutableStateFlow(BookingFormState())
    val uiState: StateFlow<BookingFormState> = _uiState.asStateFlow()

    init {
        loadOwners()
        loadPets()
        if (bookingId > 0) {
            loadBooking(bookingId)
        } else {
            // Apply preselected date if provided
            preselectedDate?.let {
                try {
                    val date = LocalDate.parse(it)
                    _uiState.value = _uiState.value.copy(appointmentDate = date)
                } catch (e: Exception) {
                    // Ignore invalid date
                }
            }
        }
    }

    private fun loadOwners() {
        viewModelScope.launch {
            ownerRepository.getAllOwners().collect { owners ->
                _uiState.value = _uiState.value.copy(owners = owners)
            }
        }
    }

    private fun loadPets() {
        viewModelScope.launch {
            petRepository.getAllPetsWithOwners().collect { petsWithOwners ->
                // Check for pending booking pet ID (from editing a booking)
                val petIdToMatch = pendingBookingPetId ?: if (preselectedPetId > 0) preselectedPetId else null
                val selectedPet = if (petIdToMatch != null) {
                    petsWithOwners.find { it.pet.id == petIdToMatch }
                } else {
                    _uiState.value.selectedPet
                }
                // Clear pending once matched
                if (pendingBookingPetId != null && selectedPet != null) {
                    pendingBookingPetId = null
                }
                // Derive the selected owner from the selected pet when available
                val selectedOwner = selectedPet?.let { sp ->
                    _uiState.value.owners.find { it.id == sp.pet.ownerId }
                        ?: OwnerEntity(id = sp.pet.ownerId, name = sp.ownerName, mobileNumber = sp.ownerPhone)
                } ?: _uiState.value.selectedOwner
                val availablePets = if (selectedOwner != null) {
                    petsWithOwners.filter { it.pet.ownerId == selectedOwner.id }
                } else {
                    petsWithOwners
                }
                _uiState.value = _uiState.value.copy(
                    pets = petsWithOwners,
                    availablePets = availablePets,
                    selectedOwner = selectedOwner,
                    selectedPet = selectedPet,
                    petId = selectedPet?.pet?.id ?: _uiState.value.petId
                )
            }
        }
    }

    private var pendingBookingPetId: Long? = null

    private fun loadBooking(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                bookingRepository.getBookingById(id)?.let { booking ->
                    val petWithOwner = _uiState.value.pets.find { it.pet.id == booking.petId }
                    // If pets aren't loaded yet, store the petId to match later
                    if (petWithOwner == null && _uiState.value.pets.isEmpty()) {
                        pendingBookingPetId = booking.petId
                    }
                    _uiState.value = _uiState.value.copy(
                        id = booking.id,
                        petId = booking.petId,
                        appointmentDate = booking.appointmentDate,
                        appointmentTime = booking.appointmentTime,
                        serviceType = booking.serviceType,
                        status = booking.status,
                        originalStatus = booking.status,
                        notes = booking.notes ?: "",
                        beforePhotoUri = booking.beforePhotoUri,
                        afterPhotoUri = booking.afterPhotoUri,
                        selectedPet = petWithOwner,
                        isLoading = false
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

    fun updatePet(petWithOwner: PetWithOwner) {
        _uiState.value = _uiState.value.copy(
            selectedPet = petWithOwner,
            petId = petWithOwner.pet.id,
            petError = null
        )
    }

    fun updateOwner(owner: OwnerEntity) {
        val state = _uiState.value
        val availablePets = state.pets.filter { it.pet.ownerId == owner.id }
        // Keep the current pet only if it still belongs to the selected owner
        val keepPet = state.selectedPet?.takeIf { it.pet.ownerId == owner.id }
        _uiState.value = state.copy(
            selectedOwner = owner,
            availablePets = availablePets,
            selectedPet = keepPet,
            petId = keepPet?.pet?.id ?: 0,
            petError = null
        )
    }

    fun updateDate(date: LocalDate) {
        _uiState.value = _uiState.value.copy(
            appointmentDate = date,
            dateError = null
        )
    }

    fun updateTime(time: LocalTime) {
        _uiState.value = _uiState.value.copy(appointmentTime = time)
    }

    fun updateServiceType(serviceType: ServiceType) {
        _uiState.value = _uiState.value.copy(serviceType = serviceType)
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun updateStatus(status: BookingStatus) {
        _uiState.value = _uiState.value.copy(status = status)
    }

    fun onBeforePhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isBeforePhotoProcessing = true)
            val path = ImageStorage.saveImage(context, uri)
            _uiState.value = _uiState.value.copy(
                beforePhotoUri = path ?: _uiState.value.beforePhotoUri,
                isBeforePhotoProcessing = false
            )
        }
    }

    fun onBeforePhotoRemoved() {
        _uiState.value = _uiState.value.copy(beforePhotoUri = null)
    }

    fun onAfterPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isAfterPhotoProcessing = true)
            val path = ImageStorage.saveImage(context, uri)
            _uiState.value = _uiState.value.copy(
                afterPhotoUri = path ?: _uiState.value.afterPhotoUri,
                isAfterPhotoProcessing = false
            )
        }
    }

    fun onAfterPhotoRemoved() {
        _uiState.value = _uiState.value.copy(afterPhotoUri = null)
    }

    fun save() {
        val state = _uiState.value
        
        var hasError = false
        var newState = state
        
        if (state.selectedPet == null) {
            newState = newState.copy(petError = "Please select a pet")
            hasError = true
        }
        if (state.appointmentDate.isBefore(LocalDate.now()) && state.id == 0L) {
            newState = newState.copy(dateError = "Date cannot be in the past")
            hasError = true
        }
        
        if (hasError) {
            _uiState.value = newState
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val booking = BookingEntity(
                    id = state.id,
                    petId = state.selectedPet!!.pet.id,
                    appointmentDate = state.appointmentDate,
                    appointmentTime = state.appointmentTime,
                    serviceType = state.serviceType,
                    status = state.status,
                    notes = state.notes.trim().ifBlank { null },
                    beforePhotoUri = state.beforePhotoUri,
                    afterPhotoUri = state.afterPhotoUri,
                    updatedAt = System.currentTimeMillis()
                )
                
                val savedId = if (state.id > 0) {
                    bookingRepository.updateBooking(booking)
                    state.id
                } else {
                    bookingRepository.insertBooking(booking)
                }
                
                // Update rebooking reminder when status changes to COMPLETED
                if (state.status == BookingStatus.COMPLETED && state.originalStatus != BookingStatus.COMPLETED) {
                    updateRebookingReminder(booking)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    savedBookingId = savedId
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
    
    private suspend fun updateRebookingReminder(booking: BookingEntity) {
        Log.d(TAG, "updateRebookingReminder called for petId=${booking.petId}, appointmentDate=${booking.appointmentDate}")
        val existingReminder = rebookingRepository.getReminderByPetId(booking.petId)
        val intervalWeeks = existingReminder?.intervalWeeks ?: 8
        val newDueDate = booking.appointmentDate.plusWeeks(intervalWeeks.toLong())
        Log.d(TAG, "Calculated newDueDate=$newDueDate (intervalWeeks=$intervalWeeks)")
        
        if (existingReminder != null) {
            // Update existing reminder
            Log.d(TAG, "Updating existing reminder id=${existingReminder.id}")
            rebookingRepository.updateReminder(
                existingReminder.copy(
                    lastGroomDate = booking.appointmentDate,
                    dueDate = newDueDate,
                    reminder7DaySent = false,
                    reminderDueDateSent = false,
                    reminder14DayOverdueSent = false
                )
            )
        } else {
            // Create new reminder
            Log.d(TAG, "Creating new reminder for petId=${booking.petId}")
            rebookingRepository.insertReminder(
                RebookingReminderEntity(
                    petId = booking.petId,
                    lastGroomDate = booking.appointmentDate,
                    dueDate = newDueDate,
                    intervalWeeks = intervalWeeks
                )
            )
        }
        Log.d(TAG, "Rebooking reminder saved successfully")
    }

    fun showDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = true)
    }

    fun hideDeleteConfirmation() {
        _uiState.value = _uiState.value.copy(showDeleteConfirmation = false)
    }

    fun delete() {
        val state = _uiState.value
        if (state.id <= 0) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, showDeleteConfirmation = false)
            try {
                bookingRepository.getBookingById(state.id)?.let { booking ->
                    bookingRepository.deleteBooking(booking)
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isDeleted = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
    }
}
