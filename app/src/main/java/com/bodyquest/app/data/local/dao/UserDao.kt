package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
abstract class UserDao {
    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    abstract fun getUser(uid: String): Flow<UserEntity?>

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    abstract suspend fun getUserOnce(uid: String): UserEntity?

    @Query("SELECT * FROM users WHERE firebaseUid = :uid LIMIT 1")
    abstract suspend fun getUserByFirebaseUid(uid: String): UserEntity?

    @Query("DELETE FROM users WHERE firebaseUid = :uid")
    abstract suspend fun deleteUserByFirebaseUid(uid: String)

    @Insert
    abstract suspend fun insertUser(user: UserEntity): Long

    @Update
    abstract suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET xp = :xp, level = :level, strengthStat = :statValue WHERE id = :userId")
    abstract suspend fun updateRewardsStrength(userId: Long, xp: Int, level: Int, statValue: Int)

    @Query("UPDATE users SET xp = :xp, level = :level, enduranceStat = :statValue WHERE id = :userId")
    abstract suspend fun updateRewardsEndurance(userId: Long, xp: Int, level: Int, statValue: Int)

    @Query("UPDATE users SET profileImageUrl = :url, updatedAt = :updatedAt WHERE firebaseUid = :uid")
    abstract suspend fun updateProfileImageUrl(uid: String, url: String, updatedAt: Long)

    @Query("UPDATE users SET equippedSkinId = :skinId WHERE firebaseUid = :uid")
    abstract suspend fun updateEquippedSkin(uid: String, skinId: String?)

    @Query("UPDATE users SET gachaTickets = :tickets, updatedAt = :updatedAt WHERE firebaseUid = :uid")
    abstract suspend fun updateGachaTickets(uid: String, tickets: Int, updatedAt: Long)

    @Transaction
    open suspend fun applyWorkoutRewards(
        userId: Long,
        newXp: Int,
        newLevel: Int,
        statType: String,
        newStatValue: Int
    ) {
        when (statType) {
            "STRENGTH" -> updateRewardsStrength(userId, newXp, newLevel, newStatValue)
            "ENDURANCE" -> updateRewardsEndurance(userId, newXp, newLevel, newStatValue)
        }
    }
}
