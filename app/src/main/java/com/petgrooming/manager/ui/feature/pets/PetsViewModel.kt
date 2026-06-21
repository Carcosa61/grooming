package com.petgrooming.manager.ui.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.domain.repository.BookingRepository
import com.petgrooming.manager.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

enum class PetSortOrder {
    NAME_ASC,
    NAME_DESC,
    LAST_VISIT
}

data class PetsUiState(
    val isLoading: Boolean = true,
    val pets: List<PetEntity> = emptyList(),
    val searchQuery: String = "",
    val sortOrder: PetSortOrder = PetSortOrder.NAME_ASC,
    val lastVisitDates: Map<Long, LocalDate> = emptyMap(),
    val error: String? = null
)

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val petRepository: PetRepository,
    private val bookingRepository: BookingRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    private var rawPets: List<PetEntity> = emptyList()

    init {
        loadLastVisitDates()
        loadAllPets()
    }

    private fun loadLastVisitDates() {
        viewModelScope.launch {
            try {
                val dates = bookingRepository.getLastVisitDates()
                _uiState.value = _uiState.value.copy(
                    lastVisitDates = dates,
                    pets = applySort(rawPets, _uiState.value.sortOrder, dates)
                )
            } catch (_: Exception) {
                // Last-visit sorting is best-effort
            }
        }
    }

    fun loadAllPets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                petRepository.getAllPets().collect { pets ->
                    rawPets = pets
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pets = applySort(pets, _uiState.value.sortOrder, _uiState.value.lastVisitDates)
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

    fun searchPets(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)

        viewModelScope.launch {
            try {
                if (query.isBlank()) {
                    petRepository.getAllPets().collect { pets ->
                        rawPets = pets
                        _uiState.value = _uiState.value.copy(
                            pets = applySort(pets, _uiState.value.sortOrder, _uiState.value.lastVisitDates)
                        )
                    }
                } else {
                    petRepository.searchPets(query).collect { pets ->
                        rawPets = pets
                        _uiState.value = _uiState.value.copy(
                            pets = applySort(pets, _uiState.value.sortOrder, _uiState.value.lastVisitDates)
                        )
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }

    fun setSortOrder(order: PetSortOrder) {
        _uiState.value = _uiState.value.copy(
            sortOrder = order,
            pets = applySort(rawPets, order, _uiState.value.lastVisitDates)
        )
    }

    private fun applySort(
        pets: List<PetEntity>,
        order: PetSortOrder,
        lastVisits: Map<Long, LocalDate>
    ): List<PetEntity> = when (order) {
        PetSortOrder.NAME_ASC -> pets.sortedBy { it.name.lowercase() }
        PetSortOrder.NAME_DESC -> pets.sortedByDescending { it.name.lowercase() }
        PetSortOrder.LAST_VISIT -> pets.sortedWith(
            compareByDescending<PetEntity> { lastVisits[it.id] != null }
                .thenByDescending { lastVisits[it.id] }
                .thenBy { it.name.lowercase() }
        )
    }

    fun deletePet(pet: PetEntity) {
        viewModelScope.launch {
            try {
                petRepository.deletePet(pet)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
    }
}
