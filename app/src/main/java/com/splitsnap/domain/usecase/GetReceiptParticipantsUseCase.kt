package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.Person
import com.splitsnap.domain.repository.SplitSnapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetReceiptParticipantsUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    operator fun invoke(receiptId: String): Flow<List<Person>> =
        repository.getReceiptParticipants(receiptId)
}
