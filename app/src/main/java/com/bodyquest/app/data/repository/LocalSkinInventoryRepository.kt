package com.bodyquest.app.data.repository

import com.bodyquest.app.data.local.dao.SkinInventoryDao
import com.bodyquest.app.data.local.entity.SkinInventoryEntity
import kotlinx.coroutines.flow.Flow

class LocalSkinInventoryRepository(
    private val dao: SkinInventoryDao
) : SkinInventoryRepository {

    override fun getInventory(userId: String): Flow<List<SkinInventoryEntity>> =
        dao.getInventory(userId)

    override suspend fun getItem(userId: String, skinId: String): SkinInventoryEntity? =
        dao.getItem(userId, skinId)

    override suspend fun addOrIncrement(userId: String, skinId: String) {
        val existing = dao.getItem(userId, skinId)
        if (existing != null) {
            dao.upsert(existing.copy(count = existing.count + 1))
        } else {
            dao.upsert(SkinInventoryEntity(skinId = skinId, userId = userId, count = 1))
        }
    }

    override suspend fun upsert(item: SkinInventoryEntity) = dao.upsert(item)
}
