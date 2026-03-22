package com.bodyquest.app.domain.model

import androidx.compose.ui.graphics.Color
import com.bodyquest.app.ui.theme.NeonBlue
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonRed

enum class Job(
    val displayName: String,
    val description: String,
    val icon: String,
    val color: Color
) {
    STRENGTH(
        displayName = "스트렝스",
        description = "웨이트 트레이닝 위주\n중량 향상이 핵심 지표",
        icon = "💪",
        color = NeonRed
    ),
    ENDURANCE(
        displayName = "엔듀런스",
        description = "러닝, 유산소 위주\n심박 효율이 핵심 지표",
        icon = "🏃",
        color = NeonBlue
    ),
    BALANCE(
        displayName = "밸런스",
        description = "꾸준한 운동, 회복, 균형\n지속 가능한 습관이 핵심",
        icon = "🧘",
        color = NeonGreen
    )
}
