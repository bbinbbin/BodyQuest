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

    override suspend fun recordClear(userId: String, bossId: Int, performance: String) {
        bossProgressDao.upsert(
            BossProgressEntity(
                bossId = bossId,
                userId = userId,
                isCleared = true,
                performance = performance
            )
        )
    }
}
