package com.example.domain.usecase

import com.example.data.local.entity.UserEntity
import com.example.data.local.entity.UserRole
import com.example.data.repository.UserRepository
import com.example.data.session.SessionManager
import com.example.domain.security.PinHasher

object RoleGuard {
    fun canManageUsers(role: UserRole): Boolean = role == UserRole.ADMIN
    fun canModifyBusinessSettings(role: UserRole): Boolean = role == UserRole.ADMIN
    fun canManageInventory(role: UserRole): Boolean = role == UserRole.ADMIN || role == UserRole.MANAGER
    fun canAdjustStock(role: UserRole): Boolean = role == UserRole.ADMIN || role == UserRole.MANAGER
    fun canViewAllReports(role: UserRole): Boolean = role == UserRole.ADMIN || role == UserRole.MANAGER
    fun canRefundOrVoid(role: UserRole): Boolean = role == UserRole.ADMIN || role == UserRole.MANAGER
    fun canPerformSale(role: UserRole): Boolean = true // Admin, Manager, and Seller can all sell
}

class CreateAdminAccountUseCase(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    suspend operator fun invoke(
        name: String,
        identifier: String,
        pin: String,
        confirmPin: String
    ): Result<UserEntity> {
        val trimmedName = name.trim()
        val trimmedIdentifier = identifier.trim()

        if (trimmedName.isBlank()) {
            return Result.failure(IllegalArgumentException("Full name is required"))
        }
        if (trimmedIdentifier.isBlank()) {
            return Result.failure(IllegalArgumentException("Phone or email identifier is required"))
        }
        if (pin.length != 6 || !pin.all { it.isDigit() }) {
            return Result.failure(IllegalArgumentException("PIN must be exactly 6 digits"))
        }
        if (pin != confirmPin) {
            return Result.failure(IllegalArgumentException("PIN confirmation does not match"))
        }

        // Verify no admin account exists yet (first launch requirement)
        val existingCount = userRepository.getUserCount()
        if (existingCount > 0) {
            return Result.failure(IllegalStateException("An Admin account has already been initialized."))
        }

        val salt = PinHasher.generateSalt()
        val hash = PinHasher.hashPin(pin, salt)

        val adminUser = UserEntity(
            name = trimmedName,
            identifier = trimmedIdentifier,
            pinHash = hash,
            pinSalt = salt,
            role = UserRole.ADMIN,
            isActive = true,
            mustChangePin = false
        )

        return runCatching {
            val userId = userRepository.insertUser(adminUser)
            val createdUser = adminUser.copy(id = userId)
            sessionManager.startSession(createdUser)
            createdUser
        }
    }
}

class LoginUseCase(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) {
    sealed class LoginResult {
        data class Success(val user: UserEntity) : LoginResult()
        data class InvalidPin(val attemptsMessage: String) : LoginResult()
        data class LockedOut(val lockoutSeconds: Long) : LoginResult()
        data class UserNotFoundOrInactive(val message: String) : LoginResult()
    }

    suspend operator fun invoke(userId: Long, candidatePin: String): LoginResult {
        val user = userRepository.getUserById(userId)
            ?: return LoginResult.UserNotFoundOrInactive("Account not found")

        if (!user.isActive) {
            return LoginResult.UserNotFoundOrInactive("Account is deactivated. Contact Admin.")
        }

        if (candidatePin.length != 6) {
            return LoginResult.InvalidPin("Please enter your 6-digit PIN")
        }

        val isValid = PinHasher.verifyPin(candidatePin, user.pinSalt, user.pinHash)
        return if (isValid) {
            sessionManager.startSession(user)
            LoginResult.Success(user)
        } else {
            val lockoutSec = sessionManager.recordFailedAttempt()
            if (lockoutSec > 0) {
                LoginResult.LockedOut(lockoutSec)
            } else {
                LoginResult.InvalidPin("Incorrect PIN. Please try again.")
            }
        }
    }
}
