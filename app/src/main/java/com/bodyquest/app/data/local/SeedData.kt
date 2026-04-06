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
    QuestEntity(
        id = "str_back_advanced",
        category = "STRENGTH", bodyPart = "등", specificArea = "등 전체",
        name = "등 고급 루틴", description = "데드리프트 + 턱걸이 + 시티드 로우",
        difficulty = 3, durationMinutes = 45, sets = 5, repsPerSet = 8,
        xpReward = 100, statType = "STRENGTH", statReward = 6
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
    QuestEntity(
        id = "str_legs_advanced",
        category = "STRENGTH", bodyPart = "하체", specificArea = "하체 전체",
        name = "하체 고급 루틴", description = "고중량 스쿼트 + 런지 + 레그컬",
        difficulty = 3, durationMinutes = 50, sets = 5, repsPerSet = 8,
        xpReward = 100, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 어깨 ──
    QuestEntity(
        id = "str_shoulder_beginner",
        category = "STRENGTH", bodyPart = "어깨", specificArea = "어깨 전체",
        name = "어깨 기초 루틴", description = "덤벨 프레스 위주 기초 어깨 운동",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_shoulder_intermediate",
        category = "STRENGTH", bodyPart = "어깨", specificArea = "어깨 전체",
        name = "어깨 중급 루틴", description = "오버헤드 프레스 + 사이드 레터럴 레이즈",
        difficulty = 2, durationMinutes = 30, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_shoulder_advanced",
        category = "STRENGTH", bodyPart = "어깨", specificArea = "어깨 전체",
        name = "어깨 고급 루틴", description = "밀리터리 프레스 + 페이스풀 + 슈러그",
        difficulty = 3, durationMinutes = 45, sets = 5, repsPerSet = 8,
        xpReward = 100, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 팔 ──
    QuestEntity(
        id = "str_arms_beginner",
        category = "STRENGTH", bodyPart = "팔", specificArea = "팔 전체",
        name = "팔 기초 루틴", description = "바이셉 컬 + 트라이셉 익스텐션",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_arms_intermediate",
        category = "STRENGTH", bodyPart = "팔", specificArea = "팔 전체",
        name = "팔 중급 루틴", description = "해머 컬 + 스컬크러셔 + 딥스",
        difficulty = 2, durationMinutes = 30, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_arms_advanced",
        category = "STRENGTH", bodyPart = "팔", specificArea = "팔 전체",
        name = "팔 고급 루틴", description = "바벨 컬 + 클로즈그립 벤치 + 케이블 컬",
        difficulty = 3, durationMinutes = 40, sets = 5, repsPerSet = 8,
        xpReward = 100, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 코어 ──
    QuestEntity(
        id = "str_core_beginner",
        category = "STRENGTH", bodyPart = "코어", specificArea = "복근 전체",
        name = "코어 기초 루틴", description = "크런치 + 플랭크 기초",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 15,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_core_intermediate",
        category = "STRENGTH", bodyPart = "코어", specificArea = "복근 전체",
        name = "코어 중급 루틴", description = "레그레이즈 + 사이드 플랭크 + 마운틴클라이머",
        difficulty = 2, durationMinutes = 25, sets = 4, repsPerSet = 12,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_core_advanced",
        category = "STRENGTH", bodyPart = "코어", specificArea = "복근 전체",
        name = "코어 고급 루틴", description = "행잉 레그레이즈 + 드래곤 플래그 + Ab 롤아웃",
        difficulty = 3, durationMinutes = 35, sets = 5, repsPerSet = 10,
        xpReward = 100, statType = "STRENGTH", statReward = 6
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

    // ── ENDURANCE: 줄넘기 ──
    QuestEntity(
        id = "end_jumprope_beginner",
        category = "ENDURANCE", bodyPart = "줄넘기", specificArea = null,
        name = "줄넘기 기초", description = "기본 줄넘기 10분",
        difficulty = 1, durationMinutes = 10, sets = 3, repsPerSet = 100,
        xpReward = 30, statType = "ENDURANCE", statReward = 2
    ),
    QuestEntity(
        id = "end_jumprope_intermediate",
        category = "ENDURANCE", bodyPart = "줄넘기", specificArea = null,
        name = "줄넘기 인터벌", description = "더블언더 + 일반 줄넘기 교차 반복",
        difficulty = 2, durationMinutes = 20, sets = 5, repsPerSet = 80,
        xpReward = 60, statType = "ENDURANCE", statReward = 4
    ),
    QuestEntity(
        id = "end_jumprope_advanced",
        category = "ENDURANCE", bodyPart = "줄넘기", specificArea = null,
        name = "줄넘기 고강도", description = "더블언더 + 크로스오버 + 스프린트 줄넘기",
        difficulty = 3, durationMinutes = 30, sets = 6, repsPerSet = 100,
        xpReward = 100, statType = "ENDURANCE", statReward = 6
    ),

    // ── BALANCE: 요가 ──
    QuestEntity(
        id = "bal_yoga_beginner",
        category = "BALANCE", bodyPart = "요가", specificArea = null,
        name = "입문 요가", description = "기본 자세와 호흡법 익히기",
        difficulty = 1, durationMinutes = 20, sets = 1, repsPerSet = 0,
        xpReward = 30, statType = "BALANCE", statReward = 2
    ),
    QuestEntity(
        id = "bal_yoga_intermediate",
        category = "BALANCE", bodyPart = "요가", specificArea = null,
        name = "빈야사 요가", description = "흐름을 따라 연결되는 동적 요가",
        difficulty = 2, durationMinutes = 40, sets = 1, repsPerSet = 0,
        xpReward = 60, statType = "BALANCE", statReward = 4
    ),
    QuestEntity(
        id = "bal_yoga_advanced",
        category = "BALANCE", bodyPart = "요가", specificArea = null,
        name = "파워 요가", description = "고강도 체중 지탱 자세 중심 요가",
        difficulty = 3, durationMinutes = 60, sets = 1, repsPerSet = 0,
        xpReward = 100, statType = "BALANCE", statReward = 6
    ),

    // ── BALANCE: 스트레칭 ──
    QuestEntity(
        id = "bal_stretch_beginner",
        category = "BALANCE", bodyPart = "스트레칭", specificArea = null,
        name = "기본 스트레칭", description = "전신 정적 스트레칭",
        difficulty = 1, durationMinutes = 15, sets = 1, repsPerSet = 0,
        xpReward = 20, statType = "BALANCE", statReward = 2
    ),
    QuestEntity(
        id = "bal_stretch_intermediate",
        category = "BALANCE", bodyPart = "스트레칭", specificArea = null,
        name = "동적 스트레칭", description = "관절 가동 범위를 넓히는 동적 스트레칭",
        difficulty = 2, durationMinutes = 25, sets = 1, repsPerSet = 0,
        xpReward = 50, statType = "BALANCE", statReward = 4
    ),
    QuestEntity(
        id = "bal_stretch_advanced",
        category = "BALANCE", bodyPart = "스트레칭", specificArea = null,
        name = "딥 스트레칭", description = "유연성 한계를 넓히는 고급 스트레칭",
        difficulty = 3, durationMinutes = 40, sets = 1, repsPerSet = 0,
        xpReward = 80, statType = "BALANCE", statReward = 6
    ),

    // ── BALANCE: 필라테스 ──
    QuestEntity(
        id = "bal_pilates_beginner",
        category = "BALANCE", bodyPart = "필라테스", specificArea = null,
        name = "입문 필라테스", description = "코어 안정화 중심 기초 필라테스",
        difficulty = 1, durationMinutes = 20, sets = 1, repsPerSet = 0,
        xpReward = 30, statType = "BALANCE", statReward = 2
    ),
    QuestEntity(
        id = "bal_pilates_intermediate",
        category = "BALANCE", bodyPart = "필라테스", specificArea = null,
        name = "중급 필라테스", description = "밸런스 + 코어 강화 필라테스",
        difficulty = 2, durationMinutes = 35, sets = 1, repsPerSet = 0,
        xpReward = 60, statType = "BALANCE", statReward = 4
    ),
    QuestEntity(
        id = "bal_pilates_advanced",
        category = "BALANCE", bodyPart = "필라테스", specificArea = null,
        name = "고급 필라테스", description = "고난이도 코어 + 전신 밸런스 필라테스",
        difficulty = 3, durationMinutes = 50, sets = 1, repsPerSet = 0,
        xpReward = 100, statType = "BALANCE", statReward = 6
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
