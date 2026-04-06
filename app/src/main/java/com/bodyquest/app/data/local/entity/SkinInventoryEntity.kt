package com.bodyquest.app.data.local.entity

import androidx.room.Entity

// TODO: DB v14에서 FK 추가 예정 (userId → users.firebaseUid)
@Entity(tableName = "skin_inventory", primaryKeys = ["skinId", "userId"])
data class SkinInventoryEntity(
    val skinId: String,
    val userId: String,
    val count: Int = 0
)
