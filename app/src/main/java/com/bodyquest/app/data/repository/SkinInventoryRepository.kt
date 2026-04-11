package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.entity.SkinInventoryEntity
import kotlinx.coroutines.flow.Flow

interface SkinInventoryRepository {
    fun getInventory(userId: String): Flow<List<SkinInventoryEntity>>
    suspend fun getItem(userId: String, skinId: String): SkinInventoryEntity?
    suspend fun addOrIncrement(userId: String, skinId: String)
    suspend fun decrementOrRemove(userId: String, skinId: String)
    suspend fun upsert(item: SkinInventoryEntity)
}
