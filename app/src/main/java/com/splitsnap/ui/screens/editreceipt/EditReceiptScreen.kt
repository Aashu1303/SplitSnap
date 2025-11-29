package com.splitsnap.ui.screens.editreceipt

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.splitsnap.domain.model.Person
import com.splitsnap.ui.components.*
import com.splitsnap.ui.theme.*
import com.splitsnap.viewmodel.EditReceiptViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditReceiptScreen(
    viewModel: EditReceiptViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToSummary: (String) -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
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
                            text = receipt?.storeName ?: "Edit Receipt",
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
                            contentDescription = "Back"
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
                        text = "Items",
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
                text = "Splitting with",
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
                    Text("Add")
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
                                Text("Create New Person", color = Primary)
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
                        contentDescription = "Remove",
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
                    text = "$selectedCount item${if (selectedCount > 1) "s" else ""} selected",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                
                TextButton(onClick = onClearSelection) {
                    Text("Clear")
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Assign to:",
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
                    text = "Total",
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
                    text = "View Split Summary",
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
