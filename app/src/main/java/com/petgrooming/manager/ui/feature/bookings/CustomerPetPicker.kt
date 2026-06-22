package com.petgrooming.manager.ui.feature.bookings

import android.net.Uri
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.Gender
import com.petgrooming.manager.data.local.entity.OwnerEntity
import com.petgrooming.manager.data.local.entity.PetType
import com.petgrooming.manager.ui.components.AvatarPhotoPicker
import com.petgrooming.manager.ui.components.DatePickerField
import com.petgrooming.manager.ui.components.DropdownField
import com.petgrooming.manager.ui.components.FormTextField
import com.petgrooming.manager.ui.components.PetAvatar
import java.time.LocalDate

@Composable
fun petTypeLabel(type: PetType): String = when (type) {
    PetType.DOG -> stringResource(R.string.pet_type_dog)
    PetType.CAT -> stringResource(R.string.pet_type_cat)
    PetType.OTHER -> stringResource(R.string.pet_type_other)
}

/**
 * A single searchable picker for choosing a pet (and, implicitly, its owner),
 * grouped by customer, with quick actions to add a pet or create a new customer.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomerPetPickerSheet(
    uiState: BookingFormState,
    onQueryChange: (String) -> Unit,
    onSelectPet: (PetWithOwner) -> Unit,
    onAddPetForOwner: (OwnerEntity) -> Unit,
    onNewCustomer: () -> Unit,
    onOpenOwnerDetail: (Long) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    val query = uiState.pickerQuery.trim()
    val filtered = if (query.isBlank()) {
        uiState.pets
    } else {
        uiState.pets.filter {
            it.pet.name.contains(query, ignoreCase = true) ||
                it.ownerName.contains(query, ignoreCase = true) ||
                it.ownerPhone.contains(query, ignoreCase = true)
        }
    }
    // Group pets by owner, preserving the order they appear in.
    val groups = filtered.groupBy { it.pet.ownerId }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp)
        ) {
            Text(
                text = stringResource(R.string.select_customer_pet),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(12.dp))

            OutlinedTextField(
                value = uiState.pickerQuery,
                onValueChange = onQueryChange,
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                placeholder = { Text(stringResource(R.string.search_hint)) }
            )
            Spacer(Modifier.height(8.dp))

            // New customer call-to-action
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onNewCustomer() }
                    .padding(vertical = 12.dp, horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.PersonAdd,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = stringResource(R.string.new_customer_and_pet),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }

            HorizontalDivider()

            if (groups.isEmpty()) {
                Spacer(Modifier.height(24.dp))
                Text(
                    text = if (uiState.pets.isEmpty()) {
                        stringResource(R.string.no_customers_yet)
                    } else {
                        stringResource(R.string.no_matches_found)
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 420.dp)
                ) {
                    groups.forEach { (ownerId, pets) ->
                        val ownerName = pets.first().ownerName
                        val ownerPhone = pets.first().ownerPhone
                        item(key = "owner_$ownerId") {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onOpenOwnerDetail(ownerId) }
                                    .padding(vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = ownerName,
                                        style = MaterialTheme.typography.titleSmall,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = ownerPhone,
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                Icon(
                                    Icons.Default.ChevronRight,
                                    contentDescription = stringResource(R.string.owner_details),
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        items(pets, key = { "pet_${it.pet.id}" }) { petWithOwner ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable { onSelectPet(petWithOwner) }
                                    .padding(start = 8.dp, top = 6.dp, bottom = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                PetAvatar(
                                    name = petWithOwner.pet.name,
                                    photoUri = petWithOwner.pet.photoUri,
                                    size = 40
                                )
                                Spacer(Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = petWithOwner.pet.name,
                                        style = MaterialTheme.typography.bodyLarge
                                    )
                                    Text(
                                        text = petTypeLabel(petWithOwner.pet.petType),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                        item(key = "addpet_$ownerId") {
                            TextButton(
                                onClick = {
                                    onAddPetForOwner(
                                        OwnerEntity(
                                            id = ownerId,
                                            name = ownerName,
                                            mobileNumber = ownerPhone
                                        )
                                    )
                                },
                                modifier = Modifier.padding(start = 4.dp)
                            ) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.add_pet))
                            }
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

/**
 * Bottom sheet to capture the minimum information needed to create a pet
 * (and, for a brand-new customer, the owner too) without leaving the booking form.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InlineCreateSheet(
    uiState: BookingFormState,
    onOwnerNameChange: (String) -> Unit,
    onOwnerMobileChange: (String) -> Unit,
    onOwnerLineIdChange: (String) -> Unit,
    onPetNameChange: (String) -> Unit,
    onPetTypeChange: (PetType) -> Unit,
    onPhotoSelected: (Uri) -> Unit,
    onPhotoRemoved: () -> Unit,
    onToggleMoreDetails: () -> Unit,
    onBreedChange: (String) -> Unit,
    onDateOfBirthChange: (LocalDate) -> Unit,
    onGenderChange: (Gender) -> Unit,
    onWeightChange: (String) -> Unit,
    onColorChange: (String) -> Unit,
    onAllergiesChange: (String) -> Unit,
    onMedicationsChange: (String) -> Unit,
    onBehaviorNotesChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isNewCustomer = uiState.createForOwner == null

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = if (isNewCustomer) {
                    stringResource(R.string.new_customer_and_pet)
                } else {
                    stringResource(R.string.add_pet_for, uiState.createForOwner!!.name)
                },
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            if (isNewCustomer) {
                FormTextField(
                    value = uiState.draftOwnerName,
                    onValueChange = onOwnerNameChange,
                    label = stringResource(R.string.customer_name),
                    isError = uiState.draftOwnerNameError != null,
                    errorMessage = uiState.draftOwnerNameError
                )
                FormTextField(
                    value = uiState.draftOwnerMobile,
                    onValueChange = onOwnerMobileChange,
                    label = stringResource(R.string.mobile_number),
                    keyboardType = KeyboardType.Phone,
                    isError = uiState.draftOwnerMobileError != null,
                    errorMessage = uiState.draftOwnerMobileError
                )
                FormTextField(
                    value = uiState.draftOwnerLineId,
                    onValueChange = onOwnerLineIdChange,
                    label = stringResource(R.string.line_id_optional)
                )
            }

            AvatarPhotoPicker(
                name = uiState.draftPetName.ifBlank { "?" },
                photoUri = uiState.draftPhotoUri,
                onPhotoSelected = onPhotoSelected,
                onPhotoRemoved = onPhotoRemoved
            )

            FormTextField(
                value = uiState.draftPetName,
                onValueChange = onPetNameChange,
                label = stringResource(R.string.pet_name),
                isError = uiState.draftPetNameError != null,
                errorMessage = uiState.draftPetNameError
            )

            DropdownField(
                selected = uiState.draftPetType,
                options = PetType.entries,
                onOptionSelected = onPetTypeChange,
                label = stringResource(R.string.pet_type),
                optionLabel = { petTypeLabel(it) },
                modifier = Modifier.fillMaxWidth()
            )

            // Expandable extra details, mirroring the Edit Pet page.
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .clickable { onToggleMoreDetails() }
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.more_details),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.weight(1f)
                )
                Icon(
                    imageVector = if (uiState.showMoreDetails) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                    contentDescription = stringResource(R.string.more_details)
                )
            }

            if (uiState.showMoreDetails) {
                FormTextField(
                    value = uiState.draftBreed,
                    onValueChange = onBreedChange,
                    label = stringResource(R.string.pet_breed)
                )

                DatePickerField(
                    selectedDate = uiState.draftDateOfBirth,
                    onDateSelected = onDateOfBirthChange,
                    label = stringResource(R.string.date_of_birth),
                    modifier = Modifier.fillMaxWidth()
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = uiState.draftGender == Gender.MALE,
                        onClick = { onGenderChange(Gender.MALE) },
                        label = { Text(stringResource(R.string.gender_male)) }
                    )
                    FilterChip(
                        selected = uiState.draftGender == Gender.FEMALE,
                        onClick = { onGenderChange(Gender.FEMALE) },
                        label = { Text(stringResource(R.string.gender_female)) }
                    )
                }

                FormTextField(
                    value = uiState.draftWeight,
                    onValueChange = onWeightChange,
                    label = stringResource(R.string.weight_kg),
                    keyboardType = KeyboardType.Decimal
                )

                FormTextField(
                    value = uiState.draftColor,
                    onValueChange = onColorChange,
                    label = stringResource(R.string.color)
                )

                FormTextField(
                    value = uiState.draftAllergies,
                    onValueChange = onAllergiesChange,
                    label = stringResource(R.string.allergies)
                )

                FormTextField(
                    value = uiState.draftMedications,
                    onValueChange = onMedicationsChange,
                    label = stringResource(R.string.medications)
                )

                FormTextField(
                    value = uiState.draftBehaviorNotes,
                    onValueChange = onBehaviorNotesChange,
                    label = stringResource(R.string.behavior_notes)
                )

                FormTextField(
                    value = uiState.draftNotes,
                    onValueChange = onNotesChange,
                    label = stringResource(R.string.pet_notes)
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                TextButton(onClick = onDismiss) {
                    Text(stringResource(R.string.cancel))
                }
                Spacer(Modifier.width(8.dp))
                TextButton(onClick = onConfirm) {
                    Text(stringResource(R.string.save))
                }
            }
        }
    }
}
