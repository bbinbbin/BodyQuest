package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.BossProgressEntity
import kotlinx.coroutines.flow.Flow

interface BossRepository {
    fun getAllBosses(): Flow<List<BossEntity>>
    fun getBossesByType(type: String): Flow<List<BossEntity>>
    suspend fun getBossById(id: Int): BossEntity?
    fun getProgressForUser(userId: String): Flow<List<BossProgressEntity>>
    suspend fun recordClear(userId: String, bossId: Int, performance: String): String
    fun getClearedBossCount(userId: String): Flow<Int>
}
