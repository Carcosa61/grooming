package com.petgrooming.manager.ui.feature.pets

import android.content.Context
import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.petgrooming.manager.data.local.entity.Gender
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.data.local.entity.PetType
import com.petgrooming.manager.data.util.ImageStorage
import com.petgrooming.manager.domain.repository.CustomBreedRepository
import com.petgrooming.manager.domain.repository.CustomColorRepository
import com.petgrooming.manager.domain.repository.CustomListItemRepository
import com.petgrooming.manager.domain.repository.OwnerRepository
import com.petgrooming.manager.domain.repository.PetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

data class PetFormState(
    val id: Long = 0,
    val ownerId: Long = 0,
    val name: String = "",
    val petType: PetType = PetType.DOG,
    val breed: String = "",
    val customBreedInput: String = "",
    val showCustomBreedInput: Boolean = false,
    val availableBreeds: List<String> = emptyList(),
    val dateOfBirth: LocalDate? = null,
    val gender: Gender? = null,
    val weight: String = "",
    val color: String = "",
    val customColorInput: String = "",
    val showCustomColorInput: Boolean = false,
    val availableColors: List<String> = emptyList(),
    val allergies: String = "",
    val medications: String = "",
    val behaviorNotes: String = "",
    val availableAllergies: List<String> = emptyList(),
    val customAllergyInput: String = "",
    val showCustomAllergyInput: Boolean = false,
    val availableMedications: List<String> = emptyList(),
    val customMedicationInput: String = "",
    val showCustomMedicationInput: Boolean = false,
    val availableBehaviorNotes: List<String> = emptyList(),
    val customBehaviorInput: String = "",
    val showCustomBehaviorInput: Boolean = false,
    val notes: String = "",
    val photoUri: String? = null,
    val isPhotoProcessing: Boolean = false,
    val owners: List<OwnerEntity> = emptyList(),
    val selectedOwner: OwnerEntity? = null,
    val isLoading: Boolean = false,
    val isSaved: Boolean = false,
    val isDeleted: Boolean = false,
    val showDeleteConfirmation: Boolean = false,
    val showUnsavedDialog: Boolean = false,
    val savedPetId: Long = 0,
    val error: String? = null,
    val nameError: String? = null,
    val breedError: String? = null,
    val ownerError: String? = null
)

// Default color lists
object ColorLists {
    val commonColors = listOf(
        "Black",
        "White",
        "Brown",
        "Golden",
        "Cream",
        "Gray",
        "Silver",
        "Orange/Ginger",
        "Tan",
        "Brindle",
        "Spotted",
        "Tri-color",
        "Black & White",
        "Brown & White",
        "Merle",
        "Sable"
    )
}

// Default breed lists
object BreedLists {
    val dogBreeds = listOf(
        "Poodle",
        "Shih Tzu",
        "Pomeranian",
        "Chihuahua",
        "Golden Retriever",
        "Labrador Retriever",
        "French Bulldog",
        "Beagle",
        "German Shepherd",
        "Yorkshire Terrier"
    )

    val catBreeds = listOf(
        "Persian",
        "Siamese",
        "Maine Coon",
        "British Shorthair",
        "Scottish Fold",
        "Bengal",
        "Ragdoll",
        "Sphynx",
        "Abyssinian",
        "Russian Blue"
    )

    val otherPetTypes = listOf(
        "Rabbit",
        "Hamster",
        "Guinea Pig",
        "Bird",
        "Ferret",
        "Hedgehog",
        "Chinchilla",
        "Turtle",
        "Fish"
    )

    fun getDefaultBreedsForType(petType: PetType): List<String> = when (petType) {
        PetType.DOG -> dogBreeds
        PetType.CAT -> catBreeds
        PetType.OTHER -> otherPetTypes
    }
}

