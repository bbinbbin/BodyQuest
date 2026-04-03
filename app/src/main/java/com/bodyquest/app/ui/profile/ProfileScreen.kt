package com.bodyquest.app.ui.profile

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
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
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Composable
fun ProfileScreen(
    onSignOut: () -> Unit = {},
    viewModel: ProfileViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val deleteState by viewModel.deleteState.collectAsState()
    var showDeleteDialog by remember { mutableStateOf(false) }

    LaunchedEffect(deleteState) {
        if (deleteState is DeleteState.Success) {
            onSignOut()
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("계정 삭제") },
            text = { Text("정말 계정을 삭제하시겠습니까?\n모든 운동 기록과 캐릭터 데이터가 삭제되며 복구할 수 없습니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount()
                    }
                ) {
                    Text("삭제", color = NeonRed)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            }
        )
    }

    when (val current = uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = current.message)
        is UiState.Success -> ProfileContent(
            state = current.data,
            deleteState = deleteState,
            onSignOut = {
                viewModel.signOut()
                onSignOut()
            },
            onDeleteClick = { showDeleteDialog = true }
        )
    }
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    deleteState: DeleteState,
    onSignOut: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "프로필",
            style = MaterialTheme.typography.titleLarge,
            color = NeonPurple
        )

        Spacer(modifier = Modifier.height(20.dp))

        // 누적 통계
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "누적 통계",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileStatCard(
                        label = "총 운동",
                        value = "${state.cumulativeStats.totalWorkouts}회",
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        label = "총 XP",
                        value = "${state.cumulativeStats.totalXp}",
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    ProfileStatCard(
                        label = "운동 시간",
                        value = ProfileViewModel.formatElapsedTime(state.cumulativeStats.totalElapsedSeconds),
                        modifier = Modifier.weight(1f)
                    )
                    ProfileStatCard(
                        label = "보스 클리어",
                        value = "${state.cumulativeStats.bossClears}회",
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // 직업 배율 안내
        if (state.userJob.isNotEmpty()) {
            val (jobName, jobColor, bonusText) = when (state.userJob) {
                "STRENGTH" -> Triple("스트렝스", NeonRed, "근력 운동 스탯 x2.0")
                "ENDURANCE" -> Triple("엔듀런스", NeonBlue, "지구력 운동 스탯 x2.0")
                "BALANCE" -> Triple("밸런스", NeonGreen, "모든 운동 스탯 x1.5")
                else -> Triple("", NeonPurple, "")
            }
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "직업 효과",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = jobColor.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = jobName,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = FontWeight.Bold,
                                color = jobColor
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = bonusText,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 주간 운동 통계
        if (state.weeklyStats.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "이번 주 운동",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    val maxCount = state.weeklyStats.maxOf { it.count }.coerceAtLeast(1)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        state.weeklyStats.forEach { stat ->
                            Column(
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Bottom,
                                modifier = Modifier.weight(1f)
                            ) {
                                if (stat.count > 0) {
                                    Text(
                                        text = "${stat.count}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonGreen
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                }
                                Box(
                                    modifier = Modifier
                                        .width(24.dp)
                                        .height(
                                            (80 * stat.count.toFloat() / maxCount).dp
                                                .coerceAtLeast(4.dp)
                                        )
                                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                                        .background(if (stat.count > 0) NeonGreen else DarkSurface)
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = stat.label,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // 운동 히스토리 달력
        WorkoutCalendarSection(calendarData = state.calendarData)

        Spacer(modifier = Modifier.height(20.dp))

        // 계정 정보
        state.accountInfo?.let { info ->
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "계정 정보",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    AccountInfoRow("이메일", info.email ?: "미설정")
                    Spacer(modifier = Modifier.height(10.dp))
                    AccountInfoRow(
                        "가입일",
                        if (info.createdAt > 0)
                            SimpleDateFormat("yyyy.MM.dd", Locale.KOREA).format(Date(info.createdAt))
                        else "알 수 없음"
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AccountInfoRow("로그인 방법", ProfileViewModel.formatAuthProvider(info.authProvider))
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // 로그아웃 / 계정 삭제
        if (deleteState is DeleteState.Loading) {
            CircularProgressIndicator(
                color = NeonPurple,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        if (deleteState is DeleteState.Error) {
            Text(
                text = (deleteState as DeleteState.Error).message,
                color = NeonRed,
                style = MaterialTheme.typography.bodySmall
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Button(
            onClick = onSignOut,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(containerColor = NeonRed)
        ) {
            Text("로그아웃", style = MaterialTheme.typography.titleSmall)
        }
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedButton(
            onClick = onDeleteClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp),
            shape = RoundedCornerShape(12.dp),
            enabled = deleteState !is DeleteState.Loading
        ) {
            Text("계정 삭제", color = NeonRed, style = MaterialTheme.typography.titleSmall)
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun WorkoutCalendarSection(calendarData: Map<String, List<WorkoutHistoryItem>>) {
    val today = Calendar.getInstance()
    var displayYear by remember { mutableIntStateOf(today.get(Calendar.YEAR)) }
    var displayMonth by remember { mutableIntStateOf(today.get(Calendar.MONTH)) }

    var selectedDateKey by remember { mutableStateOf<String?>(null) }
    var showDetailDialog by remember { mutableStateOf(false) }

    if (showDetailDialog && selectedDateKey != null) {
        val workoutsForDay = calendarData[selectedDateKey!!] ?: emptyList()
        WorkoutDetailDialog(
            dateKey = selectedDateKey!!,
            workouts = workoutsForDay,
            onDismiss = { showDetailDialog = false }
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "운동 히스토리",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(14.dp))

            // 월 네비게이션
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = {
                    if (displayMonth == 0) {
                        displayMonth = 11; displayYear--
                    } else displayMonth--
                    selectedDateKey = null
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                        contentDescription = "이전 달",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = "${displayYear}년 ${displayMonth + 1}월",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                IconButton(onClick = {
                    if (displayMonth == 11) {
                        displayMonth = 0; displayYear++
                    } else displayMonth++
                    selectedDateKey = null
                }) {
                    Icon(
                        Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "다음 달",
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 요일 헤더 (일~토)
            val dayHeaders = listOf("일", "월", "화", "수", "목", "금", "토")
            Row(modifier = Modifier.fillMaxWidth()) {
                dayHeaders.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (label == "일") NeonRed.copy(alpha = 0.7f) else TextMuted
                    )
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            // 달력 그리드
            val firstDayCal = Calendar.getInstance().apply {
                set(Calendar.YEAR, displayYear)
                set(Calendar.MONTH, displayMonth)
                set(Calendar.DAY_OF_MONTH, 1)
            }
            // DAY_OF_WEEK: SUNDAY=1..SATURDAY=7, offset for Sunday-first grid
            val firstDayOfWeek = firstDayCal.get(Calendar.DAY_OF_WEEK) - 1
            val daysInMonth = firstDayCal.getActualMaximum(Calendar.DAY_OF_MONTH)
            val todayYear = today.get(Calendar.YEAR)
            val todayMonth = today.get(Calendar.MONTH)
            val todayDay = today.get(Calendar.DAY_OF_MONTH)

            val totalCells = firstDayOfWeek + daysInMonth
            val rows = (totalCells + 6) / 7

            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        val day = cellIndex - firstDayOfWeek + 1
                        Box(
                            modifier = Modifier.weight(1f),
                            contentAlignment = Alignment.Center
                        ) {
                            if (day in 1..daysInMonth) {
                                val dateKey = String.format(
                                    "%04d-%02d-%02d",
                                    displayYear, displayMonth + 1, day
                                )
                                val hasWorkout = calendarData.containsKey(dateKey)
                                val workoutCount = calendarData[dateKey]?.size ?: 0
                                val isToday = day == todayDay &&
                                        displayMonth == todayMonth &&
                                        displayYear == todayYear
                                val isSelected = selectedDateKey == dateKey

                                CalendarDayCell(
                                    day = day,
                                    isToday = isToday,
                                    isSelected = isSelected,
                                    hasWorkout = hasWorkout,
                                    workoutCount = workoutCount,
                                    isSunday = col == 0,
                                    onClick = {
                                        if (hasWorkout) {
                                            selectedDateKey = dateKey
                                            showDetailDialog = true
                                        } else {
                                            selectedDateKey = null
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }

            // 선택된 날짜 요약 (운동 있을 때만)
            val selectedWorkouts = selectedDateKey?.let { calendarData[it] }
            if (!selectedWorkouts.isNullOrEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                HorizontalDivider(color = DarkSurface)
                Spacer(modifier = Modifier.height(12.dp))

                val representative = selectedWorkouts.first()
                val extraCount = selectedWorkouts.size - 1
                val summaryText = if (extraCount > 0)
                    "${representative.questName} 외 ${extraCount}건"
                else
                    representative.questName

                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDetailDialog = true },
                    shape = RoundedCornerShape(10.dp),
                    color = NeonPurple.copy(alpha = 0.1f)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = formatDateKeyForDisplay(selectedDateKey!!),
                                style = MaterialTheme.typography.labelSmall,
                                color = TextMuted
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = summaryText,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "자세히 보기",
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple
                        )
                    }
                }
            }

            // 운동 기록이 아예 없는 경우 안내
            if (calendarData.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "아직 운동 기록이 없어요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
            }
        }
    }
}

@Composable
private fun CalendarDayCell(
    day: Int,
    isToday: Boolean,
    isSelected: Boolean,
    hasWorkout: Boolean,
    workoutCount: Int,
    isSunday: Boolean,
    onClick: () -> Unit
) {
    val textColor = when {
        hasWorkout -> MaterialTheme.colorScheme.onSurface
        isSunday -> NeonRed.copy(alpha = 0.5f)
        else -> TextSecondary
    }

    Column(
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .then(
                if (hasWorkout || isToday) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .then(
                    when {
                        hasWorkout && isSelected ->
                            Modifier
                                .clip(CircleShape)
                                .background(NeonPurple)
                        hasWorkout ->
                            Modifier
                                .clip(CircleShape)
                                .background(NeonPurple.copy(alpha = 0.25f))
                                .border(1.dp, NeonPurple.copy(alpha = 0.6f), CircleShape)
                        isToday ->
                            Modifier
                                .clip(CircleShape)
                                .border(1.5.dp, NeonGreen.copy(alpha = 0.7f), CircleShape)
                        else -> Modifier
                    }
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "$day",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (hasWorkout) FontWeight.Bold else FontWeight.Normal,
                color = when {
                    hasWorkout && isSelected -> MaterialTheme.colorScheme.background
                    hasWorkout -> NeonPurple
                    isToday -> NeonGreen
                    else -> textColor
                }
            )
        }
        // 운동 개수 점 표시
        if (hasWorkout && workoutCount > 1) {
            Spacer(modifier = Modifier.height(1.dp))
            Text(
                text = "·".repeat(workoutCount.coerceAtMost(3)),
                style = MaterialTheme.typography.labelSmall,
                color = if (isSelected) NeonPurple else NeonPurple.copy(alpha = 0.6f),
                lineHeight = MaterialTheme.typography.labelSmall.lineHeight
            )
        }
    }
}

@Composable
private fun WorkoutDetailDialog(
    dateKey: String,
    workouts: List<WorkoutHistoryItem>,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "${formatDateKeyForDisplay(dateKey)} 운동 기록",
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Column {
                workouts.forEachIndexed { index, item ->
                    WorkoutDetailRow(item = item, isRepresentative = index == 0 && workouts.size > 1)
                    if (index < workouts.lastIndex) {
                        HorizontalDivider(
                            modifier = Modifier.padding(vertical = 10.dp),
                            color = DarkSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("닫기")
            }
        }
    )
}

@Composable
private fun WorkoutDetailRow(item: WorkoutHistoryItem, isRepresentative: Boolean) {
    val statColor = when (item.statType) {
        "STRENGTH" -> NeonRed
        "ENDURANCE" -> NeonBlue
        else -> NeonOrange
    }
    val statLabel = when (item.statType) {
        "STRENGTH" -> "근력"
        "ENDURANCE" -> "지구력"
        else -> "균형"
    }

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = item.questName,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                if (isRepresentative) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = NeonPurple.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = "대표",
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = ProfileViewModel.formatElapsedTime(item.elapsedSeconds),
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = "+ ${item.xpEarned} XP",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                color = NeonPurple
            )
            if (item.statGained > 0) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "$statLabel + ${item.statGained}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = statColor
                )
            }
        }
    }
}

private fun formatDateKeyForDisplay(dateKey: String): String {
    // dateKey: "yyyy-MM-dd" → "M월 D일"
    return try {
        val parts = dateKey.split("-")
        val month = parts[1].toInt()
        val day = parts[2].toInt()
        "${month}월 ${day}일"
    } catch (_: Exception) {
        dateKey
    }
}

@Composable
private fun ProfileStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = DarkSurface
    ) {
        Column(
            modifier = Modifier.padding(14.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = NeonGreen
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted
            )
        }
    }
}

@Composable
private fun AccountInfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}
