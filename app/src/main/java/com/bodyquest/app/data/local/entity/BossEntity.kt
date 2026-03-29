package com.bodyquest.app.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "bosses")
data class BossEntity(
    @PrimaryKey val id: Int,
    val name: String,
    val requiredStrength: Int,
    val requiredEndurance: Int,
    val requiredLevel: Int,
    val type: String,           // "STRENGTH" | "ENDURANCE" | "HYBRID"
    @ColumnInfo(name = "bossOrder") val order: Int = 0
)
