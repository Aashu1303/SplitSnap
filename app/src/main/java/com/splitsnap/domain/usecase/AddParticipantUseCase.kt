package com.splitsnap.domain.usecase

import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class AddParticipantUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(receiptId: String, personId: String) =
        repository.addParticipant(receiptId, personId)
}
