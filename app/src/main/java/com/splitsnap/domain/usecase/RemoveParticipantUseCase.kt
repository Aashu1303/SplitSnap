package com.splitsnap.domain.usecase

import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class RemoveParticipantUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(receiptId: String, personId: String) =
        repository.removeParticipant(receiptId, personId)
}
