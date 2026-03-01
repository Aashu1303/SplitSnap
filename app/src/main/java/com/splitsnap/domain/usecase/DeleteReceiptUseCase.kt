package com.splitsnap.domain.usecase

import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class DeleteReceiptUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteReceipt(id)
}
