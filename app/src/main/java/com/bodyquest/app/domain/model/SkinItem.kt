package com.bodyquest.app.domain.model

import androidx.compose.ui.graphics.Color

enum class SkinCategory(val displayName: String, val emoji: String, val color: Color) {
    TOP("상의", "👕", Color(0xFF4FC3F7)),
    BOTTOM("하의", "👖", Color(0xFF81C784)),
    SHOES("신발", "👟", Color(0xFFFFB74D)),
    GLOVES("장갑", "🥊", Color(0xFFE57373)),
    HAT("모자", "🧢", Color(0xFFCE93D8))
}

data class SkinItem(
    val id: String,
    val name: String,
    val category: SkinCategory
)

val ALL_SKINS = listOf(
    // 상의
    SkinItem("top_basic_tshirt", "기본 티셔츠", SkinCategory.TOP),
    SkinItem("top_hoodie", "후드티", SkinCategory.TOP),
    SkinItem("top_workout", "운동복 상의", SkinCategory.TOP),
    // 하의
    SkinItem("bottom_shorts", "기본 반바지", SkinCategory.BOTTOM),
    SkinItem("bottom_training", "트레이닝 팬츠", SkinCategory.BOTTOM),
    SkinItem("bottom_leggings", "레깅스", SkinCategory.BOTTOM),
    // 신발
    SkinItem("shoes_basic", "기본 운동화", SkinCategory.SHOES),
    SkinItem("shoes_running", "러닝화", SkinCategory.SHOES),
    SkinItem("shoes_hightop", "하이탑", SkinCategory.SHOES),
    // 장갑
    SkinItem("gloves_basic", "기본 장갑", SkinCategory.GLOVES),
    SkinItem("gloves_weight", "웨이트 장갑", SkinCategory.GLOVES),
    SkinItem("gloves_boxing", "권투 장갑", SkinCategory.GLOVES),
    // 모자
    SkinItem("hat_cap", "기본 캡", SkinCategory.HAT),
    SkinItem("hat_beanie", "비니", SkinCategory.HAT),
    SkinItem("hat_headband", "헤드밴드", SkinCategory.HAT)
)
