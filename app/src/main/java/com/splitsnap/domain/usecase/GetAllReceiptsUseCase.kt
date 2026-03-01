package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.repository.SplitSnapRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetAllReceiptsUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(): Flow<List<Receipt>> = repository.getAllReceipts()
}
