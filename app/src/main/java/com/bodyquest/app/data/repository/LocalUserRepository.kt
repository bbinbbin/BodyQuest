package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.UserDao
import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

class LocalUserRepository(private val userDao: UserDao) : UserRepository {
    override fun getUser(firebaseUid: String): Flow<UserEntity?> = userDao.getUser(firebaseUid)

    override suspend fun getUserOnce(firebaseUid: String): UserEntity? = userDao.getUserOnce(firebaseUid)

    override suspend fun createUser(user: UserEntity): Long = userDao.insertUser(user)

    override suspend fun updateUser(user: UserEntity) = userDao.updateUser(user)

    override suspend fun applyWorkoutRewards(
        userId: Long,
        newXp: Int,
        newLevel: Int,
        statType: String,
        newStatValue: Int
    ) = userDao.applyWorkoutRewards(userId, newXp, newLevel, statType, newStatValue)

    override suspend fun getUserByFirebaseUid(uid: String): UserEntity? =
        userDao.getUserByFirebaseUid(uid)

    override suspend fun deleteUserByFirebaseUid(uid: String) =
        userDao.deleteUserByFirebaseUid(uid)

    override suspend fun updateProfileImageUrl(firebaseUid: String, url: String) =
        userDao.updateProfileImageUrl(firebaseUid, url, System.currentTimeMillis())

    override suspend fun updateEquippedSkin(firebaseUid: String, skinId: String?) =
        userDao.updateEquippedSkin(firebaseUid, skinId)

    override suspend fun updateGachaTickets(firebaseUid: String, tickets: Int) =
        userDao.updateGachaTickets(firebaseUid, tickets, System.currentTimeMillis())
}
