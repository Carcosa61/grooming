package com.petgrooming.manager.ui.feature.bookings

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.data.local.entity.PaymentStatus
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.ui.components.DatePickerField
import com.petgrooming.manager.ui.components.DropdownField
import com.petgrooming.manager.ui.components.FormButtons
import com.petgrooming.manager.ui.components.FormTextField
import com.petgrooming.manager.ui.components.PetAvatar
import com.petgrooming.manager.ui.components.PhotoPickerField
import com.petgrooming.manager.ui.components.TimePickerField
import com.petgrooming.manager.ui.components.UnsavedChangesDialog
import com.petgrooming.manager.ui.components.detectExitSwipe
import com.petgrooming.manager.ui.theme.StatusColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookingFormScreen(
    onNavigateBack: () -> Unit,
    onBookingSaved: (Long) -> Unit,
    onAddPet: (Long?) -> Unit,
    onEditPet: (Long) -> Unit,
    viewModel: BookingFormViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val attemptExit: () -> Unit = {
        if (viewModel.hasUnsavedChanges()) viewModel.showUnsavedDialog() else onNavigateBack()
    }

    // Intercept system back / back-swipe gesture to confirm unsaved changes.
    BackHandler(enabled = true) { attemptExit() }

    LaunchedEffect(uiState.isSaved) {
        if (uiState.isSaved) {
            onBookingSaved(uiState.savedBookingId)
        }
    }

    LaunchedEffect(uiState.isDeleted) {
        if (uiState.isDeleted) {
            onNavigateBack()
        }
    }

    // Unsaved changes confirmation (triggered by swipe or back)
    if (uiState.showUnsavedDialog) {
        UnsavedChangesDialog(
            onSave = {
                viewModel.dismissUnsavedDialog()
                viewModel.save()
            },
            onDiscard = {
                viewModel.dismissUnsavedDialog()
                onNavigateBack()
            },
            onDismiss = viewModel::dismissUnsavedDialog
        )
    }

    // Delete confirmation dialog
    if (uiState.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = viewModel::hideDeleteConfirmation,
            title = { Text(stringResource(R.string.confirm_delete)) },
            text = { Text(stringResource(R.string.delete_booking_message)) },
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
                        if (uiState.id > 0) stringResource(R.string.booking_edit_title)
                        else stringResource(R.string.booking_title)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = attemptExit) {
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
                    .detectExitSwipe(attemptExit)
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                // Customer (Owner) Selection
                DropdownField(
                    selected = uiState.selectedOwner,
                    options = uiState.owners,
                    onOptionSelected = viewModel::updateOwner,
                    label = stringResource(R.string.select_owner),
                    optionLabel = { "${it.name} (${it.mobileNumber})" },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Pet Selection (filtered by selected customer)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    DropdownField(
                        selected = uiState.selectedPet,
                        options = uiState.availablePets,
                        onOptionSelected = viewModel::updatePet,
                        label = stringResource(R.string.select_pet),
                        optionLabel = { it.pet.name },
                        modifier = Modifier.weight(1f)
                    )
                    val selectedPhotoUri = uiState.selectedPet?.pet?.photoUri
                    if (selectedPhotoUri != null) {
                        Spacer(modifier = Modifier.width(8.dp))
                        PetAvatar(
                            name = uiState.selectedPet?.pet?.name ?: "",
                            photoUri = selectedPhotoUri,
                            size = 44,
                            modifier = Modifier.clickable {
                                uiState.selectedPet?.pet?.id?.let { onEditPet(it) }
                            }
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable { onAddPet(uiState.selectedOwner?.id) }
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    ) {
                        Icon(Icons.Default.Pets, contentDescription = stringResource(R.string.add_pet))
                        Text(
                            text = stringResource(R.string.add_pet),
                            style = MaterialTheme.typography.labelSmall,
                            textAlign = TextAlign.Center
                        )
                    }
                }
                if (uiState.petError != null) {
                    Text(
                        text = uiState.petError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date and Time
                Row(modifier = Modifier.fillMaxWidth()) {
                    DatePickerField(
                        selectedDate = uiState.appointmentDate,
                        onDateSelected = viewModel::updateDate,
                        label = stringResource(R.string.appointment_date),
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    TimePickerField(
                        selectedTime = uiState.appointmentTime,
                        onTimeSelected = viewModel::updateTime,
                        label = stringResource(R.string.appointment_time),
                        modifier = Modifier.weight(1f)
                    )
                }
                if (uiState.dateError != null) {
                    Text(
                        text = uiState.dateError!!,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(start = 16.dp, top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Service Type
                DropdownField(
                    selected = uiState.serviceType,
                    options = ServiceType.entries,
                    onOptionSelected = viewModel::updateServiceType,
                    label = stringResource(R.string.service_type),
                    optionLabel = { serviceType ->
                        when (serviceType) {
                            ServiceType.BATH -> stringResource(R.string.service_bath)
                            ServiceType.BATH_AND_DRY -> stringResource(R.string.service_bath_dry)
                            ServiceType.FULL_GROOM -> stringResource(R.string.service_full_groom)
                            ServiceType.NAIL_TRIM -> stringResource(R.string.service_nail_trim)
                            ServiceType.EAR_CLEANING -> stringResource(R.string.service_ear_cleaning)
                            ServiceType.CUSTOM -> stringResource(R.string.service_custom)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Price
                FormTextField(
                    value = uiState.priceInput,
                    onValueChange = viewModel::updatePrice,
                    label = stringResource(R.string.price),
                    keyboardType = KeyboardType.Decimal,
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Payment status
                DropdownField(
                    selected = uiState.paymentStatus,
                    options = PaymentStatus.entries,
                    onOptionSelected = viewModel::updatePaymentStatus,
                    label = stringResource(R.string.payment_status),
                    optionLabel = { status ->
                        when (status) {
                            PaymentStatus.UNPAID -> stringResource(R.string.payment_unpaid)
                            PaymentStatus.PAID -> stringResource(R.string.payment_paid)
                            PaymentStatus.REFUNDED -> stringResource(R.string.payment_refunded)
                        }
                    }
                )

                // Status - only show when editing
                if (uiState.id > 0) {
                    Spacer(modifier = Modifier.height(16.dp))

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(StatusColors.Scheduled.copy(alpha = 0.12f))
                            .padding(8.dp)
                    ) {
                        DropdownField(
                            selected = uiState.status,
                            options = BookingStatus.entries,
                            onOptionSelected = viewModel::updateStatus,
                            label = stringResource(R.string.status),
                            optionLabel = { status ->
                                when (status) {
                                    BookingStatus.SCHEDULED -> stringResource(R.string.status_scheduled)
                                    BookingStatus.COMPLETED -> stringResource(R.string.status_completed)
                                    BookingStatus.CANCELLED -> stringResource(R.string.status_cancelled)
                                    BookingStatus.NO_SHOW -> stringResource(R.string.status_no_show)
                                }
                            },
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Notes
                FormTextField(
                    value = uiState.notes,
                    onValueChange = viewModel::updateNotes,
                    label = stringResource(R.string.notes),
                    singleLine = false,
                    maxLines = 4
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Grooming photos (before / after)
                Text(
                    text = stringResource(R.string.grooming_photos),
                    style = MaterialTheme.typography.titleSmall
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    PhotoPickerField(
                        label = stringResource(R.string.photo_before),
                        photoUri = uiState.beforePhotoUri,
                        onPhotoSelected = viewModel::onBeforePhotoSelected,
                        onPhotoRemoved = viewModel::onBeforePhotoRemoved,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(16.dp))
                    PhotoPickerField(
                        label = stringResource(R.string.photo_after),
                        photoUri = uiState.afterPhotoUri,
                        onPhotoSelected = viewModel::onAfterPhotoSelected,
                        onPhotoRemoved = viewModel::onAfterPhotoRemoved,
                        modifier = Modifier.weight(1f)
                    )
                }

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
