package com.splitsnap.domain.repository

import android.graphics.Bitmap
import com.splitsnap.domain.api.GeminiReceiptService
import com.splitsnap.domain.api.ReceiptResult
import javax.inject.Inject

interface GeminiReceiptRepository {
    suspend fun processReceiptImage(bitmap: Bitmap): ReceiptResult
}

class GeminiReceiptRepositoryImpl @Inject constructor(
    private val service: GeminiReceiptService
) : GeminiReceiptRepository {
    override suspend fun processReceiptImage(bitmap: Bitmap): ReceiptResult =
        service.processReceiptImage(bitmap)
}