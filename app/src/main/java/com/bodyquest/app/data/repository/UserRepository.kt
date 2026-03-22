package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun getUserOnce(): UserEntity? = userDao.getUserOnce()

    suspend fun createUser(user: UserEntity): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun addXp(userId: Long, xpAmount: Int) = userDao.addXp(userId, xpAmount)

    suspend fun updateLevel(userId: Long, level: Int) = userDao.updateLevel(userId, level)

    suspend fun updateStrength(userId: Long, value: Int) = userDao.updateStrength(userId, value)

    suspend fun updateEndurance(userId: Long, value: Int) = userDao.updateEndurance(userId, value)

    suspend fun updateBalance(userId: Long, value: Int) = userDao.updateBalance(userId, value)
}
