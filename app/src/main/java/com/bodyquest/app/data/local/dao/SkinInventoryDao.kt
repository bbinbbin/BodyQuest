package com.bodyquest.app.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import com.bodyquest.app.data.local.entity.SkinInventoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SkinInventoryDao {

    @Query("SELECT * FROM skin_inventory WHERE userId = :userId")
    fun getInventory(userId: String): Flow<List<SkinInventoryEntity>>

    @Query("SELECT * FROM skin_inventory WHERE userId = :userId AND skinId = :skinId")
    suspend fun getItem(userId: String, skinId: String): SkinInventoryEntity?

    @Upsert
    suspend fun upsert(item: SkinInventoryEntity)
}
