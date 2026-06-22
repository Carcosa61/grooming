package com.petgrooming.manager.ui.feature.owners

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.domain.repository.OwnerRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OwnerFormState(
    val id: Long = 0,
    val name: String = "",
    val mobileNumber: String = "",
    val email: String = "",
    val lineId: String = "",
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val error: String? = null,
    val nameError: String? = null,
    val mobileError: String? = null
)

@HiltViewModel
class OwnerFormViewModel @Inject constructor(
    private val ownerRepository: OwnerRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ownerId: Long = savedStateHandle.get<Long>("ownerId") ?: 0

    private val _uiState = MutableStateFlow(OwnerFormState())
    val uiState: StateFlow<OwnerFormState> = _uiState.asStateFlow()

    init {
        if (ownerId > 0) {
            loadOwner(ownerId)
        }
    }

    private fun loadOwner(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                ownerRepository.getOwnerById(id)?.let { owner ->
                    _uiState.value = _uiState.value.copy(
                        id = owner.id,
                        name = owner.name,
                        mobileNumber = owner.mobileNumber,
                        email = owner.email ?: "",
                        lineId = owner.lineId ?: "",
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

    fun updateName(name: String) {
        _uiState.value = _uiState.value.copy(
            name = name,
            nameError = null
        )
    }

    fun updateMobileNumber(mobile: String) {
        _uiState.value = _uiState.value.copy(
            mobileNumber = mobile,
            mobileError = null
        )
    }

    fun updateEmail(email: String) {
        _uiState.value = _uiState.value.copy(email = email)
    }

    fun updateLineId(lineId: String) {
        _uiState.value = _uiState.value.copy(lineId = lineId)
    }

    fun save() {
        val state = _uiState.value
        
        // Validate
        var hasError = false
        var newState = state
        
        if (state.name.isBlank()) {
            newState = newState.copy(nameError = "Name is required")
            hasError = true
        }
        if (state.mobileNumber.isBlank()) {
            newState = newState.copy(mobileError = "Mobile number is required")
            hasError = true
        }
        
        if (hasError) {
            _uiState.value = newState
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val owner = OwnerEntity(
                    id = state.id,
                    name = state.name.trim(),
                    mobileNumber = state.mobileNumber.trim(),
                    email = state.email.trim().ifBlank { null },
                    lineId = state.lineId.trim().ifBlank { null },
                    updatedAt = System.currentTimeMillis()
                )
                
                val savedId = if (state.id > 0) {
                    ownerRepository.updateOwner(owner)
                    state.id
                } else {
                    ownerRepository.insertOwner(owner)
                }
                
                _uiState.value = _uiState.value.copy(
                    id = savedId,
                    isLoading = false,
                    isSaved = true
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message
                )
            }
        }
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
                ownerRepository.getOwnerById(state.id)?.let { owner ->
                    ownerRepository.deleteOwner(owner)
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
