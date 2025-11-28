package com.splitsnap.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "people")
data class PersonEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val initial: String,
    val avatarColor: String,
    val isMe: Boolean = false,
    val relationship: String? = null
)
