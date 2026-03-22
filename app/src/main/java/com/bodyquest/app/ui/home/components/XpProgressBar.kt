package com.bodyquest.app.ui.home.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.TextSecondary
import com.bodyquest.app.ui.theme.XpGold
import com.bodyquest.app.util.XpCalculator

@Composable
fun XpProgressBar(
    level: Int,
    currentXp: Int,
    modifier: Modifier = Modifier
) {
    val xpNeeded = XpCalculator.xpForNextLevel(level)
    val fraction by animateFloatAsState(
        targetValue = (currentXp.toFloat() / xpNeeded).coerceIn(0f, 1f),
        animationSpec = tween(600),
        label = "xp"
    )

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lv.$level",
                style = MaterialTheme.typography.titleMedium,
                color = XpGold
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "$currentXp / $xpNeeded XP",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )
        }
        Spacer(modifier = Modifier.height(6.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(10.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(DarkSurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(fraction)
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(XpGold)
            )
        }
    }
}
