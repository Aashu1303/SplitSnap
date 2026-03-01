package com.splitsnap.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsnap.domain.model.AvatarColor
import com.splitsnap.domain.model.Person
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptItem
import com.splitsnap.domain.repository.SplitSnapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class EditReceiptUiState(
    val receipt: Receipt? = null,
    val items: List<ReceiptItem> = emptyList(),
    val participants: List<Person> = emptyList(),
    val allPeople: List<Person> = emptyList(),
    val selectedItemIds: Set<String> = emptySet(),
    val expandedItemId: String? = null,
    val isLoading: Boolean = true,
    val showAddPersonDialog: Boolean = false
)

interface EditReceiptViewModel {
    val uiState: StateFlow<EditReceiptUiState>
    fun toggleItemSelection(itemId: String)
    fun clearSelection()
    fun toggleItemExpanded(itemId: String)
    fun assignItemsToPerson(personId: String)
    fun updateItemAssignment(itemId: String, personId: String, quantity: Int)
    fun addParticipant(personId: String)
    fun removeParticipant(personId: String)
    fun showAddPersonDialog()
    fun hideAddPersonDialog()
    fun createAndAddPerson(name: String, relationship: String?)
}

@HiltViewModel
class EditReceiptViewModelImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SplitSnapRepository
) : ViewModel(), EditReceiptViewModel {

    private val receiptId: String = checkNotNull(savedStateHandle["receiptId"])

    override val uiState = MutableStateFlow(EditReceiptUiState())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val receipt = repository.getReceiptById(receiptId)
            uiState.value = uiState.value.copy(receipt = receipt)

            combine(
                repository.getReceiptItems(receiptId),
                repository.getReceiptParticipants(receiptId),
                repository.getAllPeople()
            ) { items, participants, allPeople ->
                Triple(items, participants, allPeople)
            }.collect { (items, participants, allPeople) ->
                uiState.value = uiState.value.copy(
                    items = items,
                    participants = participants,
                    allPeople = allPeople,
                    isLoading = false
                )
            }
        }
    }

    override fun toggleItemSelection(itemId: String) {
        val currentSelection = uiState.value.selectedItemIds
        val newSelection = if (currentSelection.contains(itemId)) {
            currentSelection - itemId
        } else {
            currentSelection + itemId
        }
        uiState.value = uiState.value.copy(selectedItemIds = newSelection)
    }

    override fun clearSelection() {
        uiState.value = uiState.value.copy(selectedItemIds = emptySet())
    }

    override fun toggleItemExpanded(itemId: String) {
        val currentExpanded = uiState.value.expandedItemId
        uiState.value = uiState.value.copy(
            expandedItemId = if (currentExpanded == itemId) null else itemId
        )
    }

    override fun assignItemsToPerson(personId: String) {
        viewModelScope.launch {
            val selectedIds = uiState.value.selectedItemIds
            val items = uiState.value.items

            selectedIds.forEach { itemId ->
                val item = items.find { it.id == itemId } ?: return@forEach
                val newAssignments = item.assignments.toMutableMap()
                val currentQty = newAssignments[personId] ?: 0
                val available = item.unassignedQuantity

                if (available > 0) {
                    newAssignments[personId] = currentQty + available
                    repository.updateItemAssignments(itemId, newAssignments)
                }
            }

            clearSelection()
        }
    }

    override fun updateItemAssignment(itemId: String, personId: String, quantity: Int) {
        viewModelScope.launch {
            val item = uiState.value.items.find { it.id == itemId } ?: return@launch
            val newAssignments = item.assignments.toMutableMap()

            if (quantity <= 0) {
                newAssignments.remove(personId)
            } else {
                newAssignments[personId] = quantity
            }

            repository.updateItemAssignments(itemId, newAssignments)
        }
    }

    override fun addParticipant(personId: String) {
        viewModelScope.launch {
            repository.addParticipant(receiptId, personId)
        }
    }

    override fun removeParticipant(personId: String) {
        viewModelScope.launch {
            uiState.value.items.forEach { item ->
                if (item.assignments.containsKey(personId)) {
                    val newAssignments = item.assignments.toMutableMap()
                    newAssignments.remove(personId)
                    repository.updateItemAssignments(item.id, newAssignments)
                }
            }
            repository.removeParticipant(receiptId, personId)
        }
    }

    override fun showAddPersonDialog() {
        uiState.value = uiState.value.copy(showAddPersonDialog = true)
    }

    override fun hideAddPersonDialog() {
        uiState.value = uiState.value.copy(showAddPersonDialog = false)
    }

    override fun createAndAddPerson(name: String, relationship: String?) {
        viewModelScope.launch {
            val person = repository.createPerson(
                name = name,
                relationship = relationship,
                avatarColor = AvatarColor.random()
            )
            repository.addParticipant(receiptId, person.id)
            hideAddPersonDialog()
        }
    }
}
