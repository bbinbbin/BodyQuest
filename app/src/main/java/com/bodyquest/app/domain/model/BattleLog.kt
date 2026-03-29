package com.bodyquest.app.domain.model

enum class LogType {
    START,
    ATTACK,
    REACTION,
    CRISIS,
    FINISH,
    RESULT
}

data class BattleLog(
    val message: String,
    val type: LogType
)
