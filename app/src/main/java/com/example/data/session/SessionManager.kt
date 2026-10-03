package com.example.data.session

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.data.local.entity.UserEntity
import com.example.data.repository.UserRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.IOException
import java.util.UUID

private val Context.sessionDataStore: DataStore<Preferences> by preferencesDataStore(name = "stockpos_session")

sealed class SessionState {
    data object Loading : SessionState()
    data object Unauthenticated : SessionState()
    data class Locked(val user: UserEntity, val remainingLockoutSec: Long = 0) : SessionState()
    data class Authenticated(val user: UserEntity) : SessionState()
}

class SessionManager(
    private val context: Context,
    private val userRepository: UserRepository
) {
    private val dataStore = context.sessionDataStore

    companion object {
        private val KEY_USER_ID = longPreferencesKey("session_user_id")
        private val KEY_TOKEN = stringPreferencesKey("session_token")
        private val KEY_LAST_ACTIVE = longPreferencesKey("session_last_active")
        private val KEY_IS_LOCKED = booleanPreferencesKey("session_is_locked")
        private val KEY_FAILED_ATTEMPTS = intPreferencesKey("session_failed_attempts")
        private val KEY_LOCKOUT_UNTIL = longPreferencesKey("session_lockout_until")

        // Auto-lock inactivity threshold (e.g. 5 minutes)
        const val INACTIVITY_TIMEOUT_MS = 5 * 60 * 1000L
    }

    val sessionState: Flow<SessionState> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            } else {
                emit(androidx.datastore.preferences.core.emptyPreferences())
            }
        }
        .map { prefs ->
            try {
                val userId = prefs[KEY_USER_ID] ?: -1L
                if (userId <= 0) {
                    return@map SessionState.Unauthenticated
                }

                val user = userRepository.getUserById(userId)
                if (user == null || !user.isActive) {
                    return@map SessionState.Unauthenticated
                }

                val isLocked = prefs[KEY_IS_LOCKED] ?: false
                val lastActive = prefs[KEY_LAST_ACTIVE] ?: System.currentTimeMillis()
                val lockoutUntil = prefs[KEY_LOCKOUT_UNTIL] ?: 0L
                val now = System.currentTimeMillis()

                // Check auto-lock timeout
                val timedOut = (now - lastActive) > INACTIVITY_TIMEOUT_MS

                if (isLocked || timedOut) {
                    val remainingSec = if (lockoutUntil > now) (lockoutUntil - now) / 1000 else 0L
                    SessionState.Locked(user, remainingSec)
                } else {
                    SessionState.Authenticated(user)
                }
            } catch (_: Throwable) {
                SessionState.Unauthenticated
            }
        }

    suspend fun startSession(user: UserEntity) {
        dataStore.edit { prefs ->
            prefs[KEY_USER_ID] = user.id
            prefs[KEY_TOKEN] = UUID.randomUUID().toString()
            prefs[KEY_LAST_ACTIVE] = System.currentTimeMillis()
            prefs[KEY_IS_LOCKED] = false
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCKOUT_UNTIL] = 0L
        }
        userRepository.updateLastLogin(user.id, System.currentTimeMillis())
    }

    suspend fun lockSession() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOCKED] = true
        }
    }

    suspend fun unlockSession() {
        dataStore.edit { prefs ->
            prefs[KEY_IS_LOCKED] = false
            prefs[KEY_LAST_ACTIVE] = System.currentTimeMillis()
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun recordFailedAttempt(): Long {
        var lockoutSec = 0L
        dataStore.edit { prefs ->
            val attempts = (prefs[KEY_FAILED_ATTEMPTS] ?: 0) + 1
            prefs[KEY_FAILED_ATTEMPTS] = attempts

            // Apply exponential-like lockout after 3 failed attempts
            if (attempts >= 5) {
                lockoutSec = 60L // 60 seconds
                prefs[KEY_LOCKOUT_UNTIL] = System.currentTimeMillis() + (lockoutSec * 1000)
            } else if (attempts >= 3) {
                lockoutSec = 15L // 15 seconds
                prefs[KEY_LOCKOUT_UNTIL] = System.currentTimeMillis() + (lockoutSec * 1000)
            }
        }
        return lockoutSec
    }

    suspend fun recordActivity() {
        dataStore.edit { prefs ->
            prefs[KEY_LAST_ACTIVE] = System.currentTimeMillis()
        }
    }

    suspend fun logout() {
        dataStore.edit { prefs ->
            prefs.remove(KEY_USER_ID)
            prefs.remove(KEY_TOKEN)
            prefs.remove(KEY_LAST_ACTIVE)
            prefs[KEY_IS_LOCKED] = false
            prefs[KEY_FAILED_ATTEMPTS] = 0
            prefs[KEY_LOCKOUT_UNTIL] = 0L
        }
    }

    suspend fun getCurrentUserId(): Long? {
        val prefs = dataStore.data.first()
        val id = prefs[KEY_USER_ID] ?: -1L
        return if (id > 0) id else null
    }
}
