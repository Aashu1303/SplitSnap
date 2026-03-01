package com.splitsnap.ui.screens.assign

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CallSplit
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.splitsnap.domain.model.Friend
import com.splitsnap.domain.model.GeminiReceipt
import com.splitsnap.domain.model.GeminiReceiptItem
import com.splitsnap.ui.theme.Primary
import java.text.NumberFormat
import java.util.Locale

/**
 * Screen for assigning receipt items to friends
 * Items can be assigned to multiple people (split items)
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssignItemsScreen(
    receipt: GeminiReceipt,
    friends: List<Friend>,
    onNavigateBack: () -> Unit,
    onCalculateSplit: (GeminiReceipt) -> Unit,
    onAddFriend: () -> Unit
) {
    var editedReceipt by remember { mutableStateOf(receipt) }
    var showAddFriendDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Assign Items") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { showAddFriendDialog = true }) {
                        Icon(Icons.Default.PersonAdd, "Add Friend")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // Friends list
            if (friends.isEmpty()) {
                EmptyFriendsState(onAddFriend = { showAddFriendDialog = true })
            } else {
                FriendsList(friends = friends)
            }

            // Items list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item {
                    Text(
                        "Tap friends to assign items",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                items(editedReceipt.items) { item ->
                    AssignableItemCard(
                        item = item,
                        friends = friends,
                        onAssignmentChanged = { updatedItem ->
                            editedReceipt = editedReceipt.copy(
                                items = editedReceipt.items.map {
                                    if (it == item) updatedItem else it
                                }
                            )
                        }
                    )
                }
            }

            // Calculate button
            Button(
                onClick = { onCalculateSplit(editedReceipt) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                enabled = editedReceipt.items.any { it.assignedTo.isNotEmpty() },
                colors = ButtonDefaults.buttonColors(containerColor = Primary),
                shape = RoundedCornerShape(16.dp),
                contentPadding = PaddingValues(vertical = 16.dp)
            ) {
                Icon(Icons.Default.Calculate, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    "Calculate Split",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }

    if (showAddFriendDialog) {
        AddFriendDialog(
            onDismiss = { showAddFriendDialog = false },
            onConfirm = { friendName ->
                onAddFriend()
                showAddFriendDialog = false
            }
        )
    }
}

@Composable
private fun FriendsList(friends: List<Friend>) {
    LazyRow(
        modifier = Modifier.padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        items(friends) { friend ->
            FriendChip(friend = friend)
        }
    }
}

@Composable
private fun FriendChip(friend: Friend) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer
        )
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(Primary),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = friend.name.firstOrNull()?.uppercase() ?: "?",
                    color = Color.White,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

@Composable
private fun AssignableItemCard(
    item: GeminiReceiptItem,
    friends: List<Friend>,
    onAssignmentChanged: (GeminiReceiptItem) -> Unit
) {
    val currencyFormat = remember { NumberFormat.getCurrencyInstance(Locale.US) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Item info
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Qty: ${item.quantity} × ${currencyFormat.format(item.unitPrice)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Text(
                    text = currencyFormat.format(item.totalPrice),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = Primary
                )
            }

            // Assignment chips
            if (friends.isNotEmpty()) {
                Spacer(Modifier.height(12.dp))
                Text(
                    "Assigned to:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(friends) { friend ->
                        val isAssigned = item.assignedTo.contains(friend.id)
                        FriendAssignmentChip(
                            friend = friend,
                            isAssigned = isAssigned,
                            onClick = {
                                val updatedAssignments = if (isAssigned) {
                                    item.assignedTo.filter { it != friend.id }
                                } else {
                                    item.assignedTo + friend.id
                                }
                                onAssignmentChanged(
                                    item.copy(
                                        assignedTo = updatedAssignments,
                                        isSplit = updatedAssignments.size > 1
                                    )
                                )
                            }
                        )
                    }
                }

                // Split indicator
                if (item.isSplit && item.assignedTo.size > 1) {
                    Row(
                        modifier = Modifier
                            .padding(top = 8.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.CallSplit,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = Primary
                        )
                        Spacer(Modifier.width(4.dp))
                        Text(
                            "Split between ${item.assignedTo.size} people (${
                                currencyFormat.format(item.totalPrice / item.assignedTo.size)
                            } each)",
                            style = MaterialTheme.typography.bodySmall,
                            color = Primary
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FriendAssignmentChip(
    friend: Friend,
    isAssigned: Boolean,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isAssigned,
        onClick = onClick,
        label = {
            Text(
                text = friend.name,
                style = MaterialTheme.typography.bodyMedium
            )
        },
        leadingIcon = if (isAssigned) {
            {
                Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
            }
        } else null,
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = Primary,
            selectedLabelColor = Color.White,
            selectedLeadingIconColor = Color.White
        )
    )
}

@Composable
private fun EmptyFriendsState(onAddFriend: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                Icons.Default.PersonAdd,
                contentDescription = null,
                modifier = Modifier.size(48.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            Spacer(Modifier.height(16.dp))
            Text(
                "No friends added yet",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center
            )
            Text(
                "Add friends to split the bill",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = onAddFriend,
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Icon(Icons.Default.PersonAdd, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Add Friend")
            }
        }
    }
}

@Composable
private fun AddFriendDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var friendName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Friend") },
        text = {
            OutlinedTextField(
                value = friendName,
                onValueChange = { friendName = it },
                label = { Text("Friend's name") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = { if (friendName.isNotBlank()) onConfirm(friendName) },
                enabled = friendName.isNotBlank()
            ) {
                Text("Add")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
