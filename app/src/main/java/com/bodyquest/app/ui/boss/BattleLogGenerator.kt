package com.bodyquest.app.ui.boss

import com.bodyquest.app.domain.model.BattleLog
import com.bodyquest.app.domain.model.LogType

private val attackLogs = listOf(
    "강하게 밀어붙입니다.",
    "펀치를 날렸습니다.",
    "집중해서 공격합니다.",
    "힘을 끌어올립니다.",
    "전력으로 돌격합니다."
)

private val reactionLogs = listOf(
    "효과가 상당합니다.",
    "괴물이 흔들립니다.",
    "큰 피해를 입혔습니다.",
    "공격이 제대로 들어갔습니다.",
    "치명적인 한 방입니다!"
)

private val crisisLogs = listOf(
    "점점 지쳐갑니다.",
    "밀리고 있습니다.",
    "위기 상황입니다.",
    "버티기 힘들어 보입니다.",
    "한계에 다가섭니다."
)

private val finishLogs = listOf(
    "결정적인 한 방!",
    "마지막 공격!",
    "승부를 가릅니다!"
)

fun generateBattleLogs(
    userName: String,
    bossName: String,
    isSuccess: Boolean,
    performance: String = ""
): List<BattleLog> {
    val logs = mutableListOf<BattleLog>()

    logs.add(BattleLog("${userName}이(가) ${bossName}에게 도전합니다.", LogType.START))

    val randomCount = (3..5).random()
    val logTypes = listOf("attack", "reaction", "crisis")

    repeat(randomCount) {
        val randomType = logTypes.random()
        val (message, type) = when (randomType) {
            "attack"   -> attackLogs.random()   to LogType.ATTACK
            "reaction" -> reactionLogs.random() to LogType.REACTION
            else       -> crisisLogs.random()   to LogType.CRISIS
        }
        logs.add(BattleLog(message, type))
    }

    logs.add(BattleLog(finishLogs.random(), LogType.FINISH))

    val resultMessage = when {
        !isSuccess -> "결국 무너졌습니다."
        performance == "압도적인 승리" -> "${bossName}을(를) 압도적으로 쓰러뜨렸습니다!"
        performance == "안정적인 승리" -> "${bossName}을(를) 안정적으로 쓰러뜨렸습니다!"
        else -> "${bossName}을(를) 간신히 쓰러뜨렸습니다!"
    }
    logs.add(BattleLog(resultMessage, LogType.RESULT))

    return logs
}
