package com.splitsnap.ui.screens.editreceipt

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.splitsnap.domain.model.Person
import com.splitsnap.ui.components.AddPersonDialog
import com.splitsnap.ui.components.Avatar
import com.splitsnap.ui.components.AvatarSmall
import com.splitsnap.ui.components.ItemCard
import com.splitsnap.ui.components.formatPrice
import com.splitsnap.ui.theme.Primary
import com.splitsnap.ui.theme.PrimaryContainer
import com.splitsnap.viewmodel.EditReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceiptScreen(
    viewModel: EditReceiptViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val receipt = uiState.receipt

    if (uiState.showAddPersonDialog) {
        AddPersonDialog(
            onDismiss = { viewModel.hideAddPersonDialog() },
            onConfirm = { name, relationship ->
                viewModel.createAndAddPerson(name, relationship)
            }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text(
                            text = receipt?.storeName
                                ?: stringResource(id = com.splitsnap.R.string.edit_receipt_default_title),
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (receipt != null) {
                            Text(
                                text = receipt.date,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = stringResource(id = com.splitsnap.R.string.common_back)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            if (uiState.selectedItemIds.isNotEmpty()) {
                SelectionBottomBar(
                    selectedCount = uiState.selectedItemIds.size,
                    participants = uiState.participants,
                    onAssignTo = { personId ->
                        viewModel.assignItemsToPerson(personId)
                    },
                    onClearSelection = { viewModel.clearSelection() }
                )
            } else {
                BottomActionBar(
                    receipt = receipt,
                    onViewSummary = {
                        receipt?.let { onNavigateToSummary(it.id) }
                    }
                )
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        if (uiState.isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Participants section
                item {
                    ParticipantsSection(
                        participants = uiState.participants,
                        allPeople = uiState.allPeople,
                        onAddPerson = { viewModel.showAddPersonDialog() },
                        onAddExistingPerson = { personId ->
                            viewModel.addParticipant(personId)
                        },
                        onRemovePerson = { personId ->
                            viewModel.removeParticipant(personId)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = stringResource(id = com.splitsnap.R.string.common_items_label),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                items(uiState.items, key = { it.id }) { item ->
                    ItemCard(
                        item = item,
                        participants = uiState.participants,
                        isSelected = uiState.selectedItemIds.contains(item.id),
                        isExpanded = uiState.expandedItemId == item.id,
                        onSelectToggle = { viewModel.toggleItemSelection(item.id) },
                        onExpandToggle = { viewModel.toggleItemExpanded(item.id) },
                        onAssignmentChange = { personId, quantity ->
                            viewModel.updateItemAssignment(item.id, personId, quantity)
                        }
                    )
                }

                item {
                    Spacer(modifier = Modifier.height(100.dp))
                }
            }
        }
    }
}

@Composable
private fun ParticipantsSection(
    participants: List<Person>,
    allPeople: List<Person>,
    onAddPerson: () -> Unit,
    onAddExistingPerson: (String) -> Unit,
    onRemovePerson: (String) -> Unit
) {
    var showDropdown by remember { mutableStateOf(false) }
    val availablePeople = allPeople.filter { person ->
        participants.none { it.id == person.id }
    }

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(id = com.splitsnap.R.string.edit_receipt_splitting_with),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Box {
                TextButton(onClick = { showDropdown = true }) {
                    Icon(
                        imageVector = Icons.Default.PersonAdd,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(stringResource(id = com.splitsnap.R.string.common_add))
                }

                DropdownMenu(
                    expanded = showDropdown,
                    onDismissRequest = { showDropdown = false }
                ) {
                    if (availablePeople.isNotEmpty()) {
                        availablePeople.forEach { person ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        AvatarSmall(person = person)
                                        Spacer(modifier = Modifier.width(12.dp))
                                        Column {
                                            Text(person.name)
                                            person.relationship?.let {
                                                Text(
                                                    text = it,
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                },
                                onClick = {
                                    onAddExistingPerson(person.id)
                                    showDropdown = false
                                }
                            )
                        }
                        HorizontalDivider()
                    }

                    DropdownMenuItem(
                        text = {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.PersonAdd,
                                    contentDescription = null,
                                    tint = Primary
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Text(
                                    text = stringResource(id = com.splitsnap.R.string.edit_receipt_create_new_person),
                                    color = Primary
                                )
                            }
                        },
                        onClick = {
                            showDropdown = false
                            onAddPerson()
                        }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        LazyRow(
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(participants, key = { it.id }) { person ->
                ParticipantChip(
                    person = person,
                    onRemove = if (!person.isMe) {
                        { onRemovePerson(person.id) }
                    } else null
                )
            }
        }
    }
}

@Composable
private fun ParticipantChip(
    person: Person,
    onRemove: (() -> Unit)?
) {
    Surface(
        shape = RoundedCornerShape(24.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier.padding(start = 4.dp, end = if (onRemove != null) 4.dp else 12.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AvatarSmall(person = person)
            
            Spacer(modifier = Modifier.width(8.dp))
            
            Column {
                Text(
                    text = person.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium
                )
                person.relationship?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (onRemove != null) {
                Spacer(modifier = Modifier.width(4.dp))
                
                IconButton(
                    onClick = onRemove,
                    modifier = Modifier.size(24.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = com.splitsnap.R.string.participant_remove),
                        modifier = Modifier.size(16.dp),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun SelectionBottomBar(
    selectedCount: Int,
    participants: List<Person>,
    onAssignTo: (String) -> Unit,
    onClearSelection: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = PrimaryContainer,
        shadowElevation = 8.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = buildString {
                        append(selectedCount)
                        append(" ")
                        append(
                            if (selectedCount > 1)
                                stringResource(id = com.splitsnap.R.string.common_item_plural)
                            else
                                stringResource(id = com.splitsnap.R.string.common_item_singular)
                        )
                        append(" ")
                        append(stringResource(id = com.splitsnap.R.string.common_selected_suffix))
                    },
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = onClearSelection) {
                    Text(stringResource(id = com.splitsnap.R.string.common_clear))
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = stringResource(id = com.splitsnap.R.string.common_assign_to),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(participants) { person ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.clickable { onAssignTo(person.id) }
                    ) {
                        Avatar(person = person, size = 48.dp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = person.name,
                            style = MaterialTheme.typography.labelSmall
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BottomActionBar(
    receipt: com.splitsnap.domain.model.Receipt?,
    onViewSummary: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 8.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = stringResource(id = com.splitsnap.R.string.common_total),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = receipt?.let { formatPrice(it.total) } ?: "$0.00",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            Button(
                onClick = onViewSummary,
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(horizontal = 24.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(id = com.splitsnap.R.string.common_view_split_summary),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = null
                )
            }
        }
    }
}
