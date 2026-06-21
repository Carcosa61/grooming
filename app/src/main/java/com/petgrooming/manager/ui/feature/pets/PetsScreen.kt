package com.petgrooming.manager.ui.feature.pets

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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SearchOff
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.petgrooming.manager.R
import com.petgrooming.manager.data.local.entity.PetEntity
import com.petgrooming.manager.ui.components.DropdownField
import com.petgrooming.manager.ui.components.EmptyState
import com.petgrooming.manager.ui.components.PetAvatar
import com.petgrooming.manager.ui.components.PetDetailsDialog

@Composable
fun PetsScreen(
    onNavigateToPetDetail: (Long) -> Unit,
    onNavigateToCreatePet: () -> Unit,
    viewModel: PetsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = onNavigateToCreatePet,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(R.string.add_pet)
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
                    PetsContent(
                        uiState = uiState,
                        onSearchQueryChange = viewModel::searchPets,
                        onSortChange = viewModel::setSortOrder,
                        onPetClick = onNavigateToPetDetail
                    )
                }
            }
        }
    }
}

@Composable
private fun PetsContent(
    uiState: PetsUiState,
    onSearchQueryChange: (String) -> Unit,
    onSortChange: (PetSortOrder) -> Unit,
    onPetClick: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Text(
                text = stringResource(R.string.nav_pets),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        }

        item {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChange,
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text(stringResource(R.string.search_pets)) },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null
                    )
                },
                singleLine = true
            )
        }

        item {
            DropdownField(
                selected = uiState.sortOrder,
                options = listOf(
                    PetSortOrder.NAME_ASC,
                    PetSortOrder.NAME_DESC,
                    PetSortOrder.LAST_VISIT
                ),
                onOptionSelected = onSortChange,
                label = stringResource(R.string.sort_by),
                optionLabel = { sortOrderLabel(it) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (uiState.pets.isEmpty()) {
            item {
                if (uiState.searchQuery.isNotEmpty()) {
                    EmptyState(
                        icon = Icons.Default.SearchOff,
                        title = stringResource(R.string.empty_no_pets_search_title),
                        subtitle = stringResource(R.string.empty_no_pets_search_subtitle)
                    )
                } else {
                    EmptyState(
                        icon = Icons.Default.Pets,
                        title = stringResource(R.string.empty_no_pets_title),
                        subtitle = stringResource(R.string.empty_no_pets_subtitle)
                    )
                }
            }
        } else {
            items(uiState.pets) { pet ->
                PetListItem(
                    pet = pet,
                    onClick = { onPetClick(pet.id) }
                )
            }
        }
    }
}

@Composable
private fun sortOrderLabel(order: PetSortOrder): String = when (order) {
    PetSortOrder.NAME_ASC -> stringResource(R.string.sort_name_asc)
    PetSortOrder.NAME_DESC -> stringResource(R.string.sort_name_desc)
    PetSortOrder.LAST_VISIT -> stringResource(R.string.sort_last_visit)
}

@Composable
private fun PetListItem(
    pet: PetEntity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showDetails by remember { mutableStateOf(false) }

    if (showDetails) {
        PetDetailsDialog(
            petName = pet.name,
            weight = pet.weight?.let { "$it kg" },
            allergies = pet.allergies,
            medications = pet.medications,
            behaviorNotes = pet.behaviorNotes,
            notes = pet.notes,
            onDismiss = { showDetails = false }
        )
    }

    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            PetAvatar(name = pet.name, photoUri = pet.photoUri, size = 56)

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = pet.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = pet.breed,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (pet.behaviorNotes != null) {
                    Text(
                        text = pet.behaviorNotes,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
                TextButton(
                    onClick = { showDetails = true },
                    contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)
                ) {
                    Text(stringResource(R.string.pet_details))
                }
            }
        }
    }
}
