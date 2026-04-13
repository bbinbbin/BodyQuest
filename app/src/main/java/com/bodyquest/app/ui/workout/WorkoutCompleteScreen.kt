package com.bodyquest.app.ui.workout

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Repeat
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.domain.model.StatType
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.XpGold
import com.bodyquest.app.ui.theme.NeonBlue
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary

@Composable
fun WorkoutCompleteScreen(
    workoutId: Long,
    viewModel: WorkoutViewModel,
    onGoHome: () -> Unit
) {
    val state by viewModel.completeState.collectAsState()
    val workoutState by viewModel.state.collectAsState()

    LaunchedEffect(workoutId) {
        if (state.questName.isEmpty()) {
            viewModel.loadCompleteData(workoutId)
        }
    }

    if (state.questName.isEmpty()) return

    val job = try { Job.valueOf(state.questCategory) } catch (_: Exception) { Job.STRENGTH }
    val statType = try { StatType.valueOf(state.statType) } catch (_: Exception) { StatType.STRENGTH }

    val minutes = state.elapsedSeconds / 60
    val seconds = state.elapsedSeconds % 60

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(40.dp))

        // Trophy icon
        Icon(
            imageVector = Icons.Default.EmojiEvents,
            contentDescription = null,
            tint = XpGold,
            modifier = Modifier.size(80.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "퀘스트 완료!",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = state.questName,
            style = MaterialTheme.typography.titleMedium,
            color = job.color
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Stats summary
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "운동 요약",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    SummaryItem(
                        icon = Icons.Default.Timer,
                        value = "%02d:%02d".format(minutes, seconds),
                        label = "시간",
                        color = NeonBlue
                    )
                    SummaryItem(
                        icon = Icons.Default.Favorite,
                        value = "${state.heartRateAvg}",
                        label = "평균 BPM",
                        color = NeonRed
                    )
                    SummaryItem(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${state.caloriesBurned}",
                        label = "kcal",
                        color = NeonGreen
                    )
                }

                // 세트/reps/볼륨 행
                val showReps = state.totalReps > 0
                val showVolume = state.totalVolume > 0.0
                if (showReps || showVolume) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        SummaryItem(
                            icon = Icons.Default.ViewList,
                            value = "${state.totalSets}",
                            label = "세트",
                            color = NeonPurple
                        )
                        if (showReps) {
                            SummaryItem(
                                icon = Icons.Default.Repeat,
                                value = "${state.totalReps}",
                                label = "총 횟수",
                                color = NeonPurple
                            )
                        }
                        if (showVolume) {
                            SummaryItem(
                                icon = Icons.Default.FitnessCenter,
                                value = "${state.totalVolume.toInt()}",
                                label = "볼륨 kg",
                                color = NeonPurple
                            )
                        }
                    }
                }
            }
        }

        // 보상 저장 실패 경고
        workoutState.rewardError?.let { error ->
            Spacer(modifier = Modifier.height(12.dp))
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                color = NeonRed.copy(alpha = 0.15f)
            ) {
                Text(
                    text = error,
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonRed
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Rewards
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "획득 보상",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                // XP reward
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "XP",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        modifier = Modifier.width(80.dp)
                    )
                    Text(
                        text = "+${state.xpEarned} XP",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = XpGold
                    )
                }

                // Stat reward
                if (statType == StatType.BALANCE) {
                    // BALANCE: 양쪽 스탯 표시
                    val half = state.statReward / 2
                    val remainder = state.statReward % 2
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "균형",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.width(80.dp)
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = "근력 +${half + remainder}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonRed
                            )
                            Text(
                                text = "  /  ",
                                color = TextMuted
                            )
                            Text(
                                text = "지구력 +$half",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = NeonBlue
                            )
                        }
                    }
                    if (state.statReward != state.baseStatReward) {
                        Row(
                            modifier = Modifier.padding(start = 80.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Surface(
                                shape = RoundedCornerShape(6.dp),
                                color = NeonGreen.copy(alpha = 0.15f)
                            ) {
                                Text(
                                    text = "직업 효과",
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = NeonGreen,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                )
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = statType.displayName,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextMuted,
                            modifier = Modifier.width(80.dp)
                        )
                        if (state.statReward != state.baseStatReward) {
                            // 직업 효과 적용됨 — 기본 → 최종 표시
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "+${state.baseStatReward}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "  →  ",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "+${state.statReward}",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = statType.color
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    shape = RoundedCornerShape(6.dp),
                                    color = statType.color.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = "직업 효과",
                                        style = MaterialTheme.typography.labelMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = statType.color,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        } else {
                            // 직업 효과 없음 — 최종값만 표시
                            Text(
                                text = "+${state.statReward}",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = statType.color
                            )
                        }
                    }
                }

                // Level up notification
                if (state.leveledUp) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        color = NeonPurple.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "레벨 업! Lv.${state.newLevel}",
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .fillMaxWidth(),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            textAlign = TextAlign.Center
                        )
                    }
                }

                // 클라우드 동기화 실패 안내
                if (state.syncFailed) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "클라우드 동기화에 실패했습니다. 다음 로그인 시 자동으로 동기화됩니다.",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = onGoHome,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
        ) {
            Text(
                text = "홈으로 돌아가기",
                style = MaterialTheme.typography.titleMedium
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun SummaryItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(28.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
