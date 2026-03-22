package com.bodyquest.app.data.local

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
        xpReward = 20, statType = "ENDURANCE", statReward = 1
    ),
    QuestEntity(
        id = "end_cycling",
        category = "ENDURANCE", bodyPart = "자전거", specificArea = null,
        name = "사이클링", description = "30분 자전거 타기",
        difficulty = 2, durationMinutes = 30, sets = 1, repsPerSet = 0,
        xpReward = 50, statType = "ENDURANCE", statReward = 3
    ),

    // ── BALANCE ──
    QuestEntity(
        id = "bal_light_workout",
        category = "BALANCE", bodyPart = "전신", specificArea = null,
        name = "가벼운 전신 운동", description = "전신 스트레칭 + 가벼운 운동",
        difficulty = 1, durationMinutes = 20, sets = 1, repsPerSet = 0,
        xpReward = 25, statType = "BALANCE", statReward = 2
    ),
    QuestEntity(
        id = "bal_stretching",
        category = "BALANCE", bodyPart = "스트레칭", specificArea = null,
        name = "스트레칭 루틴", description = "전신 유연성 향상 스트레칭",
        difficulty = 1, durationMinutes = 15, sets = 1, repsPerSet = 0,
        xpReward = 20, statType = "BALANCE", statReward = 2
    ),
    QuestEntity(
        id = "bal_recovery",
        category = "BALANCE", bodyPart = "회복", specificArea = null,
        name = "회복 루틴", description = "폼롤러 + 가벼운 스트레칭",
        difficulty = 1, durationMinutes = 15, sets = 1, repsPerSet = 0,
        xpReward = 15, statType = "BALANCE", statReward = 1
    ),
    QuestEntity(
        id = "bal_walking",
        category = "BALANCE", bodyPart = "산책", specificArea = null,
        name = "산책", description = "30분 이상 걷기",
        difficulty = 1, durationMinutes = 30, sets = 1, repsPerSet = 0,
        xpReward = 20, statType = "BALANCE", statReward = 1
    ),
    QuestEntity(
        id = "bal_yoga",
        category = "BALANCE", bodyPart = "전신", specificArea = null,
        name = "요가", description = "기초 요가 루틴으로 심신 안정",
        difficulty = 2, durationMinutes = 30, sets = 1, repsPerSet = 0,
        xpReward = 40, statType = "BALANCE", statReward = 3
    )
)
