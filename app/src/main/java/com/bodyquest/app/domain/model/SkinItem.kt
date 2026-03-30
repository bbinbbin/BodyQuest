package com.bodyquest.app.domain.model

import com.bodyquest.app.R

data class SkinItem(
    val id: String,
    val name: String,
    val drawableRes: Int
)

val ALL_SKINS = listOf(
    SkinItem("underarmour_male", "언더아머 티셔츠", R.drawable.skin_underarmour_male)
)
