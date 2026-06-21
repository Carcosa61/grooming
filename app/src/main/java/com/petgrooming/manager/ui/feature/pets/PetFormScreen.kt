package com.petgrooming.manager.ui.feature.pets

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.Gender
import com.petgrooming.manager.data.local.entity.PetType
import com.petgrooming.manager.ui.components.AvatarPhotoPicker
import com.petgrooming.manager.ui.components.DatePickerField
import com.petgrooming.manager.ui.components.DropdownField
import com.petgrooming.manager.ui.components.FormButtons
import com.petgrooming.manager.ui.components.FormTextField

/**
 * Returns localized color name for display, or the original value for custom colors.
 */
@Composable
fun getLocalizedColorName(colorKey: String): String {
    return when (colorKey) {
        "Black" -> stringResource(R.string.color_black)
        "White" -> stringResource(R.string.color_white)
        "Brown" -> stringResource(R.string.color_brown)
        "Golden" -> stringResource(R.string.color_golden)
        "Cream" -> stringResource(R.string.color_cream)
        "Gray" -> stringResource(R.string.color_gray)
        "Silver" -> stringResource(R.string.color_silver)
        "Orange/Ginger" -> stringResource(R.string.color_orange_ginger)
        "Tan" -> stringResource(R.string.color_tan)
        "Brindle" -> stringResource(R.string.color_brindle)
        "Spotted" -> stringResource(R.string.color_spotted)
        "Tri-color" -> stringResource(R.string.color_tricolor)
        "Black & White" -> stringResource(R.string.color_black_white)
        "Brown & White" -> stringResource(R.string.color_brown_white)
        "Merle" -> stringResource(R.string.color_merle)
        "Sable" -> stringResource(R.string.color_sable)
        "Other" -> stringResource(R.string.color_other)
        else -> colorKey // Custom colors - return as-is
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PetFormScreen(
    onNavigateBack: () -> Unit,
    onPetSaved: (Long) -> Unit,
    onAddOwner: () -> Unit,
    viewModel: PetFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onPetSaved(uiState.savedPetId)
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteConfirmation,
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_pet_message)) },
            confirmButton = {
                TextButton(onClick = viewModel::delete) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::hideDeleteConfirmation) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (uiState.id > 0) stringResource(R.string.edit_pet)
                        else stringResource(R.string.add_pet)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.cancel))
                    }
                },
                actions = {
                    if (uiState.id > 0) {
                        IconButton(onClick = viewModel::showDeleteConfirmation) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = stringResource(R.string.delete),
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        if (uiState.isLoading && uiState.id > 0) {
            CircularProgressIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Pet Photo
                AvatarPhotoPicker(
                    name = uiState.name,
                    photoUri = uiState.photoUri,
                    onPhotoSelected = viewModel::onPhotoSelected,
                    onPhotoRemoved = viewModel::onPhotoRemoved
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Owner Selection
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DropdownField(
                        selected = uiState.selectedOwner,
                        options = uiState.owners,
                        onOptionSelected = viewModel::updateOwner,
                        label = stringResource(R.string.select_owner),
                        optionLabel = { "${it.name} (${it.mobileNumber})" },
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(onClick = onAddOwner) {
                        Icon(Icons.Default.PersonAdd, contentDescription = stringResource(R.string.add_owner))
                    }
                }
                if (uiState.ownerError != null) {
                    Text(
                        text = uiState.ownerError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Pet Name
                FormTextField(
                    value = uiState.name,
                    onValueChange = viewModel::updateName,
                    label = stringResource(R.string.pet_name),
                    isError = uiState.nameError != null,
                    errorMessage = uiState.nameError
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pet Type Selection
                Text(
                    text = stringResource(R.string.pet_type),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    FilterChip(
                        selected = uiState.petType == PetType.DOG,
                        onClick = { viewModel.updatePetType(PetType.DOG) },
                        label = { Text(stringResource(R.string.pet_type_dog)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = uiState.petType == PetType.CAT,
                        onClick = { viewModel.updatePetType(PetType.CAT) },
                        label = { Text(stringResource(R.string.pet_type_cat)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = uiState.petType == PetType.OTHER,
                        onClick = { viewModel.updatePetType(PetType.OTHER) },
                        label = { Text(stringResource(R.string.pet_type_other)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Breed Selection based on Pet Type
                if (uiState.showCustomBreedInput) {
                    // Custom breed input mode
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.customBreedInput,
                            onValueChange = viewModel::updateCustomBreedInput,
                            label = { Text(stringResource(R.string.enter_custom_breed)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = viewModel::confirmCustomBreed,
                            enabled = uiState.customBreedInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                        }
                        IconButton(onClick = viewModel::cancelCustomBreed) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    }
                } else {
                    // Normal breed dropdown
                    DropdownField(
                        selected = uiState.breed.ifBlank { null },
                        options = uiState.availableBreeds,
                        onOptionSelected = viewModel::updateBreed,
                        label = stringResource(R.string.pet_breed),
                        optionLabel = { it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
                if (uiState.breedError != null) {
                    Text(
                        text = uiState.breedError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date of Birth
                DatePickerField(
                    selectedDate = uiState.dateOfBirth,
                    onDateSelected = viewModel::updateDateOfBirth,
                    label = stringResource(R.string.date_of_birth)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Gender
                Text(
                    text = stringResource(R.string.gender),
                    style = MaterialTheme.typography.bodyMedium
                )
                Row(modifier = Modifier.padding(top = 8.dp)) {
                    FilterChip(
                        selected = uiState.gender == Gender.MALE,
                        onClick = { viewModel.updateGender(Gender.MALE) },
                        label = { Text(stringResource(R.string.gender_male)) }
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    FilterChip(
                        selected = uiState.gender == Gender.FEMALE,
                        onClick = { viewModel.updateGender(Gender.FEMALE) },
                        label = { Text(stringResource(R.string.gender_female)) }
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Weight and Color
                Row(modifier = Modifier.fillMaxWidth()) {
                    FormTextField(
                        value = uiState.weight,
                        onValueChange = viewModel::updateWeight,
                        label = stringResource(R.string.weight_kg),
                        keyboardType = KeyboardType.Decimal,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    
                    // Color dropdown/input
                    if (uiState.showCustomColorInput) {
                        // Custom color input mode
                        Row(
                            modifier = Modifier.weight(1f),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = uiState.customColorInput,
                                onValueChange = viewModel::updateCustomColorInput,
                                label = { Text(stringResource(R.string.enter_custom_color)) },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                            IconButton(
                                onClick = viewModel::confirmCustomColor,
                                enabled = uiState.customColorInput.isNotBlank()
                            ) {
                                Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                            }
                            IconButton(onClick = viewModel::cancelCustomColor) {
                                Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                            }
                        }
                    } else {
                        // Normal color dropdown
                        DropdownField(
                            selected = uiState.color.ifBlank { null },
                            options = uiState.availableColors,
                            onOptionSelected = viewModel::updateColor,
                            label = stringResource(R.string.color),
                            optionLabel = { getLocalizedColorName(it) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Allergies
                if (uiState.showCustomAllergyInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.customAllergyInput,
                            onValueChange = viewModel::updateCustomAllergyInput,
                            label = { Text(stringResource(R.string.enter_custom_allergy)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = viewModel::confirmCustomAllergy,
                            enabled = uiState.customAllergyInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                        }
                        IconButton(onClick = viewModel::cancelCustomAllergy) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    }
                } else {
                    DropdownField(
                        selected = uiState.allergies.ifBlank { null },
                        options = uiState.availableAllergies,
                        onOptionSelected = viewModel::updateAllergies,
                        label = stringResource(R.string.allergies),
                        optionLabel = { it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Medications
                if (uiState.showCustomMedicationInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.customMedicationInput,
                            onValueChange = viewModel::updateCustomMedicationInput,
                            label = { Text(stringResource(R.string.enter_custom_medication)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = viewModel::confirmCustomMedication,
                            enabled = uiState.customMedicationInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                        }
                        IconButton(onClick = viewModel::cancelCustomMedication) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    }
                } else {
                    DropdownField(
                        selected = uiState.medications.ifBlank { null },
                        options = uiState.availableMedications,
                        onOptionSelected = viewModel::updateMedications,
                        label = stringResource(R.string.medications),
                        optionLabel = { it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Behavior Notes
                if (uiState.showCustomBehaviorInput) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedTextField(
                            value = uiState.customBehaviorInput,
                            onValueChange = viewModel::updateCustomBehaviorInput,
                            label = { Text(stringResource(R.string.enter_custom_behavior)) },
                            modifier = Modifier.weight(1f),
                            singleLine = true
                        )
                        IconButton(
                            onClick = viewModel::confirmCustomBehavior,
                            enabled = uiState.customBehaviorInput.isNotBlank()
                        ) {
                            Icon(Icons.Default.Check, contentDescription = stringResource(R.string.confirm))
                        }
                        IconButton(onClick = viewModel::cancelCustomBehavior) {
                            Icon(Icons.Default.Close, contentDescription = stringResource(R.string.cancel))
                        }
                    }
                } else {
                    DropdownField(
                        selected = uiState.behaviorNotes.ifBlank { null },
                        options = uiState.availableBehaviorNotes,
                        onOptionSelected = viewModel::updateBehaviorNotes,
                        label = stringResource(R.string.behavior_notes),
                        optionLabel = { it },
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Comments/Notes
                FormTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = stringResource(R.string.pet_notes),
                    singleLine = false,
                    maxLines = 5
                )

                if (uiState.error != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = uiState.error!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                FormButtons(
                    onSave = viewModel::save,
                    onCancel = onNavigateBack,
                    saveEnabled = !uiState.isLoading
                )
            }
        }
    }
}
