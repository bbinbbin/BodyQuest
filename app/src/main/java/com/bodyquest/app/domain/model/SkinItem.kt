package com.bodyquest.app.domain.model

import androidx.compose.ui.graphics.Color

enum class SkinCategory(val displayName: String, val emoji: String, val color: Color) {
    TOP("상의", "👕", Color(0xFF4FC3F7)),
    BOTTOM("하의", "👖", Color(0xFF81C784)),
    SHOES("신발", "👟", Color(0xFFFFB74D)),
    GLOVES("장갑", "🥊", Color(0xFFE57373)),
    HAT("모자", "🧢", Color(0xFFCE93D8)),
    /** 세트 스킨 — 장착 시 모든 슬롯(TOP/BOTTOM/HAT) 초기화 후 equippedSkinId에 저장 */
    SET("세트", "🎭", Color(0xFFFFD700))
}

data class SkinItem(
    val id: String,
    val name: String,
    val category: SkinCategory,
    /** null = 모든 아바타, 0 = 남성 전용, 1 = 여성 전용 */
    val avatarFilter: Int? = null
)

val ALL_SKINS = listOf(
    // 여성 전용 개별 스킨
    SkinItem("skin_f_white_tshirt", "흰색 티셔츠", SkinCategory.TOP, avatarFilter = 1),
    SkinItem("skin_f_blue_bra", "파란 스포츠브라", SkinCategory.TOP, avatarFilter = 1),
    SkinItem("skin_f_yellow_pants", "노란 트레이닝바지", SkinCategory.BOTTOM, avatarFilter = 1),
    SkinItem("skin_f_headband", "헤어밴드", SkinCategory.HAT, avatarFilter = 1),
    // 여성 전용 세트 스킨
    SkinItem("skin_f_dinosaur_set", "공룡 세트", SkinCategory.SET, avatarFilter = 1),
    SkinItem("skin_f_bunny_set", "바니 세트", SkinCategory.SET, avatarFilter = 1),
    // 남성 전용 개별 스킨
    SkinItem("skin_m_black_tank", "검정 나시티", SkinCategory.TOP, avatarFilter = 0),
    SkinItem("skin_m_white_tshirt", "흰색 반팔티", SkinCategory.TOP, avatarFilter = 0),
    SkinItem("skin_m_yellow_pants", "노란 트레이닝바지", SkinCategory.BOTTOM, avatarFilter = 0),
    // 남성 전용 세트 스킨
    SkinItem("skin_m_dinosaur_set", "공룡 세트", SkinCategory.SET, avatarFilter = 0),
    SkinItem("skin_m_bunny_set", "바니 세트", SkinCategory.SET, avatarFilter = 0),
    SkinItem("skin_m_suit_set", "정장 세트", SkinCategory.SET, avatarFilter = 0)
)
