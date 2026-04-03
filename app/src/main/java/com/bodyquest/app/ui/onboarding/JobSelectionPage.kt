package com.bodyquest.app.ui.onboarding

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary

private data class JobCardInfo(
    val catchphrase: String,
    val features: List<String>
)

private val jobCardInfo = mapOf(
    Job.STRENGTH to JobCardInfo(
        catchphrase = "몸으로 증명하는 힘",
        features = listOf("힘 성장 속도 증가", "고강도 운동에 유리")
    ),
    Job.ENDURANCE to JobCardInfo(
        catchphrase = "멈추지 않는 지속력",
        features = listOf("지구력 성장 속도 증가", "장시간 운동에 유리")
    ),
    Job.BALANCE to JobCardInfo(
        catchphrase = "지속 가능한 성장",
        features = listOf("힘과 지구력 균형 성장", "혼합 콘텐츠에 유리")
    )
)

@Composable
fun JobSelectionPage(
    selectedJob: Job?,
    onSelectJob: (Job) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 상단 타이틀
        Text(
            text = "당신의 성장 방식을 선택하세요.",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = "운동 스타일에 따라 성장 방향이 달라집니다.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 직업 카드 3개
        Job.entries.forEach { job ->
            val isSelected = selectedJob == job
            val anySelected = selectedJob != null

            val scale by animateFloatAsState(
                targetValue = if (isSelected) 1.05f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "scale_${job.name}"
            )
            val alpha by animateFloatAsState(
                targetValue = if (anySelected && !isSelected) 0.5f else 1.0f,
                animationSpec = tween(durationMillis = 200),
                label = "alpha_${job.name}"
            )

            val info = jobCardInfo[job] ?: return@forEach

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .scale(scale)
                    .alpha(alpha)
                    .clickable { onSelectJob(job) },
                shape = RoundedCornerShape(20.dp),
                color = if (isSelected) job.color.copy(alpha = 0.15f) else DarkSurfaceVariant,
                border = BorderStroke(
                    width = if (isSelected) 2.dp else 1.dp,
                    color = if (isSelected) job.color else job.color.copy(alpha = 0.2f)
                )
            ) {
                    Row(
                        modifier = Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 아이콘 원형 배경
                        Box(
                            modifier = Modifier
                                .size(56.dp)
                                .background(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            job.color.copy(alpha = 0.3f),
                                            job.color.copy(alpha = 0.05f)
                                        )
                                    ),
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = job.icon,
                                fontSize = 28.sp
                            )
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        // 텍스트 영역
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = job.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) job.color
                                else MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = info.catchphrase,
                                style = MaterialTheme.typography.bodySmall,
                                color = if (isSelected) job.color.copy(alpha = 0.8f)
                                else TextSecondary,
                                fontWeight = FontWeight.Medium
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            info.features.forEach { feature ->
                                Row(
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .size(5.dp)
                                            .background(
                                                color = job.color.copy(
                                                    alpha = if (isSelected) 0.9f else 0.4f
                                                ),
                                                shape = CircleShape
                                            )
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = feature,
                                        style = MaterialTheme.typography.labelMedium,
                                        color = if (isSelected) MaterialTheme.colorScheme.onSurface
                                        else TextMuted
                                    )
                                }
                                Spacer(modifier = Modifier.height(3.dp))
                            }
                        }
                    }
            }

            Spacer(modifier = Modifier.height(12.dp))
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 하단 안내 문구
        Text(
            text = "직업은 이후에도 변경할 수 있습니다.",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted
        )
    }
}
