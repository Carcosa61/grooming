package com.petgrooming.manager.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.petgrooming.manager.R

/**
 * A popup showing key pet details: weight, allergies, medication, behavior notes and comments.
 */
@Composable
fun PetDetailsDialog(
    petName: String,
    weight: String?,
    allergies: String?,
    medications: String?,
    behaviorNotes: String?,
    notes: String?,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(text = "$petName — ${stringResource(R.string.pet_details)}")
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                val hasAny = !weight.isNullOrBlank() || !allergies.isNullOrBlank() ||
                    !medications.isNullOrBlank() || !behaviorNotes.isNullOrBlank() ||
                    !notes.isNullOrBlank()
                if (!hasAny) {
                    Text(stringResource(R.string.no_details_available))
                } else {
                    DetailRow(stringResource(R.string.weight), weight)
                    DetailRow(stringResource(R.string.allergies), allergies)
                    DetailRow(stringResource(R.string.medications), medications)
                    DetailRow(stringResource(R.string.behavior_notes), behaviorNotes)
                    DetailRow(stringResource(R.string.pet_notes), notes)
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.confirm))
            }
        }
    )
}

@Composable
private fun DetailRow(label: String, value: String?) {
    if (value.isNullOrBlank()) return
    Row(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold
        )
        Text(
            text = " ",
            modifier = Modifier.width(4.dp)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
