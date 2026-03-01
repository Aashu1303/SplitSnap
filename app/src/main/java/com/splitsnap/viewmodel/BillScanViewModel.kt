package com.splitsnap.viewmodel

import android.graphics.Bitmap
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsnap.domain.api.ReceiptResult
import com.splitsnap.domain.model.BillSplit
import com.splitsnap.domain.model.Friend
import com.splitsnap.domain.model.GeminiReceipt
import com.splitsnap.domain.usecase.ProcessReceiptImageUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel for the bill scanning and splitting flow
 * Manages the entire flow: Scan → Review → Assign → Split
 */
interface BillScanViewModel {
    val uiState: StateFlow<BillScanState>
    fun processReceiptImage(bitmap: Bitmap)
    fun calculateSplit(receipt: GeminiReceipt): List<BillSplit>
    fun retryProcessing(bitmap: Bitmap)
    fun showSplitSummary(splits: List<BillSplit>)
    fun addFriend(friend: Friend)
    fun removeFriend(friendId: String)
    fun reset()
}

@HiltViewModel
class BillScanViewModelImpl @Inject constructor(
    private val processReceiptImageUseCase: ProcessReceiptImageUseCase
) : ViewModel(), BillScanViewModel {

    override val uiState = MutableStateFlow<BillScanState>(BillScanState.Idle)

    val friends = mutableStateListOf<Friend>()

    /**
     * Process a receipt image using Gemini API
     */
    override fun processReceiptImage(bitmap: Bitmap) {
        viewModelScope.launch {
            uiState.value = BillScanState.Loading("Analyzing receipt...")

            when (val result = processReceiptImageUseCase.invoke(bitmap)) {
                is ReceiptResult.Success -> {
                    uiState.value = BillScanState.ReviewReceipt(result.receipt)
                }

                is ReceiptResult.Error -> {
                    uiState.value = BillScanState.Error(result.message)
                }
            }
        }
    }

    /**
     * Calculate bill split based on item assignments
     * Uses proportional tax and tip splitting
     */
    override fun calculateSplit(receipt: GeminiReceipt): List<BillSplit> {
        // Get only friends who have items assigned to them
        val friendsWhoEat = friends.filter { friend ->
            receipt.items.any { item -> item.assignedTo.contains(friend.id) }
        }

        return friendsWhoEat.map { friend ->
            val myItems = receipt.items.filter { it.assignedTo.contains(friend.id) }

            // Calculate subtotal for this friend (handling shared items)
            val mySubtotal = myItems.sumOf { item ->
                item.totalPrice / item.assignedTo.size  // Split shared items equally
            }

            // Proportional tax & tip split based on subtotal
            val proportion = if (receipt.subtotal > 0) {
                mySubtotal / receipt.subtotal
            } else {
                0.0
            }

            val myTax = receipt.tax * proportion
            val myTip = receipt.tip * proportion

            BillSplit(
                friend = friend,
                items = myItems,
                itemsTotal = mySubtotal,
                taxShare = myTax,
                tipShare = myTip,
                totalOwed = mySubtotal + myTax + myTip
            )
        }
    }

    /**
     * Retry processing after an error
     */
    override fun retryProcessing(bitmap: Bitmap) = processReceiptImage(bitmap)

    /**
     * Move to the split summary screen
     */
    override fun showSplitSummary(splits: List<BillSplit>) {
        uiState.value = BillScanState.ShowSplit(splits)
    }

    /**
     * Add a friend to the list
     */
    override fun addFriend(friend: Friend) {
        if (!friends.any { it.id == friend.id }) {
            friends.add(friend)
        }
    }

    /**
     * Remove a friend from the list
     */
    override fun removeFriend(friendId: String) {
        friends.removeAll { it.id == friendId }
    }

    /**
     * Reset to idle state
     */
    override fun reset() {
        uiState.value = BillScanState.Idle
    }
}

/**
 * UI States for the bill scanning flow
 */
sealed class BillScanState {
    object Idle : BillScanState()
    data class Loading(val message: String) : BillScanState()
    data class ReviewReceipt(val receipt: GeminiReceipt) : BillScanState()
    data class ShowSplit(val splits: List<BillSplit>) : BillScanState()
    data class Error(val message: String) : BillScanState()
}
