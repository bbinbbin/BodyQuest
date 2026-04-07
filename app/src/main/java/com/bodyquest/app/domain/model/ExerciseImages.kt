package com.bodyquest.app.domain.model

/**
 * 운동 ID → GIF 파일명 매핑.
 * GIF 파일은 assets/exercise_gif/ 폴더에 위치.
 * 새 GIF 추가 시 이 맵에 항목 추가.
 */
object ExerciseImages {
    private val gifMap = mapOf(
        // ── STRENGTH: 가슴 ──
        "str_chest_pushup" to "exercise_str_chest_pushup.gif",
        "str_chest_bench_press" to "exercise_str_chest_bench_press.gif",
        "str_chest_incline_press" to "exercise_str_chest_incline_press.gif",
        "str_chest_dumbbell_fly" to "exercise_str_chest_dumbbell_fly.gif",
        "str_chest_dips" to "exercise_str_chest_dips.gif",

        // ── STRENGTH: 등 ──
        "str_back_pullup" to "exercise_str_back_pullup.gif",
        "str_back_barbell_row" to "exercise_str_back_barbell_row.gif",
        "str_back_lat_pulldown" to "exercise_str_back_lat_pulldown.gif",
        "str_back_seated_row" to "exercise_str_back_seated_row.gif",
        "str_back_deadlift" to "exercise_str_back_deadlift.gif",

        // ── STRENGTH: 하체 ──
        "str_legs_squat" to "exercise_str_legs_squat.gif",
        "str_legs_leg_press" to "exercise_str_legs_leg_press.gif",
        "str_legs_lunge" to "exercise_str_legs_lunge.gif",
        "str_legs_leg_curl" to "exercise_str_legs_leg_curl.gif",
        "str_legs_bulgarian_split" to "exercise_str_legs_bulgarian_split.gif",

        // ── STRENGTH: 어깨 ──
        "str_shoulder_press" to "exercise_str_shoulder_press.gif",
        "str_shoulder_lateral_raise" to "exercise_str_shoulder_lateral_raise.gif",
        "str_shoulder_front_raise" to "exercise_str_shoulder_front_raise.gif",
        "str_shoulder_face_pull" to "exercise_str_shoulder_face_pull.gif",
        "str_shoulder_military_press" to "exercise_str_shoulder_military_press.gif",

        // ── STRENGTH: 팔 ──
        "str_arms_bicep_curl" to "exercise_str_arms_bicep_curl.gif",
        "str_arms_tricep_extension" to "exercise_str_arms_tricep_extension.gif",
        "str_arms_hammer_curl" to "exercise_str_arms_hammer_curl.gif",
        "str_arms_close_grip_bench" to "exercise_str_arms_close_grip_bench.gif",
    )

    /** 운동 ID로 assets 경로 반환. GIF가 없으면 null. */
    fun getGifPath(questId: String): String? {
        val fileName = gifMap[questId] ?: return null
        return "file:///android_asset/exercise_gif/$fileName"
    }

    /** 운동 GIF가 존재하는지 확인. */
    fun hasGif(questId: String): Boolean = gifMap.containsKey(questId)
}