@HiltViewModel
class PetFormViewModel @Inject constructor(
    @ApplicationContext private val context: Context,
    private val petRepository: PetRepository,
    private val ownerRepository: OwnerRepository,
    private val customBreedRepository: CustomBreedRepository,
    private val customColorRepository: CustomColorRepository,
    private val customListItemRepository: CustomListItemRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val petId: Long = savedStateHandle.get<Long>("petId") ?: 0
    private val preselectedOwnerId: Long = savedStateHandle.get<Long>("ownerId") ?: 0

    private val _uiState = MutableStateFlow(PetFormState())
    val uiState: StateFlow<PetFormState> = _uiState.asStateFlow()

    // Snapshot of saved field values, used to detect unsaved changes.
    private var savedSnapshot: String = ""
    private var baselineInitialized = false

    private fun computeSnapshot(s: PetFormState = _uiState.value): String = listOf(
        s.name, s.petType, s.breed, s.dateOfBirth, s.gender, s.weight, s.color,
        s.allergies, s.medications, s.behaviorNotes, s.notes, s.photoUri, s.ownerId
    ).joinToString("\u0001") { it?.toString() ?: "" }

    private fun captureBaseline() {
        savedSnapshot = computeSnapshot()
        baselineInitialized = true
    }

    fun hasUnsavedChanges(): Boolean = computeSnapshot() != savedSnapshot

    fun showUnsavedDialog() {
        _uiState.value = _uiState.value.copy(showUnsavedDialog = true)
    }

    fun dismissUnsavedDialog() {
        _uiState.value = _uiState.value.copy(showUnsavedDialog = false)
    }

    init {
        loadOwners()
        loadBreedsForType(_uiState.value.petType)
        loadColors()
        loadFrequentLists()
        if (petId > 0) {
            loadPet(petId)
        }
    }

    private fun loadFrequentLists() {
        viewModelScope.launch {
            val otherLabel = context.getString(com.petgrooming.manager.R.string.pet_type_other)
            val allergyDefaults = context.resources.getStringArray(com.petgrooming.manager.R.array.default_allergies).toList()
            val medicationDefaults = context.resources.getStringArray(com.petgrooming.manager.R.array.default_medications).toList()
            val behaviorDefaults = context.resources.getStringArray(com.petgrooming.manager.R.array.default_behavior_notes).toList()
            val customAllergies = customListItemRepository.getValues(CustomListItemRepository.CATEGORY_ALLERGY)
            val customMedications = customListItemRepository.getValues(CustomListItemRepository.CATEGORY_MEDICATION)
            val customBehavior = customListItemRepository.getValues(CustomListItemRepository.CATEGORY_BEHAVIOR)
            _uiState.value = _uiState.value.copy(
                availableAllergies = (allergyDefaults + customAllergies).distinct().sorted() + listOf(otherLabel),
                availableMedications = (medicationDefaults + customMedications).distinct().sorted() + listOf(otherLabel),
                availableBehaviorNotes = (behaviorDefaults + customBehavior).distinct().sorted() + listOf(otherLabel)
            )
        }
    }

    private fun loadBreedsForType(petType: PetType) {
        viewModelScope.launch {
            val customBreeds = customBreedRepository.getBreedNamesByType(petType)
            val defaultBreeds = BreedLists.getDefaultBreedsForType(petType)
            // Combine default breeds with custom breeds, add "Other" at end
            val allBreeds = (defaultBreeds + customBreeds).distinct().sorted() + listOf("Other")
            _uiState.value = _uiState.value.copy(availableBreeds = allBreeds)
        }
    }
    
    private fun loadColors() {
        viewModelScope.launch {
            val customColors = customColorRepository.getAllColorNames()
            val defaultColors = ColorLists.commonColors
            // Combine default colors with custom colors, add "Other" at end
            val allColors = (defaultColors + customColors).distinct().sorted() + listOf("Other")
            _uiState.value = _uiState.value.copy(availableColors = allColors)
        }
    }

    private fun loadOwners() {
        viewModelScope.launch {
            ownerRepository.getAllOwners().collect { owners ->
                // Check for ownerId from editing a pet, or preselected owner, or current selection
                val ownerIdToMatch = when {
                    _uiState.value.ownerId > 0 -> _uiState.value.ownerId
                    preselectedOwnerId > 0 -> preselectedOwnerId
                    else -> _uiState.value.selectedOwner?.id ?: 0
                }
                val selectedOwner = if (ownerIdToMatch > 0) {
                    owners.find { it.id == ownerIdToMatch }
                } else {
                    _uiState.value.selectedOwner
                }
                _uiState.value = _uiState.value.copy(
                    owners = owners,
                    selectedOwner = selectedOwner,
                    ownerId = selectedOwner?.id ?: _uiState.value.ownerId
                )
                // For a new pet, capture the baseline once owners (and any
                // preselected owner) are loaded so a preset owner isn't counted
                // as an unsaved change.
                if (petId == 0L && !baselineInitialized) {
                    captureBaseline()
                }
            }
        }
    }

    private fun loadPet(id: Long) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                petRepository.getPetById(id)?.let { pet ->
                    val owner = _uiState.value.owners.find { it.id == pet.ownerId }
                    _uiState.value = _uiState.value.copy(
                        id = pet.id,
                        ownerId = pet.ownerId,
                        name = pet.name,
                        petType = pet.petType,
                        breed = pet.breed,
                        dateOfBirth = pet.dateOfBirth,
                        gender = pet.gender,
                        weight = pet.weight?.toString() ?: "",
                        color = pet.color ?: "",
                        allergies = pet.allergies ?: "",
                        medications = pet.medications ?: "",
                        behaviorNotes = pet.behaviorNotes ?: "",
                        notes = pet.notes ?: "",
                        photoUri = pet.photoUri,
                        selectedOwner = owner,
                        isLoading = false
                    )
                    captureBaseline()
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
        _uiState.value = _uiState.value.copy(name = name, nameError = null)
    }

    fun onPhotoSelected(uri: Uri) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isPhotoProcessing = true)
            val path = ImageStorage.saveImage(context, uri)
            _uiState.value = _uiState.value.copy(
                photoUri = path ?: _uiState.value.photoUri,
                isPhotoProcessing = false
            )
        }
    }

    fun onPhotoRemoved() {
        _uiState.value = _uiState.value.copy(photoUri = null)
    }

    fun updatePetType(petType: PetType) {
        // Clear breed and reload breeds when type changes
        _uiState.value = _uiState.value.copy(
            petType = petType, 
            breed = "", 
            breedError = null,
            showCustomBreedInput = false,
            customBreedInput = ""
        )
        loadBreedsForType(petType)
    }

    fun updateBreed(breed: String) {
        if (breed == "Other") {
            // Show custom breed input
            _uiState.value = _uiState.value.copy(
                showCustomBreedInput = true,
                breed = "",
                breedError = null
            )
        } else {
            _uiState.value = _uiState.value.copy(
                breed = breed, 
                breedError = null,
                showCustomBreedInput = false,
                customBreedInput = ""
            )
        }
    }

    fun updateCustomBreedInput(input: String) {
        _uiState.value = _uiState.value.copy(customBreedInput = input, breedError = null)
    }

    fun confirmCustomBreed() {
        val customBreed = _uiState.value.customBreedInput.trim()
        if (customBreed.isNotBlank()) {
            viewModelScope.launch {
                // Save to custom breeds database
                customBreedRepository.insertBreed(_uiState.value.petType, customBreed)
                // Reload breeds to include the new one
                loadBreedsForType(_uiState.value.petType)
                // Set the breed
                _uiState.value = _uiState.value.copy(
                    breed = customBreed,
                    showCustomBreedInput = false,
                    customBreedInput = "",
                    breedError = null
                )
            }
        }
    }

    fun cancelCustomBreed() {
        _uiState.value = _uiState.value.copy(
            showCustomBreedInput = false,
            customBreedInput = ""
        )
    }

    fun updateOwner(owner: OwnerEntity) {
        _uiState.value = _uiState.value.copy(
            selectedOwner = owner,
            ownerId = owner.id,
            ownerError = null
        )
    }

    /**
     * Auto-selects a newly created owner once it appears in the owners list.
     * Stores the id so the owner is selected even if the list refresh arrives later.
     */
    fun selectOwnerById(ownerId: Long) {
        if (ownerId <= 0) return
        val owner = _uiState.value.owners.find { it.id == ownerId }
        _uiState.value = _uiState.value.copy(
            ownerId = ownerId,
            selectedOwner = owner ?: _uiState.value.selectedOwner,
            ownerError = null
        )
    }

    fun updateDateOfBirth(date: LocalDate) {
        _uiState.value = _uiState.value.copy(dateOfBirth = date)
    }

    fun updateGender(gender: Gender) {
        _uiState.value = _uiState.value.copy(gender = gender)
    }

    fun updateWeight(weight: String) {
        _uiState.value = _uiState.value.copy(weight = weight)
    }

    fun updateColor(color: String) {
        if (color == "Other") {
            // Show custom color input
            _uiState.value = _uiState.value.copy(
                showCustomColorInput = true,
                color = ""
            )
        } else {
            _uiState.value = _uiState.value.copy(
                color = color,
                showCustomColorInput = false,
                customColorInput = ""
            )
        }
    }

    fun updateCustomColorInput(input: String) {
        _uiState.value = _uiState.value.copy(customColorInput = input)
    }

    fun confirmCustomColor() {
        val customColor = _uiState.value.customColorInput.trim()
        if (customColor.isNotBlank()) {
            viewModelScope.launch {
                // Save to custom colors database
                customColorRepository.insertColor(customColor)
                // Reload colors to include the new one
                loadColors()
                // Set the color
                _uiState.value = _uiState.value.copy(
                    color = customColor,
                    showCustomColorInput = false,
                    customColorInput = ""
                )
            }
        }
    }

    fun cancelCustomColor() {
        _uiState.value = _uiState.value.copy(
            showCustomColorInput = false,
            customColorInput = ""
        )
    }

    fun updateAllergies(allergies: String) {
        val otherLabel = context.getString(com.petgrooming.manager.R.string.pet_type_other)
        if (allergies == otherLabel) {
            _uiState.value = _uiState.value.copy(showCustomAllergyInput = true, allergies = "")
        } else {
            _uiState.value = _uiState.value.copy(
                allergies = allergies,
                showCustomAllergyInput = false,
                customAllergyInput = ""
            )
        }
    }

    fun updateCustomAllergyInput(input: String) {
        _uiState.value = _uiState.value.copy(customAllergyInput = input)
    }

    fun confirmCustomAllergy() {
        val value = _uiState.value.customAllergyInput.trim()
        if (value.isNotBlank()) {
            viewModelScope.launch {
                customListItemRepository.insertValue(CustomListItemRepository.CATEGORY_ALLERGY, value)
                loadFrequentLists()
                _uiState.value = _uiState.value.copy(
                    allergies = value,
                    showCustomAllergyInput = false,
                    customAllergyInput = ""
                )
            }
        }
    }

    fun cancelCustomAllergy() {
        _uiState.value = _uiState.value.copy(showCustomAllergyInput = false, customAllergyInput = "")
    }

    fun updateMedications(medications: String) {
        val otherLabel = context.getString(com.petgrooming.manager.R.string.pet_type_other)
        if (medications == otherLabel) {
            _uiState.value = _uiState.value.copy(showCustomMedicationInput = true, medications = "")
        } else {
            _uiState.value = _uiState.value.copy(
                medications = medications,
                showCustomMedicationInput = false,
                customMedicationInput = ""
            )
        }
    }

    fun updateCustomMedicationInput(input: String) {
        _uiState.value = _uiState.value.copy(customMedicationInput = input)
    }

    fun confirmCustomMedication() {
        val value = _uiState.value.customMedicationInput.trim()
        if (value.isNotBlank()) {
            viewModelScope.launch {
                customListItemRepository.insertValue(CustomListItemRepository.CATEGORY_MEDICATION, value)
                loadFrequentLists()
                _uiState.value = _uiState.value.copy(
                    medications = value,
                    showCustomMedicationInput = false,
                    customMedicationInput = ""
                )
            }
        }
    }

    fun cancelCustomMedication() {
        _uiState.value = _uiState.value.copy(showCustomMedicationInput = false, customMedicationInput = "")
    }

    fun updateBehaviorNotes(notes: String) {
        val otherLabel = context.getString(com.petgrooming.manager.R.string.pet_type_other)
        if (notes == otherLabel) {
            _uiState.value = _uiState.value.copy(showCustomBehaviorInput = true, behaviorNotes = "")
        } else {
            _uiState.value = _uiState.value.copy(
                behaviorNotes = notes,
                showCustomBehaviorInput = false,
                customBehaviorInput = ""
            )
        }
    }

    fun updateCustomBehaviorInput(input: String) {
        _uiState.value = _uiState.value.copy(customBehaviorInput = input)
    }

    fun confirmCustomBehavior() {
        val value = _uiState.value.customBehaviorInput.trim()
        if (value.isNotBlank()) {
            viewModelScope.launch {
                customListItemRepository.insertValue(CustomListItemRepository.CATEGORY_BEHAVIOR, value)
                loadFrequentLists()
                _uiState.value = _uiState.value.copy(
                    behaviorNotes = value,
                    showCustomBehaviorInput = false,
                    customBehaviorInput = ""
                )
            }
        }
    }

    fun cancelCustomBehavior() {
        _uiState.value = _uiState.value.copy(showCustomBehaviorInput = false, customBehaviorInput = "")
    }

    fun updateNotes(notes: String) {
        _uiState.value = _uiState.value.copy(notes = notes)
    }

    fun save() {
        val state = _uiState.value
        
        var hasError = false
        var newState = state
        
        if (state.name.isBlank()) {
            newState = newState.copy(nameError = "Pet name is required")
            hasError = true
        }
        if (state.selectedOwner == null) {
            newState = newState.copy(ownerError = "Please select an owner")
            hasError = true
        }
        
        if (hasError) {
            _uiState.value = newState
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true)
            try {
                val pet = PetEntity(
                    id = state.id,
                    ownerId = state.selectedOwner!!.id,
                    name = state.name.trim(),
                    petType = state.petType,
                    breed = state.breed.trim(),
                    dateOfBirth = state.dateOfBirth,
                    gender = state.gender,
                    weight = state.weight.toFloatOrNull(),
                    color = state.color.trim().ifBlank { null },
                    allergies = state.allergies.trim().ifBlank { null },
                    medications = state.medications.trim().ifBlank { null },
                    behaviorNotes = state.behaviorNotes.trim().ifBlank { null },
                    notes = state.notes.trim().ifBlank { null },
                    photoUri = state.photoUri,
                    updatedAt = System.currentTimeMillis()
                )
                
                val savedId = if (state.id > 0) {
                    petRepository.updatePet(pet)
                    state.id
                } else {
                    petRepository.insertPet(pet)
                }
                
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    isSaved = true,
                    savedPetId = savedId
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
                petRepository.getPetById(state.id)?.let { pet ->
                    petRepository.deletePet(pet)
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
