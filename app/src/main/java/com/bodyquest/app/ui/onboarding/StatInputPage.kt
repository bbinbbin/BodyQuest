package com.bodyquest.app.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bodyquest.app.domain.model.StatType
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.TextSecondary

@Composable
fun StatInputPage(
    strengthStat: Int,
    enduranceStat: Int,
    balanceStat: Int,
    onStrengthChange: (Int) -> Unit,
    onEnduranceChange: (Int) -> Unit,
    onBalanceChange: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "초기 스탯을 설정하세요",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "현재 자신의 운동 능력을 대략적으로 평가해주세요",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(36.dp))

        StatSlider(
            statType = StatType.STRENGTH,
            value = strengthStat,
            onValueChange = onStrengthChange
        )
        Spacer(modifier = Modifier.height(24.dp))
        StatSlider(
            statType = StatType.ENDURANCE,
            value = enduranceStat,
            onValueChange = onEnduranceChange
        )
        Spacer(modifier = Modifier.height(24.dp))
        StatSlider(
            statType = StatType.BALANCE,
            value = balanceStat,
            onValueChange = onBalanceChange
        )
    }
}

@Composable
private fun StatSlider(
    statType: StatType,
    value: Int,
    onValueChange: (Int) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = statType.displayName,
                style = MaterialTheme.typography.titleMedium,
                color = statType.color
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "$value",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Slider(
            value = value.toFloat(),
            onValueChange = { onValueChange(it.toInt()) },
            valueRange = 1f..100f,
            modifier = Modifier.fillMaxWidth(),
            colors = SliderDefaults.colors(
                thumbColor = statType.color,
                activeTrackColor = statType.color,
                inactiveTrackColor = DarkSurfaceVariant
            )
        )
    }
}
