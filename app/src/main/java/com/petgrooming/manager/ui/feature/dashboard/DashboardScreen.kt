package com.petgrooming.manager.ui.feature.dashboard

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.ui.theme.StatusColors
import java.time.format.DateTimeFormatter
import java.time.LocalDate

import androidx.compose.runtime.LaunchedEffect

@Composable
fun DashboardScreen(
    onNavigateToBookings: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToBookingEdit: (Long) -> Unit = {},
    viewModel: DashboardViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    
    // Refresh data when screen becomes visible
    LaunchedEffect(Unit) {
        viewModel.loadDashboardData()
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                DashboardContent(
                    uiState = uiState,
                    onNavigateToBookings = onNavigateToBookings,
                    onNavigateToPets = onNavigateToPets,
                    onBookingLongPress = onNavigateToBookingEdit
                )
            }
        }
    }
}

@Composable
private fun DashboardContent(
    uiState: DashboardUiState,
    onNavigateToBookings: () -> Unit,
    onNavigateToPets: () -> Unit,
    onBookingLongPress: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_dashboard),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            RebookingStatusCards(
                dueSoonCount = uiState.dueSoonCount,
                dueTodayCount = uiState.dueTodayCount,
                overdueCount = uiState.overdueCount
            )
        }

        item {
            Text(
                text = stringResource(R.string.todays_bookings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.todaysBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_bookings_today),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(uiState.todaysBookings) { booking ->
                BookingCard(
                    booking = booking,
                    onLongPress = { onBookingLongPress(booking.id) }
                )
            }
        }

        // Upcoming Bookings section (next 7 days)
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.upcoming_bookings),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold
            )
        }

        if (uiState.upcomingBookings.isEmpty()) {
            item {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Text(
                        text = stringResource(R.string.no_upcoming_bookings),
                        modifier = Modifier.padding(16.dp),
                        style = MaterialTheme.typography.bodyLarge
                    )
                }
            }
        } else {
            items(uiState.upcomingBookings) { booking ->
                UpcomingBookingCard(
                    booking = booking,
                    onLongPress = { onBookingLongPress(booking.id) }
                )
            }
        }
    }
}

@Composable
private fun RebookingStatusCards(
    dueSoonCount: Int,
    dueTodayCount: Int,
    overdueCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StatusCard(
            title = stringResource(R.string.rebooking_due_soon),
            count = dueSoonCount,
            containerColor = StatusColors.Scheduled,
            modifier = Modifier.weight(1f)
        )
        StatusCard(
            title = stringResource(R.string.rebooking_due_today),
            count = dueTodayCount,
            containerColor = StatusColors.CheckedIn,
            modifier = Modifier.weight(1f)
        )
        StatusCard(
            title = stringResource(R.string.rebooking_overdue),
            count = overdueCount,
            containerColor = StatusColors.Cancelled,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StatusCard(
    title: String,
    count: Int,
    containerColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = containerColor)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun BookingCard(
    booking: BookingWithDetails,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val serviceLabel = getServiceTypeLabel(booking.serviceType)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongPress
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "${booking.petName} (${booking.ownerName})",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.time)}: ${booking.appointmentTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.service)}: $serviceLabel",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.status)}: ${booking.status.name.replace("_", " ")}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun UpcomingBookingCard(
    booking: UpcomingBookingWithDetails,
    onLongPress: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dateFormatter = DateTimeFormatter.ofPattern("EEE, d MMM")
    val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    val serviceLabel = getServiceTypeLabel(booking.serviceType)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                onClick = { },
                onLongClick = onLongPress
            ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "${booking.petName} (${booking.ownerName})",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = booking.appointmentDate.format(dateFormatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "${stringResource(R.string.time)}: ${booking.appointmentTime.format(timeFormatter)}",
                style = MaterialTheme.typography.bodyMedium
            )
            Text(
                text = "${stringResource(R.string.service)}: $serviceLabel",
                style = MaterialTheme.typography.bodyMedium
            )
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
