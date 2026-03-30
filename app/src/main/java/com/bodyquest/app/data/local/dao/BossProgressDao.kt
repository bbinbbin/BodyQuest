package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bodyquest.app.data.local.entity.BossProgressEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BossProgressDao {

    @Query("SELECT * FROM boss_progress WHERE userId = :userId")
    fun getProgressForUser(userId: String): Flow<List<BossProgressEntity>>

    @Query("SELECT * FROM boss_progress WHERE userId = :userId AND bossId = :bossId LIMIT 1")
    suspend fun getProgress(userId: String, bossId: Int): BossProgressEntity?

    @Upsert
    suspend fun upsert(progress: BossProgressEntity)
}
