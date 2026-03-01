package com.splitsnap.domain.usecase

import com.splitsnap.domain.model.GeminiReceipt
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.repository.SplitSnapRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

class SaveParsedReceiptUseCase @Inject constructor(
    private val repository: SplitSnapRepository
) {
    suspend operator fun invoke(geminiReceipt: GeminiReceipt): Receipt {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        val date = geminiReceipt.date ?: dateFormat.format(Date())
        val storeName = geminiReceipt.restaurantName ?: "Unknown Store"
        val totalCents = (geminiReceipt.total * 100).toInt()

        val receipt = repository.createReceipt(
            storeName = storeName,
            date = date,
            total = totalCents
        )

        geminiReceipt.items.forEach { item ->
            repository.createReceiptItem(
                receiptId = receipt.id,
                name = item.name,
                quantity = item.quantity,
                price = (item.unitPrice * 100).toInt()
            )
        }

        val me = repository.getMe()
        if (me != null) {
            repository.addParticipant(receipt.id, me.id)
        }

        return receipt
    }
}
