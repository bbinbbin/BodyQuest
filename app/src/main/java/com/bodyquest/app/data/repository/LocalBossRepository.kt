package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.BossDao
import com.bodyquest.app.data.local.dao.BossProgressDao
import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.BossProgressEntity
import kotlinx.coroutines.flow.Flow

class LocalBossRepository(
    private val bossDao: BossDao,
    private val bossProgressDao: BossProgressDao
) : BossRepository {
    override fun getAllBosses(): Flow<List<BossEntity>> = bossDao.getAllBosses()
    override fun getBossesByType(type: String): Flow<List<BossEntity>> = bossDao.getBossesByType(type)
    override suspend fun getBossById(id: Int): BossEntity? = bossDao.getBossById(id)
    override fun getProgressForUser(userId: String): Flow<List<BossProgressEntity>> =
        bossProgressDao.getProgressForUser(userId)

    override suspend fun recordClear(userId: String, bossId: Int, performance: String): ClearResult {
        val existing = bossProgressDao.getProgress(userId, bossId)
        val previousPerformance = existing?.performance?.ifEmpty { null }
        val bestPerformance = if (existing != null && existing.performance.isNotEmpty()) {
            betterPerformance(existing.performance, performance)
        } else {
            performance
        }
        bossProgressDao.upsert(
            BossProgressEntity(
                bossId = bossId,
                userId = userId,
                isCleared = true,
                performance = bestPerformance
            )
        )
        return ClearResult(previousPerformance, bestPerformance)
    }

    private fun performanceRank(performance: String): Int = when (performance) {
        "압도적인 승리" -> 3  // S
        "안정적인 승리" -> 2  // A
        "간신히 승리"   -> 1  // B
        else            -> 0
    }

    private fun betterPerformance(a: String, b: String): String =
        if (performanceRank(a) >= performanceRank(b)) a else b

    override fun getClearedBossCount(userId: String): Flow<Int> =
        bossProgressDao.getClearedBossCount(userId)
}
