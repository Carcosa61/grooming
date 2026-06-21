package com.petgrooming.manager.ui.feature.owners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerDetailState(
    val owner: OwnerEntity? = null,
    val pets: List<PetEntity> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class OwnerDetailViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository,
    private val petRepository: PetRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ownerId: Long = savedStateHandle.get<Long>("ownerId") ?: 0

    private val _uiState = MutableStateFlow(OwnerDetailState())
    val uiState: StateFlow<OwnerDetailState> = _uiState.asStateFlow()

    init {
        loadOwner()
        loadPets()
    }

    private fun loadOwner() {
        viewModelScope.launch {
            try {
                val owner = ownerRepository.getOwnerById(ownerId)
                _uiState.value = _uiState.value.copy(owner = owner, isLoading = false)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(error = e.message, isLoading = false)
            }
        }
    }

    private fun loadPets() {
        viewModelScope.launch {
            petRepository.getPetsByOwner(ownerId).collect { pets ->
                _uiState.value = _uiState.value.copy(pets = pets)
            }
        }
    }
}
