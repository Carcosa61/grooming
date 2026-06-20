package com.petgrooming.manager.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCut
import androidx.compose.material.icons.filled.EventBusy
import androidx.compose.material.icons.filled.HowToReg
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.BookingStatus
import com.petgrooming.manager.ui.theme.StatusColors
import kotlin.math.absoluteValue

/**
 * A palette of pleasant, distinct colors used to give each pet/owner a
 * consistent avatar background derived from their name.
 */
private val AvatarColors = listOf(
    Color(0xFF00897B), // Teal
    Color(0xFFFF7043), // Orange
    Color(0xFF7E57C2), // Purple
    Color(0xFF42A5F5), // Blue
    Color(0xFF66BB6A), // Green
    Color(0xFFEC407A), // Pink
    Color(0xFFAB47BC), // Violet
    Color(0xFFFFA726), // Amber
    Color(0xFF26A69A), // Sea green
    Color(0xFF5C6BC0)  // Indigo
)

/** Deterministically maps a name to a stable avatar color. */
fun avatarColorFor(name: String): Color {
    if (name.isBlank()) return AvatarColors.first()
    val index = name.fold(0) { acc, c -> acc + c.code }.absoluteValue % AvatarColors.size
    return AvatarColors[index]
}

/**
 * Circular avatar showing the pet's photo if available, otherwise a colored
 * circle with the pet's initial. Color is derived from the name for consistency.
 */
@Composable
fun PetAvatar(
    name: String,
    photoUri: String? = null,
    size: Int = 48,
    modifier: Modifier = Modifier
) {
    val bgColor = avatarColorFor(name)
    if (photoUri != null) {
        AsyncImage(
            model = photoModel(photoUri),
            contentDescription = name,
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape),
            contentScale = ContentScale.Crop
        )
    } else {
        Box(
            modifier = modifier
                .size(size.dp)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = name.firstOrNull()?.uppercase() ?: "?",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = (size * 0.42f).sp
            )
        }
    }
}

/** Returns the brand color associated with a booking status. */
fun bookingStatusColor(status: BookingStatus): Color = when (status) {
    BookingStatus.SCHEDULED -> StatusColors.Scheduled
    BookingStatus.CHECKED_IN -> StatusColors.CheckedIn
    BookingStatus.GROOMING -> StatusColors.Grooming
    BookingStatus.READY_FOR_COLLECTION -> StatusColors.ReadyForCollection
    BookingStatus.COMPLETED -> StatusColors.Completed
    BookingStatus.CANCELLED -> StatusColors.Cancelled
    BookingStatus.NO_SHOW -> StatusColors.NoShow
}

/** Returns a representative icon for a booking status. */
fun bookingStatusIcon(status: BookingStatus): ImageVector = when (status) {
    BookingStatus.SCHEDULED -> Icons.Default.Schedule
    BookingStatus.CHECKED_IN -> Icons.Default.HowToReg
    BookingStatus.GROOMING -> Icons.Default.ContentCut
    BookingStatus.READY_FOR_COLLECTION -> Icons.Default.Notifications
    BookingStatus.COMPLETED -> Icons.Default.CheckCircle
    BookingStatus.CANCELLED -> Icons.Default.Close
    BookingStatus.NO_SHOW -> Icons.Default.EventBusy
}

/** Localized label for a booking status. */
@Composable
fun bookingStatusLabel(status: BookingStatus): String = when (status) {
    BookingStatus.SCHEDULED -> stringResource(R.string.status_scheduled)
    BookingStatus.CHECKED_IN -> stringResource(R.string.status_checked_in)
    BookingStatus.GROOMING -> stringResource(R.string.status_grooming)
    BookingStatus.READY_FOR_COLLECTION -> stringResource(R.string.status_ready_for_collection)
    BookingStatus.COMPLETED -> stringResource(R.string.status_completed)
    BookingStatus.CANCELLED -> stringResource(R.string.status_cancelled)
    BookingStatus.NO_SHOW -> stringResource(R.string.status_no_show)
}

/** A colored pill badge with an icon representing a booking status. */
@Composable
fun StatusBadge(
    status: BookingStatus,
    modifier: Modifier = Modifier
) {
    val color = bookingStatusColor(status)
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(color.copy(alpha = 0.15f))
            .padding(horizontal = 10.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Icon(
            imageVector = bookingStatusIcon(status),
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(14.dp)
        )
        Text(
            text = bookingStatusLabel(status),
            style = MaterialTheme.typography.labelMedium,
            color = color,
            fontWeight = FontWeight.SemiBold
        )
    }
}

/**
 * A friendly, illustrated empty-state with an icon, title, subtitle and an
 * optional call-to-action button.
 */
@Composable
fun EmptyState(
    icon: ImageVector,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Box(
            modifier = Modifier
                .size(72.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.10f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(36.dp)
            )
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
        if (actionLabel != null && onAction != null) {
            Spacer(modifier = Modifier.height(4.dp))
            Button(onClick = onAction) {
                Text(actionLabel)
            }
        }
    }
}

/** An animated shimmer brush for skeleton loading placeholders. */
@Composable
fun shimmerBrush(): Brush {
    val transition = rememberInfiniteTransition(label = "shimmer")
    val translate by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1200),
            repeatMode = RepeatMode.Restart
        ),
        label = "shimmerTranslate"
    )
    val base = MaterialTheme.colorScheme.surfaceVariant
    val colors = listOf(
        base.copy(alpha = 0.6f),
        base.copy(alpha = 0.2f),
        base.copy(alpha = 0.6f)
    )
    return Brush.linearGradient(
        colors = colors,
        start = androidx.compose.ui.geometry.Offset(translate - 500f, 0f),
        end = androidx.compose.ui.geometry.Offset(translate, 0f)
    )
}

/** A single skeleton row used while booking lists are loading. */
@Composable
fun BookingSkeletonRow(modifier: Modifier = Modifier) {
    val brush = shimmerBrush()
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(brush)
        )
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .height(14.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
            Box(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(12.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(brush)
            )
        }
    }
}

/** A vertical stack of skeleton rows for loading states. */
@Composable
fun BookingSkeletonList(
    count: Int = 3,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(16.dp)
) {
    Column(
        modifier = modifier.padding(contentPadding),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        repeat(count) {
            BookingSkeletonRow()
        }
    }
}
