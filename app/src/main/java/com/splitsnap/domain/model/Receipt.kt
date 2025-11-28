package com.splitsnap.domain.model

data class Receipt(
    val id: String,
    val storeName: String,
    val date: String,
    val total: Int,
    val status: ReceiptStatus = ReceiptStatus.DRAFT,
    val createdAt: Long = System.currentTimeMillis()
)

enum class ReceiptStatus {
    DRAFT,
    SPLIT,
    COMPLETED;

    companion object {
        fun fromString(value: String): ReceiptStatus {
            return when (value.lowercase()) {
                "draft" -> DRAFT
                "split" -> SPLIT
                "completed" -> COMPLETED
                else -> DRAFT
            }
        }
    }

    override fun toString(): String {
        return name.lowercase()
    }
}
