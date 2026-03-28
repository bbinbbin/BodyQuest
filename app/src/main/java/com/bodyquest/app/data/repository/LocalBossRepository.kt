package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.BossDao
import com.bodyquest.app.data.local.entity.BossEntity
import kotlinx.coroutines.flow.Flow

class LocalBossRepository(private val bossDao: BossDao) : BossRepository {
    override fun getAllBosses(): Flow<List<BossEntity>> = bossDao.getAllBosses()
    override fun getBossesByType(type: String): Flow<List<BossEntity>> = bossDao.getBossesByType(type)
    override suspend fun getBossById(id: Int): BossEntity? = bossDao.getBossById(id)
}
