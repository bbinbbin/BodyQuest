package com.bodyquest.app.data.local

import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.QuestEntity

val seedQuests = listOf(
    // ── STRENGTH: 가슴 ──
    QuestEntity(
        id = "str_chest_pushup", category = "STRENGTH", bodyPart = "가슴", specificArea = null,
        name = "푸시업", description = "기본 푸시업으로 가슴 전체 자극",
        difficulty = 1, durationMinutes = 10, sets = 3, repsPerSet = 15,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_chest_bench_press", category = "STRENGTH", bodyPart = "가슴", specificArea = null,
        name = "벤치프레스", description = "바벨을 이용한 플랫 벤치프레스",
        difficulty = 2, durationMinutes = 25, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_chest_incline_press", category = "STRENGTH", bodyPart = "가슴", specificArea = null,
        name = "인클라인 프레스", description = "윗가슴을 집중 자극하는 인클라인 벤치프레스",
        difficulty = 2, durationMinutes = 25, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_chest_dumbbell_fly", category = "STRENGTH", bodyPart = "가슴", specificArea = null,
        name = "덤벨 플라이", description = "가슴 안쪽을 자극하는 덤벨 플라이",
        difficulty = 2, durationMinutes = 20, sets = 3, repsPerSet = 12,
        xpReward = 50, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_chest_dips", category = "STRENGTH", bodyPart = "가슴", specificArea = null,
        name = "딥스", description = "자체 체중을 이용한 가슴 하부 운동",
        difficulty = 3, durationMinutes = 20, sets = 4, repsPerSet = 10,
        xpReward = 80, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 등 ──
    QuestEntity(
        id = "str_back_pullup", category = "STRENGTH", bodyPart = "등", specificArea = null,
        name = "풀업", description = "턱걸이로 등 전체와 이두를 자극",
        difficulty = 2, durationMinutes = 20, sets = 4, repsPerSet = 8,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_back_barbell_row", category = "STRENGTH", bodyPart = "등", specificArea = null,
        name = "바벨 로우", description = "바벨을 이용한 벤트오버 로우",
        difficulty = 2, durationMinutes = 25, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_back_lat_pulldown", category = "STRENGTH", bodyPart = "등", specificArea = null,
        name = "랫풀다운", description = "케이블 머신을 이용한 광배근 운동",
        difficulty = 1, durationMinutes = 20, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_back_seated_row", category = "STRENGTH", bodyPart = "등", specificArea = null,
        name = "시티드 로우", description = "케이블 시티드 로우로 등 중앙부 자극",
        difficulty = 1, durationMinutes = 20, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_back_deadlift", category = "STRENGTH", bodyPart = "등", specificArea = null,
        name = "데드리프트", description = "전신 복합 운동의 왕, 바벨 데드리프트",
        difficulty = 3, durationMinutes = 30, sets = 5, repsPerSet = 5,
        xpReward = 100, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 하체 ──
    QuestEntity(
        id = "str_legs_squat", category = "STRENGTH", bodyPart = "하체", specificArea = null,
        name = "스쿼트", description = "하체 운동의 기본, 바벨 백 스쿼트",
        difficulty = 2, durationMinutes = 25, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_legs_leg_press", category = "STRENGTH", bodyPart = "하체", specificArea = null,
        name = "레그프레스", description = "머신을 이용한 하체 프레스",
        difficulty = 1, durationMinutes = 20, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_legs_lunge", category = "STRENGTH", bodyPart = "하체", specificArea = null,
        name = "런지", description = "한 발씩 번갈아 실시하는 런지",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_legs_leg_curl", category = "STRENGTH", bodyPart = "하체", specificArea = null,
        name = "레그컬", description = "허벅지 뒤쪽(햄스트링) 집중 운동",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_legs_bulgarian_split", category = "STRENGTH", bodyPart = "하체", specificArea = null,
        name = "불가리안 스플릿 스쿼트", description = "한 발을 뒤에 올린 고강도 스쿼트",
        difficulty = 3, durationMinutes = 25, sets = 4, repsPerSet = 8,
        xpReward = 80, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 어깨 ──
    QuestEntity(
        id = "str_shoulder_press", category = "STRENGTH", bodyPart = "어깨", specificArea = null,
        name = "숄더프레스", description = "덤벨 또는 바벨 오버헤드 프레스",
        difficulty = 2, durationMinutes = 20, sets = 4, repsPerSet = 10,
        xpReward = 60, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_shoulder_lateral_raise", category = "STRENGTH", bodyPart = "어깨", specificArea = null,
        name = "사이드 레터럴 레이즈", description = "측면 삼각근을 자극하는 덤벨 레이즈",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 15,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_shoulder_front_raise", category = "STRENGTH", bodyPart = "어깨", specificArea = null,
        name = "프론트 레이즈", description = "전면 삼각근을 자극하는 덤벨 레이즈",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 15,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_shoulder_face_pull", category = "STRENGTH", bodyPart = "어깨", specificArea = null,
        name = "페이스풀", description = "후면 삼각근과 승모근을 자극하는 케이블 운동",
        difficulty = 2, durationMinutes = 15, sets = 3, repsPerSet = 15,
        xpReward = 50, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_shoulder_military_press", category = "STRENGTH", bodyPart = "어깨", specificArea = null,
        name = "밀리터리 프레스", description = "스탠딩 바벨 오버헤드 프레스",
        difficulty = 3, durationMinutes = 25, sets = 4, repsPerSet = 8,
        xpReward = 80, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 팔 ──
    QuestEntity(
        id = "str_arms_bicep_curl", category = "STRENGTH", bodyPart = "팔", specificArea = null,
        name = "바이셉 컬", description = "이두근을 집중 자극하는 덤벨 컬",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_arms_tricep_extension", category = "STRENGTH", bodyPart = "팔", specificArea = null,
        name = "트라이셉 익스텐션", description = "삼두근을 집중 자극하는 오버헤드 익스텐션",
        difficulty = 1, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_arms_hammer_curl", category = "STRENGTH", bodyPart = "팔", specificArea = null,
        name = "해머 컬", description = "이두근과 전완근을 함께 자극하는 컬",
        difficulty = 2, durationMinutes = 15, sets = 3, repsPerSet = 12,
        xpReward = 50, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_arms_close_grip_bench", category = "STRENGTH", bodyPart = "팔", specificArea = null,
        name = "클로즈그립 벤치프레스", description = "좁은 그립으로 삼두근을 집중 자극",
        difficulty = 3, durationMinutes = 20, sets = 4, repsPerSet = 8,
        xpReward = 80, statType = "STRENGTH", statReward = 6
    ),

    // ── STRENGTH: 코어 ──
    QuestEntity(
        id = "str_core_plank", category = "STRENGTH", bodyPart = "코어", specificArea = null,
        name = "플랭크", description = "코어 안정화의 기본, 플랭크 버티기",
        difficulty = 1, durationMinutes = 10, sets = 3, repsPerSet = 1,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_core_crunch", category = "STRENGTH", bodyPart = "코어", specificArea = null,
        name = "크런치", description = "상복부를 집중 자극하는 크런치",
        difficulty = 1, durationMinutes = 10, sets = 3, repsPerSet = 20,
        xpReward = 30, statType = "STRENGTH", statReward = 2
    ),
    QuestEntity(
        id = "str_core_leg_raise", category = "STRENGTH", bodyPart = "코어", specificArea = null,
        name = "레그레이즈", description = "하복부를 집중 자극하는 레그레이즈",
        difficulty = 2, durationMinutes = 15, sets = 3, repsPerSet = 15,
        xpReward = 50, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_core_bicycle_crunch", category = "STRENGTH", bodyPart = "코어", specificArea = null,
        name = "바이시클 크런치", description = "복사근을 포함한 전체 복근 운동",
        difficulty = 2, durationMinutes = 15, sets = 3, repsPerSet = 20,
        xpReward = 50, statType = "STRENGTH", statReward = 4
    ),
    QuestEntity(
        id = "str_core_hanging_leg_raise", category = "STRENGTH", bodyPart = "코어", specificArea = null,
        name = "행잉 레그레이즈", description = "매달려서 실시하는 고강도 하복부 운동",
        difficulty = 3, durationMinutes = 15, sets = 4, repsPerSet = 10,
        xpReward = 80, statType = "STRENGTH", statReward = 6
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
