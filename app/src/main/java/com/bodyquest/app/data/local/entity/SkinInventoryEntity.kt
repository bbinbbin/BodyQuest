package com.bodyquest.app.data.local.entity

import androidx.room.Entity

@Entity(tableName = "skin_inventory", primaryKeys = ["skinId", "userId"])
data class SkinInventoryEntity(
    val skinId: String,
    val userId: String,
    val count: Int = 0
)
