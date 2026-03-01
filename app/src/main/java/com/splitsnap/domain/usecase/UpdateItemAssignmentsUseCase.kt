package com.splitsnap.domain.usecase

import com.splitsnap.domain.repository.SplitSnapRepository
import javax.inject.Inject

class UpdateItemAssignmentsUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(itemId: String, assignments: Map<String, Int>) =
        repository.updateItemAssignments(itemId, assignments)
}
