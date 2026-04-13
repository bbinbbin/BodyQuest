package com.bodyquest.app.ui.workout

import androidx.compose.foundation.background
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
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.layout.ContentScale
import coil.compose.AsyncImage
import com.bodyquest.app.domain.model.ExerciseImages
import com.bodyquest.app.domain.model.ExerciseInputType
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.theme.DarkBorder
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextPrimary
import com.bodyquest.app.ui.theme.TextSecondary

@Composable
fun WorkoutScreen(
    questId: String,
    viewModel: WorkoutViewModel,
    onComplete: (Long) -> Unit,
    onCancel: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(questId) {
        viewModel.loadQuest(questId)
    }

    // Navigate to complete screen when workout is done
    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted && state.workoutId > 0) {
            onComplete(state.workoutId)
        }
    }

    val quest = state.quest
    if (quest == null) {
        LoadingScreen()
        return
    }

    val job = try { Job.valueOf(quest.category) } catch (_: Exception) { Job.STRENGTH }

    Box(modifier = Modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = quest.name,
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Text(
                    text = job.displayName,
                    style = MaterialTheme.typography.bodySmall,
                    color = job.color
                )
            }
            IconButton(onClick = onCancel) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "취소",
                    tint = TextMuted
                )
            }
        }

        val isStrength = quest.category == "STRENGTH"
        val inputType = ExerciseInputType.valueOf(quest.inputType)
        val isTimeOnly = isStrength && inputType == ExerciseInputType.TIME_ONLY

        Spacer(modifier = Modifier.height(32.dp))

        if (isTimeOnly) {
            // ── TIME_ONLY STRENGTH (플랭크 등): 세트별 타이머 UI ──

            // 운동 가이드 카드
            if (state.showGuide) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("운동 가이드", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                            IconButton(onClick = { viewModel.toggleGuide() }) {
                                Icon(Icons.Default.VisibilityOff, "가이드 숨기기", tint = TextMuted, modifier = Modifier.size(20.dp))
                            }
                        }
                        val gifPath = ExerciseImages.getGifPath(quest.id)
                        if (gifPath != null) {
                            AsyncImage(
                                model = gifPath,
                                contentDescription = quest.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(120.dp)
                            )
                        } else {
                            Icon(Icons.Default.FitnessCenter, null, tint = NeonPurple.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(quest.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { viewModel.toggleGuide() }) {
                        Icon(Icons.Default.Visibility, "가이드 보기", tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // 세트 +/- 컨트롤 (타이머 실행 전에만 활성화)
            val canEditSets = !state.isRunning && state.setElapsedSeconds == 0
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.removeSet() }, enabled = canEditSets) {
                    Text(
                        "−", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = if (canEditSets) TextMuted else TextMuted.copy(alpha = 0.3f)
                    )
                }
                Text(
                    text = "${state.totalSets} 세트",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { viewModel.addSet() }, enabled = canEditSets) {
                    Text(
                        "+", fontSize = 24.sp, fontWeight = FontWeight.Bold,
                        color = if (canEditSets) NeonPurple else NeonPurple.copy(alpha = 0.3f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 세트 진행 표시 + 진행 바
            Text(
                text = "세트 ${state.completedSets + 1} / ${state.totalSets}",
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { state.completedSets.toFloat() / state.totalSets },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = NeonPurple,
                trackColor = DarkSurfaceVariant,
            )

            Spacer(modifier = Modifier.height(24.dp))


            if (!state.isRunning && state.setElapsedSeconds == 0) {
                // ── 목표 시간 설정 카드 ──
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "목표 시간",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextMuted
                        )
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            OutlinedTextField(
                                value = state.targetMinutesInput,
                                onValueChange = { viewModel.updateTargetMinutes(it) },
                                modifier = Modifier.width(72.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = strengthTextFieldColors()
                            )
                            Text(
                                text = "분",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                            OutlinedTextField(
                                value = state.targetSecondsInput,
                                onValueChange = { viewModel.updateTargetSeconds(it) },
                                modifier = Modifier.width(72.dp),
                                singleLine = true,
                                textStyle = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    textAlign = TextAlign.Center
                                ),
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                colors = strengthTextFieldColors()
                            )
                            Text(
                                text = "초",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextSecondary,
                                modifier = Modifier.padding(horizontal = 8.dp)
                            )
                        }
                    }
                }
            } else {
                // ── 타이머 실행 중 ──
                val goalReached = state.setElapsedSeconds >= state.targetDuration
                val setMin = state.setElapsedSeconds / 60
                val setSec = state.setElapsedSeconds % 60
                Text(
                    text = "%02d:%02d".format(setMin, setSec),
                    fontSize = 64.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (goalReached) NeonGreen else MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(8.dp))

                // 목표 달성 여부 표시
                val targetMin = state.targetDuration / 60
                val targetSec = state.targetDuration % 60
                Text(
                    text = if (goalReached) "목표 달성!" else "목표: %02d:%02d".format(targetMin, targetSec),
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (goalReached) NeonGreen else TextMuted
                )

                Spacer(modifier = Modifier.height(20.dp))

                // 통계
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatChip(
                        icon = Icons.Default.Favorite,
                        value = if (state.heartRate > 0) "${state.heartRate}" else "--",
                        label = "BPM",
                        color = NeonRed
                    )
                    StatChip(
                        icon = Icons.Default.LocalFireDepartment,
                        value = "${state.caloriesBurned}",
                        label = "kcal",
                        color = com.bodyquest.app.ui.theme.NeonBlue
                    )
                }
            }

        } else if (isStrength) {
            // ── STRENGTH 테이블 UI (WEIGHT_REPS / REPS_ONLY / MIXED) ──

            // 운동 가이드 카드
            if (state.showGuide) {
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("운동 가이드", style = MaterialTheme.typography.labelMedium, color = TextMuted)
                            IconButton(onClick = { viewModel.toggleGuide() }) {
                                Icon(Icons.Default.VisibilityOff, "가이드 숨기기", tint = TextMuted, modifier = Modifier.size(20.dp))
                            }
                        }
                        val gifPath = ExerciseImages.getGifPath(quest.id)
                        if (gifPath != null) {
                            AsyncImage(
                                model = gifPath,
                                contentDescription = quest.name,
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.size(120.dp)
                            )
                        } else {
                            Icon(Icons.Default.FitnessCenter, null, tint = NeonPurple.copy(alpha = 0.6f), modifier = Modifier.size(64.dp))
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(quest.description, style = MaterialTheme.typography.bodySmall, color = TextMuted)
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
            } else {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                    IconButton(onClick = { viewModel.toggleGuide() }) {
                        Icon(Icons.Default.Visibility, "가이드 보기", tint = TextMuted, modifier = Modifier.size(20.dp))
                    }
                }
            }

            // 세트 +/- 컨트롤
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { viewModel.removeSet() }) {
                    Text("−", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = TextMuted)
                }
                Text(
                    text = "세트",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                IconButton(onClick = { viewModel.addSet() }) {
                    Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = NeonPurple)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 세트 테이블 헤더
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("", modifier = Modifier.width(36.dp)) // 번호 자리
                when (inputType) {
                    ExerciseInputType.WEIGHT_REPS -> {
                        Text("kg", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        Text("횟수", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                    ExerciseInputType.REPS_ONLY -> {
                        Text("횟수", modifier = Modifier.weight(2f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                    ExerciseInputType.MIXED -> {
                        Text("횟수", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                        Text("초", modifier = Modifier.weight(1f), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = TextMuted)
                    }
                    else -> {} // TIME_ONLY는 이 브랜치에 없음
                }
                Text("", modifier = Modifier.width(48.dp)) // 체크 자리
            }

            Spacer(modifier = Modifier.height(4.dp))

            // 세트 행들
            state.setRows.forEachIndexed { index, row ->
                val hasError = state.setRowError == index
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // 세트 번호
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (row.completed) NeonPurple.copy(alpha = 0.2f) else DarkSurfaceVariant,
                            modifier = Modifier.size(36.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Text(
                                    text = "${row.setNumber}",
                                    fontWeight = FontWeight.Bold,
                                    color = if (row.completed) NeonPurple else TextMuted
                                )
                            }
                        }

                        // inputType별 입력 필드
                        when (inputType) {
                            ExerciseInputType.WEIGHT_REPS -> {
                                OutlinedTextField(
                                    value = row.weight,
                                    onValueChange = { viewModel.updateSetWeight(index, it) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                    enabled = !row.completed,
                                    singleLine = true,
                                    isError = hasError,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                                    colors = strengthTextFieldColors()
                                )
                                OutlinedTextField(
                                    value = row.reps,
                                    onValueChange = { viewModel.updateSetReps(index, it) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                    enabled = !row.completed,
                                    singleLine = true,
                                    isError = hasError,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = strengthTextFieldColors()
                                )
                            }
                            ExerciseInputType.REPS_ONLY -> {
                                OutlinedTextField(
                                    value = row.reps,
                                    onValueChange = { viewModel.updateSetReps(index, it) },
                                    modifier = Modifier.weight(2f).padding(horizontal = 4.dp),
                                    enabled = !row.completed,
                                    singleLine = true,
                                    isError = hasError,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = strengthTextFieldColors()
                                )
                            }
                            ExerciseInputType.MIXED -> {
                                OutlinedTextField(
                                    value = row.reps,
                                    onValueChange = { viewModel.updateSetReps(index, it) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                    enabled = !row.completed,
                                    singleLine = true,
                                    isError = hasError,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = strengthTextFieldColors()
                                )
                                OutlinedTextField(
                                    value = row.durationSeconds,
                                    onValueChange = { viewModel.updateSetDuration(index, it) },
                                    modifier = Modifier.weight(1f).padding(horizontal = 4.dp),
                                    enabled = !row.completed,
                                    singleLine = true,
                                    isError = hasError,
                                    textStyle = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center),
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    colors = strengthTextFieldColors()
                                )
                            }
                            else -> {} // TIME_ONLY는 이 브랜치에 없음
                        }

                        // 체크 버튼
                        IconButton(
                            onClick = { viewModel.completeSetRow(index) },
                            enabled = !row.completed,
                            modifier = Modifier.size(48.dp)
                        ) {
                            Surface(
                                shape = CircleShape,
                                color = if (row.completed) NeonPurple else DarkSurfaceVariant,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = "세트 완료",
                                        tint = if (row.completed) MaterialTheme.colorScheme.onPrimary else TextMuted,
                                        modifier = Modifier.size(20.dp)
                                    )
                                }
                            }
                        }
                    }

                    // 검증 에러 메시지
                    if (hasError) {
                        Text(
                            text = when (inputType) {
                                ExerciseInputType.WEIGHT_REPS -> "무게와 횟수를 입력해주세요."
                                ExerciseInputType.REPS_ONLY -> "횟수를 입력해주세요."
                                ExerciseInputType.MIXED -> "횟수 또는 시간을 입력해주세요."
                                else -> ""
                            },
                            color = NeonRed,
                            style = MaterialTheme.typography.labelSmall,
                            modifier = Modifier.padding(start = 44.dp, bottom = 4.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 경과 시간
            val minutes = state.elapsedSeconds / 60
            val seconds = state.elapsedSeconds % 60
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                style = MaterialTheme.typography.titleMedium,
                color = TextMuted
            )

        } else {
            // ── ENDURANCE/BALANCE: 기존 타이머 중심 UI ──
            val minutes = state.elapsedSeconds / 60
            val seconds = state.elapsedSeconds % 60
            Text(
                text = "%02d:%02d".format(minutes, seconds),
                fontSize = 64.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Progress
            if (quest.sets > 1) {
                val progress = state.completedSets.toFloat() / quest.sets
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "세트 ${state.completedSets}/${quest.sets}",
                        style = MaterialTheme.typography.titleMedium,
                        color = NeonPurple
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    LinearProgressIndicator(
                        progress = { progress },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(8.dp)
                            .clip(RoundedCornerShape(4.dp)),
                        color = NeonPurple,
                        trackColor = DarkSurfaceVariant,
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "세트당 ${quest.repsPerSet}회",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Stats row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                StatChip(
                    icon = Icons.Default.Favorite,
                    value = if (state.heartRate > 0) "${state.heartRate}" else "--",
                    label = "BPM",
                    color = NeonRed
                )
                StatChip(
                    icon = Icons.Default.LocalFireDepartment,
                    value = "${state.caloriesBurned}",
                    label = "kcal",
                    color = com.bodyquest.app.ui.theme.NeonBlue
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Control buttons
        if (isTimeOnly) {
            val goalReached = state.isRunning && state.setElapsedSeconds >= state.targetDuration
            when {
                goalReached -> {
                    // 목표 달성 → 완료 버튼
                    Button(
                        onClick = { viewModel.completeTimeSet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.Check, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "세트 완료 (${state.completedSets + 1}/${state.totalSets})",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
                state.isRunning -> {
                    // 실행 중, 목표 미달 → 정지 버튼 (세트 무효)
                    OutlinedButton(
                        onClick = { viewModel.cancelTimeSet() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp)
                    ) {
                        Icon(Icons.Default.Pause, contentDescription = null, tint = NeonRed)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("정지 (세트 무효)", color = NeonRed, style = MaterialTheme.typography.titleMedium)
                    }
                }
                else -> {
                    // 시작 전 또는 세트 간 휴식 → 시작 버튼
                    val targetSecs = (state.targetMinutesInput.toIntOrNull() ?: 0) * 60 +
                        (state.targetSecondsInput.toIntOrNull() ?: 0)
                    Button(
                        onClick = { viewModel.startTimeSet() },
                        enabled = targetSecs > 0,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = if (state.completedSets == 0) "운동 시작" else "세트 ${state.completedSets + 1} 시작",
                            style = MaterialTheme.typography.titleMedium
                        )
                    }
                }
            }
        } else if (isStrength) {
            // STRENGTH 테이블: 세트별 체크로 완료하므로 버튼 없음
            // (모든 세트 체크 완료 시 자동으로 finishWorkout 호출됨)
        } else if (!state.isRunning && state.elapsedSeconds == 0) {
            // Not started yet (ENDURANCE/BALANCE)
            Button(
                onClick = { viewModel.startWorkout() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
            ) {
                Icon(Icons.Default.PlayArrow, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("운동 시작", style = MaterialTheme.typography.titleMedium)
            }
        } else if (state.isRunning) {
            // Running - show set complete + pause
            if (quest.sets > 1) {
                Button(
                    onClick = { viewModel.completeSet() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                ) {
                    Text(
                        text = "세트 완료 (${state.completedSets + 1}/${quest.sets})",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
                Spacer(modifier = Modifier.height(12.dp))
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { viewModel.pauseWorkout() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null, tint = TextSecondary)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("일시정지", color = TextSecondary)
                }

                if (quest.sets <= 1) {
                    Button(
                        onClick = { viewModel.completeSet() },
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Text("운동 완료")
                    }
                }
            }
        } else {
            // Paused (ENDURANCE/BALANCE)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onCancel,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text("그만하기", color = NeonRed)
                }
                Button(
                    onClick = { viewModel.resumeWorkout() },
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("계속하기")
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }

    // 휴식 타이머 풀스크린 오버레이
    if (state.isResting) {
        RestTimerOverlay(
            remaining = state.restTimerSeconds,
            total = state.restTimerTotal,
            completedSets = state.completedSets,
            totalSets = state.totalSets,
            onSkip = { viewModel.skipRestTimer() }
        )
    }
    } // Box
}

@Composable
private fun strengthTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = NeonPurple,
    unfocusedLabelColor = TextMuted
)

@Composable
private fun StatChip(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: androidx.compose.ui.graphics.Color
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Column {
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
    }
}

@Composable
private fun RestTimerOverlay(
    remaining: Int,
    total: Int,
    completedSets: Int,
    totalSets: Int,
    onSkip: () -> Unit
) {
    val progress = if (total > 0) remaining.toFloat() / total else 0f
    val min = remaining / 60
    val sec = remaining % 60
    val arcColor = NeonPurple
    val trackColor = NeonPurple.copy(alpha = 0.15f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xF0050510)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "휴식 중",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "세트 $completedSets / $totalSets 완료",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(40.dp))

            // 원형 타이머
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(180.dp)) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val strokeWidth = 10.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = androidx.compose.ui.geometry.Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val arcSize = androidx.compose.ui.geometry.Size(diameter, diameter)
                    drawArc(
                        color = trackColor,
                        startAngle = -90f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = arcColor,
                        startAngle = -90f,
                        sweepAngle = 360f * progress,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
                    )
                }
                Text(
                    text = "%d:%02d".format(min, sec),
                    fontSize = 48.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "다음 세트를 준비하세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(40.dp))

            Button(
                onClick = onSkip,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 40.dp)
                    .height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                Text(
                    text = "건너뛰기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}
