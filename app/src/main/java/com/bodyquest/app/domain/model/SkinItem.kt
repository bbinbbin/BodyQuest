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
    val category: SkinCategory,
    /** null = 모든 아바타, 0 = 남성 전용, 1 = 여성 전용 */
    val avatarFilter: Int? = null
)

val ALL_SKINS = listOf(
    // 여성 전용 스킨
    SkinItem("skin_f_white_tshirt", "흰색 티셔츠", SkinCategory.TOP, avatarFilter = 1),
    SkinItem("skin_f_blue_bra", "파란 스포츠브라", SkinCategory.TOP, avatarFilter = 1),
    SkinItem("skin_f_yellow_pants", "노란 트레이닝바지", SkinCategory.BOTTOM, avatarFilter = 1),
    SkinItem("skin_f_headband", "헤어밴드", SkinCategory.HAT, avatarFilter = 1)
)
