package com.example.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "local_accounts",
    indices = [Index(value = ["email"], unique = true)]
)
data class LocalAccountEntity(
    @PrimaryKey val id: String,
    val displayName: String,
    val email: String,
    val passwordSalt: String,
    val passwordHash: String,
    val createdAt: Long
)
