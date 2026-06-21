package com.petgrooming.manager.ui.feature.dashboard

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.EventAvailable
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.data.local.entity.ServiceType
import com.petgrooming.manager.ui.components.BookingSkeletonList
import com.petgrooming.manager.ui.components.EmptyState
import com.petgrooming.manager.ui.components.PetAvatar
import com.petgrooming.manager.ui.components.StatusBadge
import com.petgrooming.manager.ui.components.bookingStatusColor
import com.petgrooming.manager.ui.theme.StatusColors
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

import androidx.compose.runtime.LaunchedEffect

@Composable
fun DashboardScreen(
    onNavigateToBookings: () -> Unit,
    onNavigateToPets: () -> Unit,
    onNavigateToBookingEdit: (Long) -> Unit = {},
    onNavigateToCreateBooking: () -> Unit = {},
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
                BookingSkeletonList(count = 4, modifier = Modifier.fillMaxWidth())
            }
            uiState.error != null -> {
                Text(
                    text = uiState.error ?: "",
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.align(Alignment.Center)
                )
            }
            else -> {
                AnimatedVisibility(
                    visible = true,
                    enter = fadeIn(),
                    exit = fadeOut()
                ) {
                    DashboardContent(
                        uiState = uiState,
                        onNavigateToBookings = onNavigateToBookings,
                        onNavigateToPets = onNavigateToPets,
                        onBookingLongPress = onNavigateToBookingEdit,
                        onAddBooking = onNavigateToCreateBooking
                    )
                }
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
    onAddBooking: () -> Unit,
    modifier: Modifier = Modifier
) {
    val inSalonCount = uiState.todaysBookings.count {
        it.status == BookingStatus.CHECKED_IN || it.status == BookingStatus.GROOMING
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item { Spacer(modifier = Modifier.height(4.dp)) }

        item {
            HeroHeader(todaysCount = uiState.todaysBookings.size)
        }

        item {
            QuickStatsRow(
                todayCount = uiState.todaysBookings.size,
                upcomingCount = uiState.upcomingBookings.size,
                inSalonCount = inSalonCount
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
            SectionHeader(text = stringResource(R.string.todays_bookings))
        }

        if (uiState.todaysBookings.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.EventAvailable,
                    title = stringResource(R.string.empty_no_bookings_title),
                    subtitle = stringResource(R.string.empty_no_bookings_subtitle),
                    actionLabel = stringResource(R.string.add_booking),
                    onAction = onAddBooking
                )
            }
        } else {
            items(uiState.todaysBookings) { booking ->
                BookingCard(
                    booking = booking,
                    onLongPress = { onBookingLongPress(booking.id) }
                )
            }
        }

        item {
            Spacer(modifier = Modifier.height(4.dp))
            SectionHeader(text = stringResource(R.string.upcoming_bookings))
        }

        if (uiState.upcomingBookings.isEmpty()) {
            item {
                EmptyState(
                    icon = Icons.Default.DateRange,
                    title = stringResource(R.string.empty_no_upcoming_title),
                    subtitle = stringResource(R.string.empty_no_upcoming_subtitle)
                )
            }
        } else {
            items(uiState.upcomingBookings) { booking ->
                UpcomingBookingCard(
                    booking = booking,
                    onLongPress = { onBookingLongPress(booking.id) }
                )
            }
        }

        item { Spacer(modifier = Modifier.height(16.dp)) }
    }
}

@Composable
private fun HeroHeader(
    todaysCount: Int,
    modifier: Modifier = Modifier
) {
    val hour = LocalTime.now().hour
    val greeting = when {
        hour < 12 -> stringResource(R.string.greeting_morning)
        hour < 18 -> stringResource(R.string.greeting_afternoon)
        else -> stringResource(R.string.greeting_evening)
    }
    val dateText = LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, d MMMM"))
    val summary = when (todaysCount) {
        0 -> stringResource(R.string.dashboard_summary_none)
        1 -> stringResource(R.string.dashboard_summary_one)
        else -> stringResource(R.string.dashboard_summary_many, todaysCount)
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            StatusColors.Grooming
                        )
                    )
                )
                .padding(20.dp)
        ) {
            Column {
                Text(
                    text = greeting,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.White.copy(alpha = 0.9f)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = dateText,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
        }
    }
}

@Composable
private fun QuickStatsRow(
    todayCount: Int,
    upcomingCount: Int,
    inSalonCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        QuickStatCard(
            icon = Icons.Default.Event,
            value = todayCount,
            label = stringResource(R.string.stat_today),
            tint = StatusColors.Scheduled,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Default.DateRange,
            value = upcomingCount,
            label = stringResource(R.string.stat_upcoming),
            tint = StatusColors.ReadyForCollection,
            modifier = Modifier.weight(1f)
        )
        QuickStatCard(
            icon = Icons.Default.Pets,
            value = inSalonCount,
            label = stringResource(R.string.stat_in_salon),
            tint = StatusColors.Grooming,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun QuickStatCard(
    icon: ImageVector,
    value: Int,
    label: String,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 18.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(tint.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = tint,
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun SectionHeader(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.SemiBold,
        modifier = modifier
    )
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
        RebookingCard(
            title = stringResource(R.string.rebooking_due_soon),
            count = dueSoonCount,
            icon = Icons.Default.Schedule,
            gradient = listOf(StatusColors.Scheduled, Color(0xFF1565C0)),
            modifier = Modifier.weight(1f)
        )
        RebookingCard(
            title = stringResource(R.string.rebooking_due_today),
            count = dueTodayCount,
            icon = Icons.Default.NotificationsActive,
            gradient = listOf(StatusColors.CheckedIn, Color(0xFF2E7D32)),
            modifier = Modifier.weight(1f)
        )
        RebookingCard(
            title = stringResource(R.string.rebooking_overdue),
            count = overdueCount,
            icon = Icons.Default.Warning,
            gradient = listOf(StatusColors.Cancelled, Color(0xFFC62828)),
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun RebookingCard(
    title: String,
    count: Int,
    icon: ImageVector,
    gradient: List<Color>,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.Transparent)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(gradient))
                .padding(vertical = 16.dp, horizontal = 8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = count.toString(),
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.95f)
                )
            }
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
    val accent = bookingStatusColor(booking.status)

    Card(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(onClick = { }, onLongClick = onLongPress),
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
                            text = "•",
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
            .combinedClickable(onClick = { }, onLongClick = onLongPress),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(name = booking.petName, photoUri = booking.photoUri, size = 44)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = booking.petName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = "${booking.appointmentTime.format(timeFormatter)} • $serviceLabel",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer)
                    .padding(horizontal = 10.dp, vertical = 6.dp)
            ) {
                Text(
                    text = booking.appointmentDate.format(dateFormatter),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold
                )
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
