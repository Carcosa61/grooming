package com.petgrooming.manager.ui.feature.bookings

import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.ui.components.PetAvatar
import com.petgrooming.manager.ui.components.StatusBadge
import com.petgrooming.manager.ui.components.bookingStatusColor
import com.petgrooming.manager.ui.feature.dashboard.BookingWithDetails
import java.time.LocalDate
import java.time.format.DateTimeFormatter

@Composable
fun BookingsScreen(
    onNavigateToBookingDetail: (Long) -> Unit,
    onNavigateToCreateBooking: (LocalDate) -> Unit,
    viewModel: BookingsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { onNavigateToCreateBooking(uiState.selectedDate) },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.create_booking)
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                uiState.error != null -> {
                    Text(
                        text = uiState.error ?: "",
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.align(Alignment.Center)
                    )
                }
                else -> {
                    BookingsContent(
                        uiState = uiState,
                        onBookingClick = onNavigateToBookingDetail,
                        onPreviousDay = { viewModel.goToPreviousDay() },
                        onNextDay = { viewModel.goToNextDay() }
                    )
                }
            }
        }
    }
}

@Composable
private fun BookingsContent(
    uiState: BookingsUiState,
    onBookingClick: (Long) -> Unit,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")
    var dragOffset by remember { mutableFloatStateOf(0f) }
    val swipeThreshold = 100f

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
            .pointerInput(Unit) {
                detectHorizontalDragGestures(
                    onDragEnd = {
                        if (dragOffset < -swipeThreshold) {
                            onNextDay()
                        } else if (dragOffset > swipeThreshold) {
                            onPreviousDay()
                        }
                        dragOffset = 0f
                    },
                    onHorizontalDrag = { _, dragAmount ->
                        dragOffset += dragAmount
                    }
                )
            },
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_bookings),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onPreviousDay) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = stringResource(R.string.previous_day)
                    )
                }
                Text(
                    text = uiState.selectedDate.format(dateFormatter),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                IconButton(onClick = onNextDay) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = stringResource(R.string.next_day)
                    )
                }
            }
        }

        if (uiState.bookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_bookings_for_date),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(uiState.bookings) { booking ->
                BookingListItem(
                    booking = booking,
                    onClick = { onBookingClick(booking.id) }
                )
            }
        }
    }
}

@Composable
private fun BookingListItem(
    booking: BookingWithDetails,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val serviceLabel = getServiceTypeLabel(booking.serviceType)
    val accent = bookingStatusColor(booking.status)

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .size(width = 5.dp, height = 96.dp)
                    .background(accent)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                PetAvatar(name = booking.petName, photoUri = booking.photoUri, size = 48)
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = booking.petName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = booking.ownerName,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = null,
                            modifier = Modifier.size(14.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = booking.appointmentTime.format(timeFormatter),
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = "\u2022",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = serviceLabel,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    StatusBadge(status = booking.status)
                }
            }
        }
    }
}

@Composable
private fun getServiceTypeLabel(serviceType: ServiceType): String {
    return when (serviceType) {
        ServiceType.BATH -> stringResource(R.string.service_bath)
        ServiceType.BATH_AND_DRY -> stringResource(R.string.service_bath_dry)
        ServiceType.FULL_GROOM -> stringResource(R.string.service_full_groom)
        ServiceType.NAIL_TRIM -> stringResource(R.string.service_nail_trim)
        ServiceType.EAR_CLEANING -> stringResource(R.string.service_ear_cleaning)
        ServiceType.CUSTOM -> stringResource(R.string.service_custom)
    }
}
