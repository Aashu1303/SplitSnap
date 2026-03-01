package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class CreateReceiptUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(storeName: String, date: String, total: Int): Receipt =
        repository.createReceipt(storeName, date, total)
}
