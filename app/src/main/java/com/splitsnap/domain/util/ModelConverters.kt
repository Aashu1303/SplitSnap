package com.splitsnap.domain.util

import com.splitsnap.domain.model.BillSplit
import com.splitsnap.domain.model.GeminiReceipt
import com.splitsnap.domain.model.GeminiReceiptItem
import com.splitsnap.domain.model.Person
import com.splitsnap.domain.model.PersonSplit
import com.splitsnap.domain.model.Receipt
import com.splitsnap.domain.model.ReceiptItem
import com.splitsnap.domain.model.ReceiptStatus
import com.splitsnap.domain.model.toFriend
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

/**
 * Utilities for converting between Gemini API models and database models
 */

/**
 * Convert GeminiReceipt to database Receipt entity
 */
fun GeminiReceipt.toReceipt(): Receipt {
    return Receipt(
        id = UUID.randomUUID().toString(),
        storeName = restaurantName ?: "Unknown",
        date = date ?: SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()).format(Date()),
        total = (total * 100).toInt(), // Convert dollars to cents
        status = ReceiptStatus.DRAFT,
        createdAt = System.currentTimeMillis()
    )
}

/**
 * Convert GeminiReceiptItem to database ReceiptItem
 */
fun GeminiReceiptItem.toReceiptItem(receiptId: String): ReceiptItem {
    return ReceiptItem(
        id = UUID.randomUUID().toString(),
        receiptId = receiptId,
        name = name,
        quantity = quantity,
        price = (unitPrice * 100).toInt(), // Convert dollars to cents
        assignments = assignedTo.associateWith { quantity } // Assign full quantity to each person
    )
}

/**
 * Convert database ReceiptItem to GeminiReceiptItem
 */
fun ReceiptItem.toGeminiReceiptItem(): GeminiReceiptItem {
    return GeminiReceiptItem(
        name = name,
        quantity = quantity,
        unitPrice = price / 100.0, // Convert cents to dollars
        totalPrice = totalPrice / 100.0, // Convert cents to dollars
        note = null,
        assignedTo = assignments.keys.toList(),
        isSplit = assignments.size > 1
    )
}

/**
 * Convert PersonSplit to BillSplit
 */
fun PersonSplit.toBillSplit(person: Person): BillSplit {
    return BillSplit(
        friend = person.toFriend(),
        items = items.map { splitItem ->
            GeminiReceiptItem(
                name = splitItem.name,
                quantity = splitItem.quantity,
                unitPrice = splitItem.unitPrice / 100.0,
                totalPrice = splitItem.totalPrice / 100.0,
                note = null,
                assignedTo = listOf(person.id),
                isSplit = false
            )
        },
        itemsTotal = items.sumOf { it.totalPrice } / 100.0,
        taxShare = 0.0, // Would need to be calculated separately
        tipShare = 0.0, // Would need to be calculated separately
        totalOwed = total / 100.0
    )
}

/**
 * Create a shareable text summary of the bill split
 */
fun createShareText(
    splits: List<BillSplit>,
    restaurantName: String?,
    geminiReceipt: GeminiReceipt
): String {
    val currencyFormat = java.text.NumberFormat.getCurrencyInstance(Locale.US)
    val sb = StringBuilder()

    sb.appendLine("📊 Bill Split - ${restaurantName ?: "Receipt"}")
    sb.appendLine("=".repeat(40))
    sb.appendLine()

    sb.appendLine("Total: ${currencyFormat.format(geminiReceipt.total)}")
    sb.appendLine("Date: ${geminiReceipt.date ?: "Today"}")
    sb.appendLine()

    splits.forEach { split ->
        sb.appendLine("👤 ${split.friend.name}")
        sb.appendLine("-".repeat(40))

        split.items.forEach { item ->
            val itemPrice = if (item.isSplit && item.assignedTo.isNotEmpty()) {
                item.totalPrice / item.assignedTo.size
            } else {
                item.totalPrice
            }
            val splitIndicator = if (item.isSplit) " (split)" else ""
            sb.appendLine("  ${item.name}$splitIndicator: ${currencyFormat.format(itemPrice)}")
        }

        sb.appendLine()
        sb.appendLine("  Items: ${currencyFormat.format(split.itemsTotal)}")
        sb.appendLine("  Tax: ${currencyFormat.format(split.taxShare)}")
        sb.appendLine("  Tip: ${currencyFormat.format(split.tipShare)}")
        sb.appendLine("  TOTAL: ${currencyFormat.format(split.totalOwed)}")
        sb.appendLine()
    }

    sb.appendLine("=".repeat(40))
    sb.appendLine("Split with SplitSnap 📱")

    return sb.toString()
}

/**
 * Calculate fair tip split based on who ordered alcohol
 * (Advanced feature for future)
 */
fun calculateProportionalTip(
    items: List<GeminiReceiptItem>,
    totalTip: Double,
    alcoholKeywords: List<String> = listOf(
        "beer",
        "wine",
        "cocktail",
        "margarita",
        "vodka",
        "whiskey"
    )
): Map<String, Double> {
    // This is a placeholder for more sophisticated tip splitting
    // For now, just split proportionally by subtotal
    val totalAmount = items.sumOf { it.totalPrice }

    return items.flatMap { item ->
        item.assignedTo.map { personId ->
            val share = (item.totalPrice / item.assignedTo.size) / totalAmount * totalTip
            personId to share
        }
    }
        .groupBy { it.first }
        .mapValues { (_, values) -> values.sumOf { it.second } }
}
