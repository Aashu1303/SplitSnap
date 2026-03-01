package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.ReceiptItem
import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class CreateReceiptItemUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(
        receiptId: String,
        name: String,
        quantity: Int,
        price: Int
    ): ReceiptItem = repository.createReceiptItem(receiptId, name, quantity, price)
}
