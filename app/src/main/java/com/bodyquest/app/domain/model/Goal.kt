package com.bodyquest.app.domain.model

enum class Goal(
    val displayName: String,
    val description: String,
    val icon: String
) {
    DIET(
        displayName = "다이어트",
        description = "체중 감량, 체지방 감소",
        icon = "🍽️"
    ),
    BULK_UP(
        displayName = "벌크업",
        description = "근육량 증가, 체중 증가",
        icon = "💪"
    ),
    MAINTAIN(
        displayName = "유지 / 건강 관리",
        description = "현재 상태 유지, 전반적 건강",
        icon = "💚"
    )
}
