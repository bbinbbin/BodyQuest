package com.bodyquest.app.domain.model

enum class ExerciseInputType {
    WEIGHT_REPS,  // 중량 + 횟수 (벤치프레스, 스쿼트 등)
    REPS_ONLY,    // 횟수만 (푸시업, 풀업, 크런치 등)
    TIME_ONLY,    // 시간 기반 (플랭크, 러닝, 요가 등)
    MIXED         // 횟수 또는 시간 (줄넘기)
}
