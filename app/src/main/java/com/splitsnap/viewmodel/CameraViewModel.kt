package com.splitsnap.viewmodel

import android.content.Context
import android.graphics.Bitmap
import androidx.camera.view.PreviewView
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsnap.camera.CameraManager
import com.splitsnap.domain.api.ReceiptResult
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.usecase.AddParticipantUseCase
import com.splitsnap.domain.usecase.CreateReceiptItemUseCase
import com.splitsnap.domain.usecase.CreateReceiptUseCase
import com.splitsnap.domain.usecase.GetMeUseCase
import com.splitsnap.domain.usecase.ProcessReceiptImageUseCase
import com.splitsnap.domain.usecase.SaveParsedReceiptUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class CameraUiState(
    val isCameraInitialized: Boolean = false,
    val isCapturing: Boolean = false,
    val isProcessing: Boolean = false,
    val capturedBitmap: Bitmap? = null,
    val createdReceiptId: String? = null,
    val error: String? = null,
    val processingStep: String = ""
)

interface CameraViewModel {
    val uiState: StateFlow<CameraUiState>
    fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    )

    fun captureReceipt()
    fun captureWithMockData()
    fun retryCapture()
    fun clearCreatedReceipt()
}

@HiltViewModel
class CameraViewModelImpl @Inject constructor(
    private val processReceiptImage: ProcessReceiptImageUseCase,
    private val saveParsedReceipt: SaveParsedReceiptUseCase,
    private val createReceipt: CreateReceiptUseCase,
    private val createReceiptItem: CreateReceiptItemUseCase,
    private val getMe: GetMeUseCase,
    private val addParticipant: AddParticipantUseCase
) : ViewModel(), CameraViewModel {

    override val uiState = MutableStateFlow(CameraUiState())

    private var cameraManager: CameraManager? = null

    override fun initializeCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        previewView: PreviewView
    ) {
        viewModelScope.launch {
            try {
                cameraManager = CameraManager(context)
                val success = cameraManager?.initializeCamera(lifecycleOwner, previewView) ?: false
                uiState.update {
                    it.copy(
                        isCameraInitialized = success,
                        error = if (!success) "Failed to initialize camera" else null
                    )
                }
            } catch (e: Exception) {
                uiState.update {
                    it.copy(
                        isCameraInitialized = false,
                        error = "Camera initialization failed: ${e.message}"
                    )
                }
            }
        }
    }

    override fun captureReceipt() {
        viewModelScope.launch {
            uiState.update { it.copy(isCapturing = true, processingStep = "Capturing image...") }

            try {
                val bitmap = cameraManager?.captureImage()

                if (bitmap != null) {
                    uiState.update {
                        it.copy(
                            isCapturing = false,
                            isProcessing = true,
                            capturedBitmap = bitmap,
                            processingStep = "Analyzing receipt..."
                        )
                    }

                    uiState.update { it.copy(processingStep = "Extracting items...") }

                    when (val result = processReceiptImage(bitmap)) {
                        is ReceiptResult.Success -> {
                            val receipt = saveParsedReceipt(result.receipt)
                            uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    createdReceiptId = receipt.id,
                                    processingStep = ""
                                )
                            }
                        }

                        is ReceiptResult.Error -> {
                            uiState.update {
                                it.copy(
                                    isProcessing = false,
                                    error = result.message,
                                    processingStep = ""
                                )
                            }
                        }
                    }
                } else {
                    uiState.update {
                        it.copy(isCapturing = false, error = "Failed to capture image")
                    }
                }
            } catch (e: Exception) {
                uiState.update {
                    it.copy(
                        isCapturing = false,
                        isProcessing = false,
                        error = "Failed to process receipt: ${e.message}"
                    )
                }
            }
        }
    }

    override fun captureWithMockData() {
        viewModelScope.launch {
            uiState.update { it.copy(isCapturing = true, processingStep = "Capturing...") }
            delay(1000)
            uiState.update {
                it.copy(
                    isCapturing = false,
                    isProcessing = true,
                    processingStep = "Processing receipt..."
                )
            }
            delay(1500)

            try {
                val receipt = createMockReceipt()
                uiState.update {
                    it.copy(
                        isProcessing = false,
                        createdReceiptId = receipt.id,
                        processingStep = ""
                    )
                }
            } catch (e: Exception) {
                uiState.update { it.copy(isProcessing = false, error = "Failed to create receipt") }
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

        val receipt = createReceipt("Trader Joe's", today, items.sumOf { it.second * it.third })

        items.forEach { (name, qty, price) ->
            createReceiptItem(receipt.id, name, qty, price)
        }

        val me = getMe()
        if (me != null) {
            addParticipant(receipt.id, me.id)
        }

        return receipt
    }

    override fun retryCapture() {
        uiState.update { it.copy(capturedBitmap = null, error = null) }
    }

    override fun clearCreatedReceipt() {
        uiState.update { it.copy(createdReceiptId = null) }
    }

    override fun onCleared() {
        super.onCleared()
        cameraManager?.shutdown()
    }
}
