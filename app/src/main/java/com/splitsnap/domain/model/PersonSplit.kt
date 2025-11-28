package com.splitsnap.domain.model

data class PersonSplit(
    val person: Person,
    val items: List<SplitItem>,
    val total: Int
)

data class SplitItem(
    val itemId: String,
    val name: String,
    val quantity: Int,
    val unitPrice: Int,
    val totalPrice: Int
)
