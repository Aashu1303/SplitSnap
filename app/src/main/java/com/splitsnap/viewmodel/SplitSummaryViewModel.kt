package com.splitsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitsnap.data.repository.SplitSnapRepository
import com.splitsnap.domain.model.PersonSplit
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class SplitSummaryUiState(
    val receipt: Receipt? = null,
    val splits: List<PersonSplit> = emptyList(),
    val expandedPersonId: String? = null,
    val isLoading: Boolean = true
)

class SplitSummaryViewModel(
    private val receiptId: String,
    private val repository: SplitSnapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplitSummaryUiState())
    val uiState: StateFlow<SplitSummaryUiState> = _uiState.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val receipt = repository.getReceiptById(receiptId)
            val splits = repository.calculateSplits(receiptId)
            
            _uiState.value = _uiState.value.copy(
                receipt = receipt,
                splits = splits.filter { it.total > 0 },
                isLoading = false
            )
        }
    }

    fun togglePersonExpanded(personId: String) {
        val currentExpanded = _uiState.value.expandedPersonId
        _uiState.value = _uiState.value.copy(
            expandedPersonId = if (currentExpanded == personId) null else personId
        )
    }

    fun markAsCompleted() {
        viewModelScope.launch {
            val receipt = _uiState.value.receipt ?: return@launch
            val updatedReceipt = receipt.copy(status = ReceiptStatus.COMPLETED)
            repository.updateReceipt(updatedReceipt)
            _uiState.value = _uiState.value.copy(receipt = updatedReceipt)
        }
    }

    class Factory(
        private val receiptId: String,
        private val repository: SplitSnapRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(SplitSummaryViewModel::class.java)) {
                return SplitSummaryViewModel(receiptId, repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
