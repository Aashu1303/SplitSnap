package com.splitsnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "receipts")
data class ReceiptEntity(
    @PrimaryKey
    val id: String,
    val storeName: String,
    val date: String,
    val total: Int,
    val status: String = "draft",
    val createdAt: Long = System.currentTimeMillis()
)
