package com.petgrooming.manager.ui.feature.owners

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.ui.components.PetAvatar
import com.petgrooming.manager.ui.feature.bookings.petTypeLabel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OwnerDetailScreen(
    onNavigateBack: () -> Unit,
    onEditOwner: (Long) -> Unit,
    onAddPet: (Long) -> Unit,
    onPetClick: (Long) -> Unit,
    onBookPet: (Long) -> Unit,
    viewModel: OwnerDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val owner = uiState.owner

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(owner?.name ?: stringResource(R.string.owner_details)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel)
                        )
                    }
                },
                actions = {
                    if (owner != null) {
                        IconButton(onClick = { onEditOwner(owner.id) }) {
                            Icon(Icons.Default.Edit, contentDescription = stringResource(R.string.edit_owner))
                        }
                    }
                }
            )
        }
    ) { paddingValues ->
        when {
            uiState.isLoading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
            owner == null -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.owner_not_found))
                }
            }
            else -> {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    item {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(Modifier.padding(16.dp)) {
                                Text(
                                    text = owner.name,
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(Modifier.height(8.dp))
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        Icons.Default.Phone,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Spacer(Modifier.width(8.dp))
                                    Text(owner.mobileNumber, style = MaterialTheme.typography.bodyLarge)
                                }
                                owner.email?.takeIf { it.isNotBlank() }?.let { email ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = email,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                owner.lineId?.takeIf { it.isNotBlank() }?.let { lineId ->
                                    Spacer(Modifier.height(4.dp))
                                    Text(
                                        text = stringResource(R.string.line_id_value, lineId),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }

                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = stringResource(R.string.pets_count, uiState.pets.size),
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Button(onClick = { onAddPet(owner.id) }) {
                                Icon(Icons.Default.Add, contentDescription = null)
                                Spacer(Modifier.width(4.dp))
                                Text(stringResource(R.string.add_pet))
                            }
                        }
                    }

                    if (uiState.pets.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.no_pets_for_owner),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        items(uiState.pets, key = { it.id }) { pet ->
                            OwnerPetRow(
                                pet = pet,
                                onClick = { onPetClick(pet.id) },
                                onBook = { onBookPet(pet.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun OwnerPetRow(
    pet: PetEntity,
    onClick: () -> Unit,
    onBook: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(name = pet.name, photoUri = pet.photoUri, size = 44)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(pet.name, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = petTypeLabel(pet.petType),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            OutlinedButton(onClick = onBook) {
                Icon(Icons.Default.Pets, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text(stringResource(R.string.book))
            }
        }
    }
}
