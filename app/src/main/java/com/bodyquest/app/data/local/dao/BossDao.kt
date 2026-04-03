package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import com.bodyquest.app.data.local.entity.BossEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BossDao {

    @Query("SELECT * FROM bosses ORDER BY bossOrder ASC, type ASC")
    fun getAllBosses(): Flow<List<BossEntity>>

    @Query("SELECT * FROM bosses WHERE type = :type ORDER BY bossOrder ASC")
    fun getBossesByType(type: String): Flow<List<BossEntity>>

    @Query("SELECT * FROM bosses WHERE id = :id")
    suspend fun getBossById(id: Int): BossEntity?

    @Query("SELECT COUNT(*) FROM bosses")
    fun getTotalBossCount(): Flow<Int>
}
