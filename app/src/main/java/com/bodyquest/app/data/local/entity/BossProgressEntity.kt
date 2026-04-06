package com.bodyquest.app.data.local.entity

import androidx.room.Entity

// TODO: DB v14에서 FK 추가 예정 (userId → users.firebaseUid, bossId → bosses.id)
@Entity(
    tableName = "boss_progress",
    primaryKeys = ["userId", "bossId"]
)
data class BossProgressEntity(
    val bossId: Int,
    val userId: String,
    val isCleared: Boolean = false,
    val performance: String = ""   // "압도적인 승리" | "안정적인 승리" | "간신히 승리"
)
