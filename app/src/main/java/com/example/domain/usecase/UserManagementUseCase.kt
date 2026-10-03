package com.example.domain.usecase

import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.repository.UserRepository
import com.example.domain.security.PinHasher

class UserManagementUseCase(
    private val userRepository: UserRepository
) {
    suspend fun createUser(
        actingUser: UserEntity,
        name: String,
        identifier: String,
        pin: String,
        role: UserRole,
        notes: String
    ): Result<Long> {
        if (!RoleGuard.canManageUsers(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Only Admins can create accounts"))
        }
        if (name.isBlank()) return Result.failure(IllegalArgumentException("Name cannot be empty"))
        if (identifier.isBlank()) return Result.failure(IllegalArgumentException("Identifier cannot be empty"))
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("PIN must be 6 digits"))
        }

        val existing = userRepository.getUserByIdentifier(identifier.trim())
        if (existing != null) {
            return Result.failure(IllegalArgumentException("An account with this identifier already exists"))
        }

        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin(pin, salt)

        val newUser = UserEntity(
            name = name.trim(),
            identifier = identifier.trim(),
            pinHash = hash,
            pinSalt = salt,
            role = role,
            notes = notes.trim(),
            isActive = true,
            mustChangePin = false
        )
        return runCatching { userRepository.insertUser(newUser) }
    }

    suspend fun updateUser(
        actingUser: UserEntity,
        targetUserId: Long,
        name: String,
        role: UserRole,
        notes: String
    ): Result<Unit> {
        if (!RoleGuard.canManageUsers(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Only Admins can modify accounts"))
        }

        val target = userRepository.getUserById(targetUserId)
            ?: return Result.failure(IllegalArgumentException("User not found"))

        // Guard against demoting the last active Admin
        if (target.role == UserRole.ADMIN && role != UserRole.ADMIN) {
            val adminCount = userRepository.countActiveAdmins()
            if (adminCount <= 1) {
                return Result.failure(IllegalStateException("Cannot demote the last remaining active Admin"))
            }
        }

        val updated = target.copy(
            name = name.trim(),
            role = role,
            notes = notes.trim()
        )
        return runCatching { userRepository.updateUser(updated) }
    }

    suspend fun resetPin(
        actingUser: UserEntity,
        targetUserId: Long,
        temporaryPin: String
    ): Result<Unit> {
        if (!RoleGuard.canManageUsers(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Only Admins can reset PINs"))
        }
        if (temporaryPin.length != 6 || !temporaryPin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("PIN must be 6 digits"))
        }

        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin(temporaryPin, salt)
        return runCatching {
            userRepository.updatePin(targetUserId, hash, salt, mustChangePin = true)
        }
    }

    suspend fun toggleUserActive(
        actingUser: UserEntity,
        targetUserId: Long,
        setActive: Boolean
    ): Result<Unit> {
        if (!RoleGuard.canManageUsers(actingUser.role)) {
            return Result.failure(SecurityException("Unauthorized: Only Admins can toggle account status"))
        }

        val target = userRepository.getUserById(targetUserId)
            ?: return Result.failure(IllegalArgumentException("User not found"))

        // Guard against deactivating the last active Admin
        if (target.role == UserRole.ADMIN && !setActive) {
            val adminCount = userRepository.countActiveAdmins()
            if (adminCount <= 1) {
                return Result.failure(IllegalStateException("Cannot deactivate the last active Admin"))
            }
        }

        return runCatching {
            userRepository.setUserActive(targetUserId, setActive)
        }
    }
}
