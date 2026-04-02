package com.bodyquest.app.ui.boss

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.domain.model.BattleLog
import com.bodyquest.app.domain.model.BossResult
import com.bodyquest.app.domain.model.LogType
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
import com.bodyquest.app.ui.theme.XpGold

// ─── StatStatus ───────────────────────────────────────────────────────────────

private enum class StatStatus { SUFFICIENT, NORMAL, LOW }

private fun getStatStatus(current: Int, required: Int): StatStatus {
    val ratio = current.toFloat() / required.toFloat()
    return when {
        ratio >= 1.0f -> StatStatus.SUFFICIENT
        ratio >= 0.7f -> StatStatus.NORMAL
        else          -> StatStatus.LOW
    }
}

private fun getGaugeRatio(current: Int, required: Int): Float =
    (current.toFloat() / required.toFloat()).coerceAtMost(1.0f)

private fun getStatusText(status: StatStatus) = when (status) {
    StatStatus.SUFFICIENT -> "충분함"
    StatStatus.NORMAL     -> "가능"
    StatStatus.LOW        -> "부족"
}

@Composable
private fun statusColor(status: StatStatus) = when (status) {
    StatStatus.SUFFICIENT -> NeonGreen
    StatStatus.NORMAL     -> NeonOrange
    StatStatus.LOW        -> NeonRed
}

// ─── 실패 메시지 빌더 ─────────────────────────────────────────────────────────

private fun buildFailureMessage(result: BossResult): String {
    val missing = buildList {
        if (result.missingStrength > 0)  add("근력")
        if (result.missingEndurance > 0) add("지구력")
        if (result.missingLevel > 0)     add("레벨")
    }
    return when (missing.size) {
        0    -> "아슬아슬하게 패배했습니다."
        1    -> "${missing[0]}이 부족합니다."
        2    -> "${missing[0]}과 ${missing[1]}이 부족합니다."
        else -> "${missing.dropLast(1).joinToString(", ")}과 ${missing.last()}이 부족합니다."
    }
}

private fun buildMotivationMessage(result: BossResult): String {
    val onlyStr = result.missingStrength > 0 && result.missingEndurance == 0 && result.missingLevel == 0
    val onlyEnd = result.missingEndurance > 0 && result.missingStrength == 0 && result.missingLevel == 0
    val onlyLvl = result.missingLevel > 0 && result.missingStrength == 0 && result.missingEndurance == 0
    return when {
        onlyStr -> "근력 운동을 추천합니다."
        onlyEnd -> "지구력 운동을 추천합니다."
        onlyLvl -> "조금 더 운동하면 레벨이 올라갑니다."
        else    -> "꾸준한 운동으로 강해질 수 있습니다."
    }
}

// ─── 화면 진입점 ──────────────────────────────────────────────────────────────

@Composable
fun BossScreen(viewModel: BossViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (val current = uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = current.message, onRetry = { viewModel.retry() })
        is UiState.Success -> BossContent(
            state = current.data,
            onChallenge = { viewModel.challengeBoss(it) },
            onConfirmBattle = { viewModel.confirmBattle() },
            onDismissResult = { viewModel.dismissResult() }
        )
    }
}

// ─── 보스 목록 화면 ───────────────────────────────────────────────────────────

