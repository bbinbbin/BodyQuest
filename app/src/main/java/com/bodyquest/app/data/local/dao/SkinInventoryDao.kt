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

    @Query("UPDATE skin_inventory SET count = count + 1 WHERE skinId = :skinId AND userId = :userId")
    suspend fun incrementCount(skinId: String, userId: String): Int

    @Query("UPDATE skin_inventory SET count = count - 1 WHERE skinId = :skinId AND userId = :userId AND count > 1")
    suspend fun decrementCount(skinId: String, userId: String): Int

    @Query("DELETE FROM skin_inventory WHERE skinId = :skinId AND userId = :userId")
    suspend fun deleteItem(skinId: String, userId: String)
}
