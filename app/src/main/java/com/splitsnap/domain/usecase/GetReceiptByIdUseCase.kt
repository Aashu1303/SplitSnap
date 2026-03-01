package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class GetReceiptByIdUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(id: String): Receipt? = repository.getReceiptById(id)
}
