package com.bodyquest.app.domain.model

data class BossResult(
    val bossId: Int,
    val bossName: String,
    val success: Boolean,
    val missingStrength: Int,
    val missingEndurance: Int,
    val missingLevel: Int,
    val performance: String = ""   // "간신히 승리" | "안정적인 승리" | "압도적인 승리" | "패배"
)
