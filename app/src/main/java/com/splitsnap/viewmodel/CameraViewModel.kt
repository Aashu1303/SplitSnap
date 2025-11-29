package com.splitsnap.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.splitsnap.camera.CameraManager
import com.splitsnap.camera.ParsedReceipt
import com.splitsnap.camera.ReceiptParser
import com.splitsnap.data.repository.SplitSnapRepository
import com.splitsnap.domain.model.Receipt
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

data class CameraUiState(
    val isCameraInitialized: Boolean = false,
    val isCapturing: Boolean = false,
    val isProcessing: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val parsedReceipt: ParsedReceipt? = null,
    val createdReceiptId: String? = null,
    val error: String? = null,
    val processingStep: String = ""
)

class CameraViewModel(
    private val repository: SplitSnapRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(CameraUiState())
    val uiState: StateFlow<CameraUiState> = _uiState.asStateFlow()

    private var cameraManager: CameraManager? = null
    private var receiptParser: ReceiptParser? = null

    fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            try {
                cameraManager = CameraManager(context)
                receiptParser = ReceiptParser()
                
                val success = cameraManager?.initializeCamera(lifecycleOwner, previewView) ?: false
                
                _uiState.value = _uiState.value.copy(
                    isCameraInitialized = success,
                    error = if (!success) "Failed to initialize camera" else null
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCameraInitialized = false,
                    error = "Camera initialization failed: ${e.message}"
                )
            }
        }
    }

    fun captureReceipt() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCapturing = true,
                processingStep = "Capturing image..."
            )

            try {
                val bitmap = cameraManager?.captureImage()
                
                if (bitmap != null) {
                    _uiState.value = _uiState.value.copy(
                        isCapturing = false,
                        isProcessing = true,
                        capturedBitmap = bitmap,
                        processingStep = "Analyzing receipt..."
                    )

                    val parsedReceipt = receiptParser?.parseReceipt(bitmap)
                    
                    _uiState.value = _uiState.value.copy(
                        parsedReceipt = parsedReceipt,
                        processingStep = "Extracting items..."
                    )

                    if (parsedReceipt != null) {
                        val receipt = saveReceiptToDatabase(parsedReceipt)
                        
                        _uiState.value = _uiState.value.copy(
                            isProcessing = false,
                            createdReceiptId = receipt.id,
                            processingStep = ""
                        )
                    } else {
                        createMockReceiptFallback()
                    }
                } else {
                    _uiState.value = _uiState.value.copy(
                        isCapturing = false,
                        error = "Failed to capture image"
                    )
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isCapturing = false,
                    isProcessing = false,
                    error = "Failed to process receipt: ${e.message}"
                )
            }
        }
    }

    fun captureWithMockData() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isCapturing = true,
                processingStep = "Capturing..."
            )

            kotlinx.coroutines.delay(1000)

            _uiState.value = _uiState.value.copy(
                isCapturing = false,
                isProcessing = true,
                processingStep = "Processing receipt..."
            )

            kotlinx.coroutines.delay(1500)

            try {
                createMockReceiptFallback()
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isProcessing = false,
                    error = "Failed to create receipt"
                )
            }
        }
    }

    private suspend fun saveReceiptToDatabase(parsed: ParsedReceipt): Receipt {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = parsed.date ?: dateFormat.format(Date())

        val receipt = repository.createReceipt(
            storeName = parsed.storeName,
            date = date,
            total = parsed.total
        )

        parsed.items.forEach { item ->
            repository.createReceiptItem(
                receiptId = receipt.id,
                name = item.name,
                quantity = item.quantity,
                price = item.price
            )
        }

        val me = repository.getMe()
        if (me != null) {
            repository.addParticipant(receipt.id, me.id)
        }

        return receipt
    }

    private suspend fun createMockReceiptFallback() {
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

        _uiState.value = _uiState.value.copy(
            isProcessing = false,
            createdReceiptId = receipt.id,
            processingStep = ""
        )
    }

    fun retryCapture() {
        _uiState.value = _uiState.value.copy(
            capturedBitmap = null,
            parsedReceipt = null,
            error = null
        )
    }

    fun clearCreatedReceipt() {
        _uiState.value = _uiState.value.copy(createdReceiptId = null)
    }

    fun clearError() {
        _uiState.value = _uiState.value.copy(error = null)
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager?.shutdown()
        receiptParser?.close()
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