@Composable
private fun BossContent(
    state: BossState,
    onChallenge: (BossWithProgress) -> Unit,
    onConfirmBattle: () -> Unit,
    onDismissResult: () -> Unit
) {
    val groupOrder = listOf("STRENGTH", "ENDURANCE", "HYBRID")

    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(top = 20.dp, bottom = 24.dp)
        ) {
            item {
                Text(
                    text = "보스 도전",
                    style = MaterialTheme.typography.titleLarge,
                    color = NeonPurple,
                    modifier = Modifier.padding(horizontal = 20.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))

                state.user?.let { user ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 20.dp),
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceEvenly
                        ) {
                            StatChip("레벨", user.level.toString(), NeonPurple)
                            StatChip("근력", user.strengthStat.toString(), NeonRed)
                            StatChip("지구력", user.enduranceStat.toString(), NeonBlue)
                        }
                    }
                }
                Spacer(modifier = Modifier.height(28.dp))
            }

            groupOrder.forEach { type ->
                val bosses = state.bossGroups[type] ?: return@forEach
                val typeColor = when (type) {
                    "STRENGTH"  -> NeonRed
                    "ENDURANCE" -> NeonBlue
                    else        -> NeonPurple
                }
                val typeLabel = when (type) {
                    "STRENGTH"  -> "근력 보스"
                    "ENDURANCE" -> "지구력 보스"
                    else        -> "하이브리드 보스"
                }
                val typeEmoji = when (type) {
                    "STRENGTH"  -> "💪"
                    "ENDURANCE" -> "🏃"
                    else        -> "⚡"
                }

                item {
                    Row(
                        modifier = Modifier.padding(horizontal = 20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = typeEmoji, fontSize = 18.sp)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = typeLabel,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = typeColor
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))

                    LazyRow(
                        contentPadding = PaddingValues(horizontal = 20.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(bosses) { bwp ->
                            BossCard(
                                bwp = bwp,
                                user = state.user,
                                typeColor = typeColor,
                                onChallenge = onChallenge
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(28.dp))
                }
            }
        }

        AnimatedVisibility(
            visible = state.isBattleActive,
            enter = fadeIn(tween(300)) + slideInVertically(tween(300)) { it / 4 },
            exit = fadeOut(tween(200))
        ) {
            BattleOverlay(
                bossName = state.battleResult?.bossName ?: "",
                isSuccess = state.battleResult?.success ?: false,
                performance = state.battleResult?.performance ?: "",
                logs = state.battleLogs,
                isComplete = state.isBattleComplete,
                onConfirm = onConfirmBattle
            )
        }

        state.challengeResult?.let { result ->
            ChallengeResultDialog(result = result, onDismiss = onDismissResult)
        }
    }
}

// ─── 보스 카드 ────────────────────────────────────────────────────────────────

@Composable
private fun BossCard(
    bwp: BossWithProgress,
    user: UserEntity?,
    typeColor: Color,
    onChallenge: (BossWithProgress) -> Unit
) {
    val boss = bwp.boss
    val dimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.3f)

    Surface(
        modifier = Modifier
            .width(220.dp)
            .height(230.dp),
        shape = RoundedCornerShape(14.dp),
        color = DarkSurfaceVariant
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = boss.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = if (bwp.isLocked) dimColor else MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (bwp.isCleared) {
                // 클리어 완료: 등급 뱃지만 표시
                val gradeColor = when (bwp.clearedGrade) {
                    "S"  -> NeonPurple
                    "A"  -> NeonGreen
                    else -> NeonOrange   // B
                }
                val gradeLabel = when (bwp.clearedGrade) {
                    "S"  -> "압도적 클리어"
                    "A"  -> "안정적 클리어"
                    else -> "간신히 클리어"
                }
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = bwp.clearedGrade,
                        fontSize = 48.sp,
                        fontWeight = FontWeight.Bold,
                        color = gradeColor
                    )
                    Text(
                        text = gradeLabel,
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                }
            } else {
                // 미클리어: 요구 조건 게이지
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    RequirementGauge(
                        label = "레벨",
                        required = boss.requiredLevel,
                        current = user?.level ?: 0,
                        dimmed = bwp.isLocked
                    )
                    if (boss.requiredStrength > 0) {
                        RequirementGauge(
                            label = "근력",
                            required = boss.requiredStrength,
                            current = user?.strengthStat ?: 0,
                            dimmed = bwp.isLocked
                        )
                    }
                    if (boss.requiredEndurance > 0) {
                        RequirementGauge(
                            label = "지구력",
                            required = boss.requiredEndurance,
                            current = user?.enduranceStat ?: 0,
                            dimmed = bwp.isLocked
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            when {
                bwp.isLocked -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        color = DarkSurface
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = null,
                                tint = dimColor,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("잠금", style = MaterialTheme.typography.labelLarge, color = dimColor)
                        }
                    }
                }
                bwp.isCleared -> {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            color = NeonGreen.copy(alpha = 0.12f)
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = NeonGreen,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text("클리어 완료", style = MaterialTheme.typography.labelLarge, color = NeonGreen)
                            }
                        }
                        Button(
                            onClick = { onChallenge(bwp) },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = typeColor.copy(alpha = 0.6f)
                            )
                        ) {
                            Text("재도전", style = MaterialTheme.typography.labelLarge)
                        }
                    }
                }
                else -> {
                    Button(
                        onClick = { onChallenge(bwp) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(10.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = typeColor)
                    ) {
                        Text("도전하기", style = MaterialTheme.typography.labelLarge)
                    }
                }
            }
        }
    }
}

// ─── 요구 조건 게이지 ─────────────────────────────────────────────────────────

