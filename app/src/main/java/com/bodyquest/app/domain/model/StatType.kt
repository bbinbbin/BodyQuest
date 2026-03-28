package com.bodyquest.app.domain.model

import androidx.compose.ui.graphics.Color
import com.bodyquest.app.ui.theme.NeonBlue
import com.bodyquest.app.ui.theme.NeonRed

enum class StatType(
    val displayName: String,
    val color: Color
) {
    STRENGTH("근력", NeonRed),
    ENDURANCE("지구력", NeonBlue)
}
