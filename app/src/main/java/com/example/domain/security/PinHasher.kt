package com.example.domain.security

import android.util.Base64
import java.security.MessageDigest
import java.security.SecureRandom
import javax.crypto.SecretKeyFactory
import javax.crypto.spec.PBEKeySpec

/**
 * Secure PBKDF2 PIN hashing utility.
 * Raw PINs are never stored in memory longer than necessary and never written to disk or logs.
 */
object PinHasher {

    private const val ITERATIONS = 12000
    private const val KEY_LENGTH = 256
    private const val SALT_LENGTH = 16
    private const val ALGORITHM = "PBKDF2WithHmacSHA256"

    /**
     * Generates a cryptographically strong random salt.
     */
    fun generateSalt(): String {
        val random = SecureRandom()
        val salt = ByteArray(SALT_LENGTH)
        random.nextBytes(salt)
        return Base64.encodeToString(salt, Base64.NO_WRAP)
    }

    /**
     * Computes the PBKDF2 hash of the given PIN using the provided salt.
     */
    fun hashPin(pin: String, saltBase64: String): String {
        val salt = Base64.decode(saltBase64, Base64.NO_WRAP)
        val spec = PBEKeySpec(pin.toCharArray(), salt, ITERATIONS, KEY_LENGTH)
        val factory = SecretKeyFactory.getInstance(ALGORITHM)
        val hash = factory.generateSecret(spec).encoded
        return Base64.encodeToString(hash, Base64.NO_WRAP)
    }

    /**
     * Verifies the candidate PIN against the stored hash and salt using a constant-time comparison.
     */
    fun verifyPin(candidatePin: String, saltBase64: String, expectedHashBase64: String): Boolean {
        val computedHash = hashPin(candidatePin, saltBase64)
        return MessageDigest.isEqual(
            computedHash.toByteArray(Charsets.UTF_8),
            expectedHashBase64.toByteArray(Charsets.UTF_8)
        )
    }
}
