package com.splitsnap.domain.model

data class ReceiptItem(
    val id: String,
    val receiptId: String,
    val name: String,
    val quantity: Int,
    val price: Int,
    val assignments: Map<String, Int> = emptyMap()
) {
    val totalPrice: Int get() = quantity * price
    
    val assignedQuantity: Int get() = assignments.values.sum()
    
    val unassignedQuantity: Int get() = quantity - assignedQuantity
    
    fun getAssignedQuantity(personId: String): Int = assignments[personId] ?: 0
    
    fun getPersonShare(personId: String): Int {
        val qty = assignments[personId] ?: return 0
        return qty * price
    }
}
