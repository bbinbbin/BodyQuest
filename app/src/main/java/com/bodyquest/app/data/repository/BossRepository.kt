package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.entity.BossEntity
import kotlinx.coroutines.flow.Flow

interface BossRepository {
    fun getAllBosses(): Flow<List<BossEntity>>
    fun getBossesByType(type: String): Flow<List<BossEntity>>
    suspend fun getBossById(id: Int): BossEntity?
}
