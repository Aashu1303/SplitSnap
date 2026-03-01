package com.splitsnap.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsnap.domain.model.PersonSplit
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptStatus
import com.splitsnap.domain.repository.SplitSnapRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class SplitSummaryUiState(
    val receipt: Receipt? = null,
    val splits: List<PersonSplit> = emptyList(),
    val expandedPersonId: String? = null,
    val isLoading: Boolean = true
)

interface SplitSummaryViewModel {
    val state: StateFlow<SplitSummaryUiState>
    fun togglePersonExpanded(personId: String)
    fun markAsCompleted()
}

@HiltViewModel
class SplitSummaryViewModelImpl @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val repository: SplitSnapRepository
) : ViewModel(), SplitSummaryViewModel {

    private val receiptId: String = checkNotNull(savedStateHandle["receiptId"])

    override val state = MutableStateFlow(SplitSummaryUiState())

    init {
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            val receipt = repository.getReceiptById(receiptId)
            val splits = repository.calculateSplits(receiptId)

            state.value = state.value.copy(
                receipt = receipt,
                splits = splits.filter { it.total > 0 },
                isLoading = false
            )
        }
    }

    override fun togglePersonExpanded(personId: String) {
        val currentExpanded = state.value.expandedPersonId
        state.value = state.value.copy(
            expandedPersonId = if (currentExpanded == personId) null else personId
        )
    }

    override fun markAsCompleted() {
        viewModelScope.launch {
            val receipt = state.value.receipt ?: return@launch
            val updatedReceipt = receipt.copy(status = ReceiptStatus.COMPLETED)
            repository.updateReceipt(updatedReceipt)
            state.value = state.value.copy(receipt = updatedReceipt)
        }
    }
}
