package com.petgrooming.manager.ui.feature.pets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class PetsUiState(
    val isLoading: Boolean = true,
    val pets: List<PetEntity> = emptyList(),
    val searchQuery: String = "",
    val error: String? = null
)

@HiltViewModel
class PetsViewModel @Inject constructor(
    private val petRepository: PetRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PetsUiState())
    val uiState: StateFlow<PetsUiState> = _uiState.asStateFlow()

    init {
        loadAllPets()
    }

    fun loadAllPets() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                petRepository.getAllPets().collect { pets ->
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        pets = pets
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
                        _uiState.value = _uiState.value.copy(pets = pets)
                    }
                } else {
                    petRepository.searchPets(query).collect { pets ->
                        _uiState.value = _uiState.value.copy(pets = pets)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message)
            }
        }
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
