package com.example.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * User account entity.
 * Identifier is unique (e.g. phone or username).
 * Passwords/PINs are stored strictly as salted PBKDF2 hashes; raw PINs are never stored or logged.
 */
@Entity(
    tableName = "users",
    indices = [
        Index(value = ["identifier"], unique = true),
        Index(value = ["role"]),
        Index(value = ["isActive"])
    ]
)
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val identifier: String,
    val pinHash: String,
    val pinSalt: String,
    val role: UserRole,
    val isActive: Boolean = true,
    val mustChangePin: Boolean = false,
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val lastLoginAt: Long? = null
)
