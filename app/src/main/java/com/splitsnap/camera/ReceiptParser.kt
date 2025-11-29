package com.splitsnap.camera

import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.suspendCancellableCoroutine
import java.util.regex.Pattern
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class ParsedReceipt(
    val storeName: String,
    val date: String?,
    val items: List<ParsedItem>,
    val total: Int,
    val rawText: String
)

data class ParsedItem(
    val name: String,
    val quantity: Int,
    val price: Int
)

class ReceiptParser {
    private val textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    private val pricePattern = Pattern.compile("[$€£¥₹]?\\s*\\d{1,3}(?:[,.]\\d{3})*[,.]\\d{2}|[$€£¥₹]?\\s*\\d+[.,]\\d{2}")
    private val datePattern = Pattern.compile(
        "(\\d{1,2}[/\\-.]\\d{1,2}[/\\-.]\\d{2,4})|" +
        "(\\w{3,9}\\s+\\d{1,2},?\\s+\\d{4})"
    )
    private val totalKeywords = listOf("total", "subtotal", "amount", "sum", "balance", "due")
    private val skipKeywords = listOf("tax", "tip", "discount", "change", "cash", "credit", "debit", "card")

    suspend fun parseReceipt(bitmap: Bitmap): ParsedReceipt {
        val text = recognizeText(bitmap)
        return parseReceiptText(text)
    }

    private suspend fun recognizeText(bitmap: Bitmap): String = suspendCancellableCoroutine { continuation ->
        val image = InputImage.fromBitmap(bitmap, 0)
        
        textRecognizer.process(image)
            .addOnSuccessListener { result ->
                continuation.resume(result.text)
            }
            .addOnFailureListener { exception ->
                continuation.resumeWithException(exception)
            }
    }

    private fun parseReceiptText(text: String): ParsedReceipt {
        val lines = text.split("\n").map { it.trim() }.filter { it.isNotEmpty() }
        
        val storeName = extractStoreName(lines)
        val date = extractDate(text)
        val items = extractItems(lines)
        val total = extractTotal(lines) ?: items.sumOf { it.price * it.quantity }

        return ParsedReceipt(
            storeName = storeName,
            date = date,
            items = items,
            total = total,
            rawText = text
        )
    }

    private fun extractStoreName(lines: List<String>): String {
        for (line in lines.take(5)) {
            val cleaned = line.replace(Regex("[^a-zA-Z\\s&']"), "").trim()
            if (cleaned.length >= 3 && !cleaned.all { it.isDigit() }) {
                val priceMatcher = pricePattern.matcher(line)
                if (!priceMatcher.find()) {
                    return cleaned.split("\\s+".toRegex())
                        .take(4)
                        .joinToString(" ")
                        .take(30)
                }
            }
        }
        return "Unknown Store"
    }

    private fun extractDate(text: String): String? {
        val matcher = datePattern.matcher(text)
        return if (matcher.find()) {
            matcher.group()
        } else null
    }

    private fun extractItems(lines: List<String>): List<ParsedItem> {
        val items = mutableListOf<ParsedItem>()
        
        for (line in lines) {
            val lowerLine = line.lowercase()
            
            if (skipKeywords.any { lowerLine.contains(it) }) continue
            if (totalKeywords.any { lowerLine.startsWith(it) }) continue
            
            val priceMatcher = pricePattern.matcher(line)
            if (priceMatcher.find()) {
                val priceStr = priceMatcher.group()
                val price = parsePrice(priceStr)
                
                if (price > 0 && price < 100000) {
                    val itemName = line
                        .substring(0, priceMatcher.start())
                        .replace(Regex("[^a-zA-Z\\s]"), "")
                        .trim()
                    
                    if (itemName.length >= 2) {
                        val quantity = extractQuantity(line) ?: 1
                        
                        items.add(ParsedItem(
                            name = itemName.take(50),
                            quantity = quantity,
                            price = if (quantity > 1) price / quantity else price
                        ))
                    }
                }
            }
        }
        
        return items.take(50)
    }

    private fun extractQuantity(line: String): Int? {
        val qtyPattern = Pattern.compile("^(\\d+)\\s*[xX@]")
        val matcher = qtyPattern.matcher(line.trim())
        return if (matcher.find()) {
            matcher.group(1)?.toIntOrNull()
        } else null
    }

    private fun extractTotal(lines: List<String>): Int? {
        for (line in lines.reversed()) {
            val lowerLine = line.lowercase()
            
            if (totalKeywords.any { lowerLine.contains(it) }) {
                val priceMatcher = pricePattern.matcher(line)
                if (priceMatcher.find()) {
                    return parsePrice(priceMatcher.group())
                }
            }
        }
        
        val lastLinesWithPrices = lines.takeLast(5)
        for (line in lastLinesWithPrices.reversed()) {
            val priceMatcher = pricePattern.matcher(line)
            if (priceMatcher.find()) {
                val price = parsePrice(priceMatcher.group())
                if (price > 100) {
                    return price
                }
            }
        }
        
        return null
    }

    private fun parsePrice(priceStr: String): Int {
        var cleanPrice = priceStr
            .replace(Regex("[$€£¥₹\\s]"), "")
            .trim()
        
        val lastDotIndex = cleanPrice.lastIndexOf('.')
        val lastCommaIndex = cleanPrice.lastIndexOf(',')
        
        cleanPrice = if (lastCommaIndex > lastDotIndex && 
                        cleanPrice.length - lastCommaIndex == 3) {
            cleanPrice.replace(".", "").replace(",", ".")
        } else {
            cleanPrice.replace(",", "")
        }
        
        return try {
            (cleanPrice.toDouble() * 100).toInt()
        } catch (e: NumberFormatException) {
            0
        }
    }

    fun close() {
        textRecognizer.close()
    }
}
