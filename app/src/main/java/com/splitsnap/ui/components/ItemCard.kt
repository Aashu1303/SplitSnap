package com.splitsnap.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.MaterialTheme.typography
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.splitsnap.domain.model.Person
import com.splitsnap.domain.model.ReceiptItem
import com.splitsnap.ui.theme.Primary
import com.splitsnap.ui.theme.PrimaryContainer
import com.splitsnap.ui.theme.Success

@Composable
fun ItemCard(
    item: ReceiptItem,
    participants: List<Person>,
    isSelected: Boolean,
    isExpanded: Boolean,
    onSelectToggle: () -> Unit,
    onExpandToggle: () -> Unit,
    onAssignmentChange: (personId: String, quantity: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val isFullyAssigned = item.unassignedQuantity == 0

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onSelectToggle),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = when {
                isSelected -> PrimaryContainer
                isFullyAssigned -> Color(0xFFF0FDF4)
                else -> colorScheme.surface
            }
        ),
        border = if (isSelected) {
            androidx.compose.foundation.BorderStroke(2.dp, Primary)
        } else null,
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Main row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Selection indicator
                Box(
                    modifier = Modifier
                        .size(24.dp)
                        .clip(CircleShape)
                        .background(
                            if (isSelected) Primary
                            else if (isFullyAssigned) Success
                            else colorScheme.surfaceVariant
                        )
                        .border(
                            width = if (!isSelected && !isFullyAssigned) 2.dp else 0.dp,
                            color = colorScheme.outline,
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected || isFullyAssigned) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Item info
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = item.name,
                        style = typography.bodyLarge,
                        fontWeight = FontWeight.Medium,
                        color = colorScheme.onSurface
                    )
                    
                    Spacer(modifier = Modifier.height(2.dp))
                    
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(id = com.splitsnap.R.string.item_card_quantity_prefix) + item.quantity,
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = "${formatPrice(item.price)} ${stringResource(id = com.splitsnap.R.string.item_card_each_suffix)}",
                            style = typography.bodySmall,
                            color = colorScheme.onSurfaceVariant
                        )
                    }
                }

                // Total price
                Column(
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = formatPrice(item.totalPrice),
                        style = typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = Primary
                    )
                    
                    if (item.assignedQuantity > 0) {
                        Text(
                            text = "${item.assignedQuantity}/${item.quantity} ${stringResource(id = com.splitsnap.R.string.item_card_assigned_suffix)}",
                            style = typography.labelSmall,
                            color = if (isFullyAssigned) Success else colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                // Expand button
                IconButton(
                    onClick = onExpandToggle,
                    modifier = Modifier.size(32.dp)
                ) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                        contentDescription = if (isExpanded)
                            stringResource(id = com.splitsnap.R.string.item_card_collapse)
                        else
                            stringResource(id = com.splitsnap.R.string.item_card_expand),
                        tint = colorScheme.onSurfaceVariant
                    )
                }
            }

            // Assigned avatars row
            if (item.assignments.isNotEmpty() && !isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))
                
                Row(
                    horizontalArrangement = Arrangement.spacedBy((-8).dp)
                ) {
                    item.assignments.forEach { (personId, qty) ->
                        val person = participants.find { it.id == personId }
                        person?.let {
                            Box {
                                AvatarSmall(person = person)
                                if (qty > 1) {
                                    Surface(
                                        modifier = Modifier
                                            .align(Alignment.BottomEnd)
                                            .offset(x = 4.dp, y = 4.dp),
                                        shape = CircleShape,
                                        color = Primary
                                    ) {
                                        Text(
                                            text = qty.toString(),
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp),
                                            style = typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // Expanded content
            AnimatedVisibility(
                visible = isExpanded,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    modifier = Modifier.padding(top = 16.dp)
                ) {
                    HorizontalDivider()
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = "Assign to:",
                        style = typography.labelMedium,
                        color = colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.height(8.dp))

                    participants.forEach { person ->
                        val assignedQty = item.getAssignedQuantity(person.id)
                        val maxQty = assignedQty + item.unassignedQuantity

                        PersonAssignmentRow(
                            person = person,
                            quantity = assignedQty,
                            maxQuantity = maxQty,
                            onQuantityChange = { newQty ->
                                onAssignmentChange(person.id, newQty)
                            }
                        )
                        
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
private fun PersonAssignmentRow(
    person: Person,
    quantity: Int,
    maxQuantity: Int,
    onQuantityChange: (Int) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarSmall(person = person)
        
        Spacer(modifier = Modifier.width(12.dp))
        
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = person.name,
                style = typography.bodyMedium,
                fontWeight = FontWeight.Medium
            )
            if (person.relationship != null) {
                Text(
                    text = person.relationship,
                    style = typography.bodySmall,
                    color = colorScheme.onSurfaceVariant
                )
            }
        }

        // Quantity controls
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            IconButton(
                onClick = { if (quantity > 0) onQuantityChange(quantity - 1) },
                enabled = quantity > 0,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp)
                    .background(colorScheme.surfaceVariant)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = stringResource(id = com.splitsnap.R.string.item_card_decrease),
                    modifier = Modifier.size(16.dp)
                )
            }

            Text(
                text = quantity.toString(),
                style = typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.width(24.dp),
                textAlign = TextAlign.Center
            )

            IconButton(
                onClick = { if (quantity < maxQuantity) onQuantityChange(quantity + 1) },
                enabled = quantity < maxQuantity,
                modifier = Modifier
                    .clip(CircleShape)
                    .size(24.dp)
                    .background(Primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = stringResource(id = com.splitsnap.R.string.item_card_increase),
                    tint = Color.White,
                    modifier = Modifier
                        .size(16.dp)
                )
            }
        }
    }
}
