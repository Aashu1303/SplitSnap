package com.splitsnap.domain.api

import android.graphics.Bitmap
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.GenerativeBackend
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import com.google.gson.Gson
import com.splitsnap.domain.model.GeminiReceipt
import com.splitsnap.domain.util.RateLimitManager

class GeminiReceiptService(
    private val rateLimitManager: RateLimitManager? = null
) {
    private val generativeModel = Firebase.ai(
        backend = GenerativeBackend.googleAI()
    ).generativeModel(
        modelName = "gemini-3-flash-preview",
        generationConfig = generationConfig {
            temperature = 0.1f
            topK = 32
            topP = 1f
            maxOutputTokens = 2048
        }
    )

    private val receiptPrompt = """
        You are an expert receipt parser. Analyze this receipt image carefully.

        Extract ALL items with their prices. Handle:
        - Abbreviated item names (expand them naturally)
        - Quantity formats like "2x", "2 @", "qty 2"
        - Any currency format
        - Discounts or coupons (show as negative items)
        - Crossed out prices (use the final price)

        Respond ONLY in this exact JSON format with no markdown or explanation:
        {
          "restaurant_name": "Name or null",
          "date": "YYYY-MM-DD or null",
          "items": [
            {
              "name": "Full item name",
              "quantity": 1,
              "unit_price": 0.00,
              "total_price": 0.00,
              "note": "any special note or null"
            }
          ],
          "subtotal": 0.00,
          "discount": 0.00,
          "tax": 0.00,
          "tip": 0.00,
          "total": 0.00,
          "currency": "USD",
          "confidence": "high/medium/low",
          "unclear_items": ["list any items you were unsure about"]
        }
    """.trimIndent()

    suspend fun processReceiptImage(bitmap: Bitmap): ReceiptResult {
        return try {
            if (rateLimitManager != null && !rateLimitManager.canMakeRequest()) {
                return ReceiptResult.Error(
                    "Daily limit reached (${rateLimitManager.getTodayCount()} requests). Try again tomorrow."
                )
            }

            val response = generativeModel.generateContent(
                content {
                    image(bitmap)
                    text(receiptPrompt)
                }
            )

            Log.d("GeminiReceiptService", "Response: ${response.text}")


            rateLimitManager?.recordRequest()

            val rawJson = response.text
                ?.trim()
                ?.removePrefix("```json")
                ?.removeSuffix("```")
                ?.trim()
                ?: throw Exception("Empty response from Gemini")

            val receipt = Gson().fromJson(rawJson, GeminiReceipt::class.java)
            ReceiptResult.Success(receipt)
        } catch (e: Exception) {
            Log.e("GeminiReceiptService", "Error processing receipt image", e)
            ReceiptResult.Error(handleError(e))
        }
    }

    private fun handleError(e: Exception): String {
        return when {
            e.message?.contains("quota") == true ->
                "Daily limit reached. Try again tomorrow."

            e.message?.contains("network") == true ->
                "No internet connection. Please check and retry."

            e.message?.contains("invalid") == true ->
                "Image unclear. Please retake the photo."

            else -> "Something went wrong. Please try again."
        }
    }
}

sealed class ReceiptResult {
    data class Success(val receipt: GeminiReceipt) : ReceiptResult()
    data class Error(val message: String) : ReceiptResult()
}
