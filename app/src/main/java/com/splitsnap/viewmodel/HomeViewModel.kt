package com.splitsnap.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.repository.SplitSnapRepository
import com.splitsnap.ui.navigation.HomeNavigationAction
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class HomeUiState(
    val receipts: List<Receipt> = emptyList(),
    val isLoading: Boolean = true
)

interface HomeViewModel {
    val state: StateFlow<HomeUiState>
    val navigationAction: Flow<HomeNavigationAction>
    fun deleteReceipt(receiptId: String)
    fun onNavigateToCamera()
    fun onNavigateToReceipt(receiptId: String)
}

@HiltViewModel
class HomeViewModelImpl @Inject constructor(
    private val repository: SplitSnapRepository
) : ViewModel(), HomeViewModel {

    override val state = MutableStateFlow(HomeUiState())
    override val navigationAction: MutableSharedFlow<HomeNavigationAction> = MutableSharedFlow()

    init {
        loadReceipts()
    }

    private fun loadReceipts() {
        viewModelScope.launch {
            repository.getAllReceipts().collect { receipts ->
                state.update {
                    it.copy(
                        receipts = receipts,
                        isLoading = false
                    )
                }
            }
        }
    }

    override fun deleteReceipt(receiptId: String) {
        viewModelScope.launch {
            repository.deleteReceipt(receiptId)
        }
    }

    override fun onNavigateToCamera() {
        viewModelScope.launch {
            navigationAction.emit(HomeNavigationAction.NavigateToCamera)
        }
    }

    override fun onNavigateToReceipt(receiptId: String) {
        viewModelScope.launch {
            navigationAction.emit(HomeNavigationAction.NavigateToReceipt(receiptId))
        }
    }
}
