package com.splitsnap.domain.model

/**
 * Receipt model matching Gemini API response format
 * This represents the raw parsed data from the receipt image
 */
data class GeminiReceipt(
    val restaurantName: String?,
    val date: String?,
    val items: List<GeminiReceiptItem>,
    val subtotal: Double,
    val discount: Double,
    val tax: Double,
    val tip: Double,
    val total: Double,
    val currency: String,
    val confidence: String,
    val unclearItems: List<String>
)

/**
 * Receipt item as parsed from Gemini API
 */
data class GeminiReceiptItem(
    val name: String,
    val quantity: Int,
    val unitPrice: Double,
    val totalPrice: Double,
    val note: String?,

    // Bill splitting fields (app logic)
    var assignedTo: List<String> = emptyList(),
    var isSplit: Boolean = false
)
