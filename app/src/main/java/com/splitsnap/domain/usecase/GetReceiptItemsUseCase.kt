package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.ReceiptItem
import com.splitsnap.domain.repository.SplitSnapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReceiptItemsUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    operator fun invoke(receiptId: String): Flow<List<ReceiptItem>> =
        repository.getReceiptItems(receiptId)
}
