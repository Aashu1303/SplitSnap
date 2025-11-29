package com.splitsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitsnap.data.repository.SplitSnapRepository
import com.splitsnap.domain.model.Receipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class HomeUiState(
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = true
)

class HomeViewModel(
    private val repository: SplitSnapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadReceipts()
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            repository.getAllReceipts().collect { receipts ->
                _uiState.value = _uiState.value.copy(
                    receipts = receipts,
                    isLoading = false
                )
            }
        }
    }

    fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            repository.deleteReceipt(receiptId)
        }
    }

    class Factory(private val repository: SplitSnapRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(HomeViewModel::class.java)) {
                return HomeViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
