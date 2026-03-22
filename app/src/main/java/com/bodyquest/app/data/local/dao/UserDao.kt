package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.bodyquest.app.data.local.entity.UserEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    @Query("SELECT * FROM users LIMIT 1")
    fun getUser(): Flow<UserEntity?>

    @Query("SELECT * FROM users LIMIT 1")
    suspend fun getUserOnce(): UserEntity?

    @Insert
    suspend fun insertUser(user: UserEntity): Long

    @Update
    suspend fun updateUser(user: UserEntity)

    @Query("UPDATE users SET xp = xp + :xpAmount WHERE id = :userId")
    suspend fun addXp(userId: Long, xpAmount: Int)

    @Query("UPDATE users SET level = :level WHERE id = :userId")
    suspend fun updateLevel(userId: Long, level: Int)

    @Query("UPDATE users SET strengthStat = :value WHERE id = :userId")
    suspend fun updateStrength(userId: Long, value: Int)

    @Query("UPDATE users SET enduranceStat = :value WHERE id = :userId")
    suspend fun updateEndurance(userId: Long, value: Int)

    @Query("UPDATE users SET balanceStat = :value WHERE id = :userId")
    suspend fun updateBalance(userId: Long, value: Int)
}
