package com.splitsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitsnap.data.repository.SplitSnapRepository
import com.splitsnap.domain.model.AvatarColor
import com.splitsnap.domain.model.Person
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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

class EditReceiptViewModel(
    private val receiptId: String,
    private val repository: SplitSnapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(EditReceiptUiState())
    val uiState: StateFlow<EditReceiptUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val receipt = repository.getReceiptById(receiptId)
            _uiState.value = _uiState.value.copy(receipt = receipt)

            combine(
                repository.getReceiptItems(receiptId),
                repository.getReceiptParticipants(receiptId),
                repository.getAllPeople()
            ) { items, participants, allPeople ->
                Triple(items, participants, allPeople)
            }.collect { (items, participants, allPeople) ->
                _uiState.value = _uiState.value.copy(
                    items = items,
                    participants = participants,
                    allPeople = allPeople,
                    isLoading = false
                )
            }
        }
    }

    fun toggleItemSelection(itemId: String) {
        val currentSelection = _uiState.value.selectedItemIds
        val newSelection = if (currentSelection.contains(itemId)) {
            currentSelection - itemId
        } else {
            currentSelection + itemId
        }
        _uiState.value = _uiState.value.copy(selectedItemIds = newSelection)
    }

    fun clearSelection() {
        _uiState.value = _uiState.value.copy(selectedItemIds = emptySet())
    }

    fun toggleItemExpanded(itemId: String) {
        val currentExpanded = _uiState.value.expandedItemId
        _uiState.value = _uiState.value.copy(
            expandedItemId = if (currentExpanded == itemId) null else itemId
        )
    }

    fun assignItemsToPerson(personId: String) {
        viewModelScope.launch {
            val selectedIds = _uiState.value.selectedItemIds
            val items = _uiState.value.items

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

    fun updateItemAssignment(itemId: String, personId: String, quantity: Int) {
        viewModelScope.launch {
            val item = _uiState.value.items.find { it.id == itemId } ?: return@launch
            val newAssignments = item.assignments.toMutableMap()
            
            if (quantity <= 0) {
                newAssignments.remove(personId)
            } else {
                newAssignments[personId] = quantity
            }
            
            repository.updateItemAssignments(itemId, newAssignments)
        }
    }

    fun addParticipant(personId: String) {
        viewModelScope.launch {
            repository.addParticipant(receiptId, personId)
        }
    }

    fun removeParticipant(personId: String) {
        viewModelScope.launch {
            _uiState.value.items.forEach { item ->
                if (item.assignments.containsKey(personId)) {
                    val newAssignments = item.assignments.toMutableMap()
                    newAssignments.remove(personId)
                    repository.updateItemAssignments(item.id, newAssignments)
                }
            }
            repository.removeParticipant(receiptId, personId)
        }
    }

    fun showAddPersonDialog() {
        _uiState.value = _uiState.value.copy(showAddPersonDialog = true)
    }

    fun hideAddPersonDialog() {
        _uiState.value = _uiState.value.copy(showAddPersonDialog = false)
    }

    fun createAndAddPerson(name: String, relationship: String?) {
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

    class Factory(
        private val receiptId: String,
        private val repository: SplitSnapRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(EditReceiptViewModel::class.java)) {
                return EditReceiptViewModel(receiptId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
