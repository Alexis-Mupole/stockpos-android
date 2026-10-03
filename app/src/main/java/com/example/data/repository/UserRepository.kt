package com.example.data.repository

import com.example.data.local.dao.UserDao
import com.example.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    suspend fun getUserCount(): Int
    suspend fun getUserById(id: Long): UserEntity?
    fun getUserByIdFlow(id: Long): Flow<UserEntity?>
    suspend fun getUserByIdentifier(identifier: String): UserEntity?
    fun getAllUsers(): Flow<List<UserEntity>>
    fun getActiveUsers(): Flow<List<UserEntity>>
    suspend fun insertUser(user: UserEntity): Long
    suspend fun updateUser(user: UserEntity)
    suspend fun updatePin(userId: Long, pinHash: String, pinSalt: String, mustChangePin: Boolean)
    suspend fun updateLastLogin(userId: Long, timestamp: Long)
    suspend fun setUserActive(userId: Long, isActive: Boolean)
    suspend fun countActiveAdmins(): Int
    suspend fun hasSalesHistory(userId: Long): Boolean
}

class UserRepositoryImpl(
    private val userDao: UserDao
) : UserRepository {
    override suspend fun getUserCount(): Int = userDao.getUserCount()
    override suspend fun getUserById(id: Long): UserEntity? = userDao.getUserById(id)
    override fun getUserByIdFlow(id: Long): Flow<UserEntity?> = userDao.getUserByIdFlow(id)
    override suspend fun getUserByIdentifier(identifier: String): UserEntity? = userDao.getUserByIdentifier(identifier)
    override fun getAllUsers(): Flow<List<UserEntity>> = userDao.getAllUsers()
    override fun getActiveUsers(): Flow<List<UserEntity>> = userDao.getActiveUsers()
    override suspend fun insertUser(user: UserEntity): Long = userDao.insertUser(user)
    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)
    override suspend fun updatePin(userId: Long, pinHash: String, pinSalt: String, mustChangePin: Boolean) {
        userDao.updatePin(userId, pinHash, pinSalt, mustChangePin)
    }
    override suspend fun updateLastLogin(userId: Long, timestamp: Long) = userDao.updateLastLogin(userId, timestamp)
    override suspend fun setUserActive(userId: Long, isActive: Boolean) = userDao.setUserActive(userId, isActive)
    override suspend fun countActiveAdmins(): Int = userDao.countActiveAdmins()
    override suspend fun hasSalesHistory(userId: Long): Boolean = userDao.hasSalesHistory(userId)
}
