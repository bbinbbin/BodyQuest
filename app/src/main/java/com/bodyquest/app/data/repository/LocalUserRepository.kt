package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(private val userDao: UserDao) : UserRepository {
    override fun getUser(): Flow<UserEntity?> = userDao.getUser()

    override suspend fun getUserOnce(): UserEntity? = userDao.getUserOnce()

    override suspend fun createUser(user: UserEntity): Long = userDao.insertUser(user)

    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    override suspend fun applyWorkoutRewards(
        userId: Long,
        newXp: Int,
        newLevel: Int,
        statType: String,
        newStatValue: Int
    ) = userDao.applyWorkoutRewards(userId, newXp, newLevel, statType, newStatValue)
}
