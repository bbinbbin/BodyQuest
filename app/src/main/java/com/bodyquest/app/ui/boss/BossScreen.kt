package com.bodyquest.app.ui.boss

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.domain.model.BossResult
import com.bodyquest.app.ui.common.ErrorScreen
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.common.UiState
import com.bodyquest.app.ui.theme.DarkSurface
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonBlue
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonOrange
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary

@Composable
fun BossScreen(viewModel: BossViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (val current = uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = current.message, onRetry = { viewModel.retry() })
        is UiState.Success -> {
            BossContent(
                state = current.data,
                onChallenge = { boss -> viewModel.challengeBoss(boss) },
                onDismissResult = { viewModel.dismissResult() }
            )
        }
    }
}

@Composable
private fun BossContent(
    state: BossState,
    onChallenge: (BossEntity) -> Unit,
    onDismissResult: () -> Unit
) {
    val user = state.user

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "보스 도전",
            style = MaterialTheme.typography.titleLarge,
            color = NeonPurple
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 현재 유저 스탯 표시
        if (user != null) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = DarkSurfaceVariant
            ) {
                Row(
                    modifier = Modifier.padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip(label = "레벨", value = user.level.toString(), color = NeonPurple)
                    StatChip(label = "근력", value = user.strengthStat.toString(), color = NeonRed)
                    StatChip(label = "지구력", value = user.enduranceStat.toString(), color = NeonBlue)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(state.bosses) { boss ->
                BossCard(boss = boss, user = user, onChallenge = onChallenge)
            }
        }
    }

    // 결과 다이얼로그
    state.challengeResult?.let { result ->
        ChallengeResultDialog(result = result, onDismiss = onDismissResult)
    }
}

@Composable
private fun StatChip(label: String, value: String, color: androidx.compose.ui.graphics.Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

@Composable
private fun BossCard(
    boss: BossEntity,
    user: UserEntity?,
    onChallenge: (BossEntity) -> Unit
) {
    val typeColor = when (boss.type) {
        "STRENGTH" -> NeonRed
        "ENDURANCE" -> NeonBlue
        "HYBRID" -> NeonPurple
        else -> NeonOrange
    }
    val typeLabel = when (boss.type) {
        "STRENGTH" -> "근력"
        "ENDURANCE" -> "지구력"
        "HYBRID" -> "하이브리드"
        else -> boss.type
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = boss.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.weight(1f)
                )
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = typeColor.copy(alpha = 0.15f)
                ) {
                    Text(
                        text = typeLabel,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = typeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 요구 조건 표시
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                RequirementChip(
                    label = "Lv",
                    required = boss.requiredLevel,
                    current = user?.level ?: 0
                )
                if (boss.requiredStrength > 0) {
                    RequirementChip(
                        label = "근력",
                        required = boss.requiredStrength,
                        current = user?.strengthStat ?: 0
                    )
                }
                if (boss.requiredEndurance > 0) {
                    RequirementChip(
                        label = "지구력",
                        required = boss.requiredEndurance,
                        current = user?.enduranceStat ?: 0
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onChallenge(boss) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(containerColor = typeColor)
            ) {
                Text("도전하기", style = MaterialTheme.typography.labelLarge)
            }
        }
    }
}

@Composable
private fun RequirementChip(label: String, required: Int, current: Int) {
    val met = current >= required
    val color = if (met) NeonGreen else NeonRed
    Surface(
        shape = RoundedCornerShape(8.dp),
        color = color.copy(alpha = 0.1f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$current / $required",
                style = MaterialTheme.typography.labelSmall,
                color = color
            )
        }
    }
}

@Composable
private fun ChallengeResultDialog(result: BossResult, onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = if (result.success) "보스 클리어!" else "도전 실패",
                style = MaterialTheme.typography.titleMedium,
                color = if (result.success) NeonGreen else NeonRed
            )
        },
        text = {
            Column {
                Text(
                    text = result.bossName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                if (!result.success) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "부족한 조건:",
                        style = MaterialTheme.typography.labelMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    if (result.missingLevel > 0) {
                        Text(
                            text = "• 레벨 +${result.missingLevel} 필요",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonOrange
                        )
                    }
                    if (result.missingStrength > 0) {
                        Text(
                            text = "• 근력 +${result.missingStrength} 필요",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonRed
                        )
                    }
                    if (result.missingEndurance > 0) {
                        Text(
                            text = "• 지구력 +${result.missingEndurance} 필요",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonBlue
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("확인", color = NeonPurple)
            }
        }
    )
}
