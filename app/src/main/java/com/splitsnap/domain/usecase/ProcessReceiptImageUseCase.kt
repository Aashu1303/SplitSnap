package com.splitsnap.domain.usecase

import android.graphics.Bitmap
import com.splitsnap.domain.api.ReceiptResult
import com.splitsnap.domain.repository.GeminiReceiptRepository
import javax.inject.Inject

class ProcessReceiptImageUseCase @Inject constructor(
    private val repository: GeminiReceiptRepository
) {
    suspend operator fun invoke(bitmap: Bitmap): ReceiptResult =
        repository.processReceiptImage(bitmap)
}
