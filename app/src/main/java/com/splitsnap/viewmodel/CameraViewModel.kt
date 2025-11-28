package com.splitsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitsnap.data.repository.SplitSnapRepository
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CameraUiState(
    val isCapturing: Boolean = false,
    val isProcessing: Boolean = false,
    val createdReceiptId: String? = null,
    val error: String? = null
)

class CameraViewModel(
    private val repository: SplitSnapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    fun captureReceipt() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isCapturing = true)
            
            kotlinx.coroutines.delay(1500)
            
            _uiState.value = _uiState.value.copy(
                isCapturing = false,
                isProcessing = true
            )
            
            kotlinx.coroutines.delay(2000)
            
            try {
                val receipt = createMockReceipt()
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    createdReceiptId = receipt.id
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Failed to process receipt"
                )
            }
        }
    }

    private suspend fun createMockReceipt(): Receipt {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val today = dateFormat.format(Date())

        val items = listOf(
            Triple("Organic Milk", 2, 549),
            Triple("Whole Wheat Bread", 1, 399),
            Triple("Fresh Bananas", 6, 79),
            Triple("Greek Yogurt", 4, 199),
            Triple("Eggs (Dozen)", 1, 489),
            Triple("Orange Juice", 1, 599),
            Triple("Cheddar Cheese", 1, 649),
            Triple("Chicken Breast", 2, 899)
        )

        val total = items.sumOf { it.second * it.third }

        val receipt = repository.createReceipt(
            storeName = "Trader Joe's",
            date = today,
            total = total
        )

        items.forEach { (name, qty, price) ->
            repository.createReceiptItem(
                receiptId = receipt.id,
                name = name,
                quantity = qty,
                price = price
            )
        }

        val me = repository.getMe()
        if (me != null) {
            repository.addParticipant(receipt.id, me.id)
        }

        return receipt
    }

    fun clearCreatedReceipt() {
        _uiState.value = _uiState.value.copy(createdReceiptId = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    class Factory(private val repository: SplitSnapRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            if (modelClass.isAssignableFrom(CameraViewModel::class.java)) {
                return CameraViewModel(repository) as T
            }
            throw IllegalArgumentException("Unknown ViewModel class")
        }
    }
}
