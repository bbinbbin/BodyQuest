package com.bodyquest.app.data.local

import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.QuestEntity

val seedQuests = listOf(
    // ── STRENGTH: 가슴 ──
    QuestEntity(
        id = "str_chest_beginner",
        category = "STRENGTH", bodyPart = "가슴", specificArea = "가슴 전체",
        name = "가슴 기초 루틴", description = "푸시업 위주의 기초 가슴 운동",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_chest_intermediate",
        category = "STRENGTH", bodyPart = "가슴", specificArea = "가슴 전체",
        name = "가슴 중급 루틴", description = "벤치프레스 중심 가슴 운동",
        difficulty = 2, durationMinutes = 30, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_chest_advanced",
        category = "STRENGTH", bodyPart = "가슴", specificArea = "가슴 전체",
        name = "가슴 고급 루틴", description = "고중량 벤치프레스 + 플라이",
        difficulty = 3, durationMinutes = 45, sets = 5, repsPerSet = 8,
        xpReward = 100, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 등 ──
    QuestEntity(
        id = "str_back_beginner",
        category = "STRENGTH", bodyPart = "등", specificArea = "등 전체",
        name = "등 기초 루틴", description = "밴드 로우 위주의 기초 등 운동",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_back_intermediate",
        category = "STRENGTH", bodyPart = "등", specificArea = "등 전체",
        name = "등 중급 루틴", description = "풀업 + 바벨 로우",
        difficulty = 2, durationMinutes = 30, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),

    // ── STRENGTH: 하체 ──
    QuestEntity(
        id = "str_legs_beginner",
        category = "STRENGTH", bodyPart = "하체", specificArea = "하체 전체",
        name = "하체 기초 루틴", description = "스쿼트 위주의 기초 하체 운동",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 15,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_legs_intermediate",
        category = "STRENGTH", bodyPart = "하체", specificArea = "하체 전체",
        name = "하체 중급 루틴", description = "바벨 스쿼트 + 레그프레스",
        difficulty = 2, durationMinutes = 35, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),

    // ── STRENGTH: 어깨 ──
    QuestEntity(
        id = "str_shoulder_beginner",
        category = "STRENGTH", bodyPart = "어깨", specificArea = "어깨 전체",
        name = "어깨 기초 루틴", description = "덤벨 프레스 위주 기초 어깨 운동",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),

    // ── STRENGTH: 팔 ──
    QuestEntity(
        id = "str_arms_beginner",
        category = "STRENGTH", bodyPart = "팔", specificArea = "팔 전체",
        name = "팔 기초 루틴", description = "바이셉 컬 + 트라이셉 익스텐션",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),

    // ── ENDURANCE ──
    QuestEntity(
        id = "end_light_run",
        category = "ENDURANCE", bodyPart = "달리기", specificArea = null,
        name = "가볍게 달리기", description = "편안한 페이스로 20분 달리기",
        difficulty = 1, durationMinutes = 20, sets = 1, repsPerSet = 0,
        xpReward = 30, statType = "ENDURANCE", statReward = 2
    ),
    QuestEntity(
        id = "end_interval",
        category = "ENDURANCE", bodyPart = "달리기", specificArea = null,
        name = "인터벌 트레이닝", description = "1분 스프린트 + 1분 조깅 반복",
        difficulty = 2, durationMinutes = 25, sets = 8, repsPerSet = 0,
        xpReward = 60, statType = "ENDURANCE", statReward = 4
    ),
    QuestEntity(
        id = "end_long_distance",
        category = "ENDURANCE", bodyPart = "달리기", specificArea = null,
        name = "장거리 달리기", description = "일정 페이스로 40분 이상 달리기",
        difficulty = 3, durationMinutes = 45, sets = 1, repsPerSet = 0,
        xpReward = 100, statType = "ENDURANCE", statReward = 6
    ),
    QuestEntity(
        id = "end_recovery_jog",
        category = "ENDURANCE", bodyPart = "달리기", specificArea = null,
        name = "회복 조깅", description = "매우 느린 페이스로 가볍게 조깅",
        difficulty = 1, durationMinutes = 15, sets = 1, repsPerSet = 0,
        xpReward = 20, statType = "ENDURANCE", statReward = 2
    ),
    QuestEntity(
        id = "end_cycling",
        category = "ENDURANCE", bodyPart = "자전거", specificArea = null,
        name = "사이클링", description = "30분 자전거 타기",
        difficulty = 2, durationMinutes = 30, sets = 1, repsPerSet = 0,
        xpReward = 50, statType = "ENDURANCE", statReward = 4
    ),
)

// ── 보스 생성 ─────────────────────────────────────────────────────────────────

private val strengthPrefixes = listOf(
    "잠든", "눈뜬", "성난", "무쇠", "화강암의",
    "불굴의", "분노한", "용암의", "화염의", "전설의"
)
private val strengthSuffixes = listOf("야수", "전사", "거인", "파괴자", "군주")

private val endurancePrefixes = listOf(
    "미풍의", "산들의", "거센", "폭풍의", "번개의",
    "질풍의", "회오리의", "태풍의", "폭풍우의", "전설의"
)
private val enduranceSuffixes = listOf("사자", "용사", "달인", "지배자", "챔피언")

private val hybridPrefixes = listOf(
    "고요한", "균형의", "이중의", "혼돈의", "융합의",
    "조화의", "복합의", "이원의", "초월의", "전설의"
)
private val hybridSuffixes = listOf("수호자", "현자", "전사", "심판자", "군주")

/** index 0~49 기준 기본 스탯 계산 (누적 증가율 구간별 감소) */
private fun computeBaseStat(index: Int): Int {
    var stat = 10.0
    for (i in 1..index) {
        stat += when {
            i <= 15 -> 3.0
            i <= 30 -> 2.5
            else    -> 2.0
        }
    }
    return stat.toInt()
}

private fun buildBoss(type: String, index: Int): BossEntity {
    val idOffset = when (type) {
        "STRENGTH"  -> 1
        "ENDURANCE" -> 51
        else        -> 101
    }
    val prefixes = when (type) {
        "STRENGTH"  -> strengthPrefixes
        "ENDURANCE" -> endurancePrefixes
        else        -> hybridPrefixes
    }
    val suffixes = when (type) {
        "STRENGTH"  -> strengthSuffixes
        "ENDURANCE" -> enduranceSuffixes
        else        -> hybridSuffixes
    }

    val name = "${prefixes[index / 5]} ${suffixes[index % 5]}"
    val base = computeBaseStat(index)

    val (reqStr, reqEnd) = when (type) {
        "STRENGTH"  -> Pair(base, (base * 0.3).toInt())
        "ENDURANCE" -> Pair((base * 0.3).toInt(), base)
        else        -> Pair((base * 0.8).toInt(), (base * 0.8).toInt())
    }

    return BossEntity(
        id = idOffset + index,
        name = name,
        requiredStrength = reqStr,
        requiredEndurance = reqEnd,
        requiredLevel = index,
        type = type,
        order = index
    )
}

val seedBosses: List<BossEntity> = buildList {
    for (type in listOf("STRENGTH", "ENDURANCE", "HYBRID")) {
        repeat(50) { index -> add(buildBoss(type, index)) }
    }
}
