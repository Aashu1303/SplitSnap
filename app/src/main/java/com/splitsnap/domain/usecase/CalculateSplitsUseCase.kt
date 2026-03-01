package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.PersonSplit
import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class CalculateSplitsUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(receiptId: String): List<PersonSplit> =
        repository.calculateSplits(receiptId)
}