@Composable
private fun RequirementGauge(
    label: String,
    required: Int,
    current: Int,
    dimmed: Boolean = false
) {
    if (dimmed) {
        // 잠금 상태: 라벨 + 빈 회색 게이지
        val dimColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.2f)
        Column {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = dimColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(dimColor.copy(alpha = 0.15f))
            )
        }
        return
    }

    val status = getStatStatus(current, required)
    val targetRatio = getGaugeRatio(current, required)
    val animatedRatio by animateFloatAsState(
        targetValue = targetRatio,
        animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing),
        label = "gauge_$label"
    )
    val color = statusColor(status)
    val statusText = getStatusText(status)

    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondary
            )
            Text(
                text = statusText,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        // 게이지 바
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(5.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(color.copy(alpha = 0.15f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedRatio)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(3.dp))
                    .background(color)
            )
        }
    }
}

// ─── 공통 컴포넌트 ────────────────────────────────────────────────────────────

@Composable
private fun StatChip(label: String, value: String, color: Color) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = value, style = MaterialTheme.typography.titleMedium, color = color)
        Text(text = label, style = MaterialTheme.typography.labelSmall, color = TextMuted)
    }
}

// ─── 전투 오버레이 ────────────────────────────────────────────────────────────

@Composable
private fun BattleOverlay(
    bossName: String,
    isSuccess: Boolean,
    performance: String,
    logs: List<BattleLog>,
    isComplete: Boolean,
    onConfirm: () -> Unit
) {
    val listState = rememberLazyListState()

    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) listState.animateScrollToItem(logs.lastIndex)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0050510))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp, vertical = 40.dp)
        ) {
            Text(
                text = "⚔ $bossName",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = NeonRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "전투 진행 중...",
                style = MaterialTheme.typography.labelMedium,
                color = TextMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
            if (isComplete && isSuccess && performance.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                val perfColor = when (performance) {
                    "압도적인 승리" -> NeonPurple
                    "안정적인 승리" -> NeonGreen
                    else           -> NeonOrange
                }
                Surface(
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                    shape = RoundedCornerShape(20.dp),
                    color = perfColor.copy(alpha = 0.18f)
                ) {
                    Text(
                        text = performance,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.Bold,
                        color = perfColor,
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp)
                    )
                }
            }
            Spacer(modifier = Modifier.height(24.dp))

            LazyColumn(
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                itemsIndexed(logs) { _, log -> BattleLogItem(log = log) }
            }

            Spacer(modifier = Modifier.height(24.dp))

            AnimatedVisibility(visible = isComplete, enter = fadeIn(tween(500))) {
                Button(
                    onClick = onConfirm,
                    modifier = Modifier.fillMaxWidth().height(52.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isSuccess) NeonGreen else NeonRed
                    )
                ) {
                    Text(
                        text = if (isSuccess) "${performance}! 돌아가기" else "확인",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
            if (!isComplete) Spacer(modifier = Modifier.height(52.dp))
        }
    }
}

@Composable
private fun BattleLogItem(log: BattleLog) {
    val (textColor, fontWeight, prefix) = when (log.type) {
        LogType.START    -> Triple(MaterialTheme.colorScheme.onSurface, FontWeight.Normal,   "▶")
        LogType.ATTACK   -> Triple(NeonOrange,  FontWeight.SemiBold, "💥")
        LogType.REACTION -> Triple(NeonGreen,   FontWeight.Normal,   "✨")
        LogType.CRISIS   -> Triple(NeonRed,     FontWeight.SemiBold, "⚠")
        LogType.FINISH   -> Triple(NeonPurple,  FontWeight.Bold,     "🔥")
        LogType.RESULT   -> Triple(XpGold,      FontWeight.Bold,     "★")
    }
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = prefix, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.width(28.dp))
        Text(
            text = log.message,
            style = if (log.type == LogType.RESULT) MaterialTheme.typography.titleMedium
                    else MaterialTheme.typography.bodyMedium,
            fontWeight = fontWeight,
            color = textColor
        )
    }
}

// ─── 실패 다이얼로그 ──────────────────────────────────────────────────────────

@Composable
private fun ChallengeResultDialog(result: BossResult, onDismiss: () -> Unit) {
    val failureMsg    = buildFailureMessage(result)
    val motivationMsg = buildMotivationMessage(result)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = {
            Text(
                text = "도전 실패",
                style = MaterialTheme.typography.titleMedium,
                color = NeonRed
            )
        },
        text = {
            Column {
                Text(
                    text = failureMsg,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = NeonOrange.copy(alpha = 0.1f)
                ) {
                    Text(
                        text = motivationMsg,
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonOrange,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)
                    )
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
