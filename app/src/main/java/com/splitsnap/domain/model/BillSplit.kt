package com.splitsnap.domain.model

/**
 * Represents how much a single friend owes for a receipt
 */
data class BillSplit(
    val friend: Friend,
    val items: List<GeminiReceiptItem>,
    val itemsTotal: Double,
    val taxShare: Double,
    val tipShare: Double,
    val totalOwed: Double
)
