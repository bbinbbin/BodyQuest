package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class UserRepository(private val userDao: UserDao) {
    fun getUser(): Flow<UserEntity?> = userDao.getUser()

    suspend fun getUserOnce(): UserEntity? = userDao.getUserOnce()

    suspend fun createUser(user: UserEntity): Long = userDao.insertUser(user)

    suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    suspend fun applyWorkoutRewards(
        userId: Long,
        newXp: Int,
        newLevel: Int,
        statType: String,
        newStatValue: Int
    ) = userDao.applyWorkoutRewards(userId, newXp, newLevel, statType, newStatValue)
}
