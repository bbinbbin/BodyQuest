package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getUser(firebaseUid: String): Flow<UserEntity?>
    suspend fun getUserOnce(firebaseUid: String): UserEntity?
    suspend fun createUser(user: UserEntity): Long
    suspend fun updateUser(user: UserEntity)
    suspend fun applyWorkoutRewards(
        userId: Long,
        newXp: Int,
        newLevel: Int,
        statType: String,
        newStatValue: Int
    )
    suspend fun getUserByFirebaseUid(uid: String): UserEntity?
    suspend fun deleteUserByFirebaseUid(uid: String)
}
