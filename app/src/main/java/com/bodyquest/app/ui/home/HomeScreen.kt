package com.bodyquest.app.ui.home

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.domain.model.StatType
import com.bodyquest.app.ui.common.ErrorScreen
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.common.UiState
import com.bodyquest.app.ui.home.components.StatBar
import com.bodyquest.app.ui.home.components.TodayQuestCard
import com.bodyquest.app.ui.home.components.XpProgressBar
import com.bodyquest.app.ui.theme.DarkSurface
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary
import java.util.Calendar
import android.Manifest
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Icon
import com.bodyquest.app.ui.home.components.ImagePickerSheet
import com.bodyquest.app.ui.home.components.ProfileImage
import com.bodyquest.app.util.ImageUtil

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToQuest: () -> Unit = {},
    onQuestClick: (String) -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()

    val context = androidx.compose.ui.platform.LocalContext.current
    var cameraUri by androidx.compose.runtime.remember { androidx.compose.runtime.mutableStateOf<Uri?>(null) }

    val galleryLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let { viewModel.uploadProfileImage(it) }
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { success: Boolean ->
        if (success) cameraUri?.let { viewModel.uploadProfileImage(it) }
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted: Boolean ->
        if (granted) {
            val uri = ImageUtil.createTempImageUri(context)
            cameraUri = uri
            cameraLauncher.launch(uri)
        }
    }

    when (val current = uiState) {
        is UiState.Loading -> {
            LoadingScreen()
            return
        }
        is UiState.Error -> {
            ErrorScreen(message = current.message, onRetry = { viewModel.retry() })
            return
        }
        is UiState.Success -> {
            HomeContent(
                state = current.data,
                onNavigateToQuest = onNavigateToQuest,
                onQuestClick = onQuestClick,
                onProfileClick = { viewModel.showImagePicker() }
            )

            if (current.data.showImagePicker) {
                ImagePickerSheet(
                    onDismiss = { viewModel.dismissImagePicker() },
                    onGalleryClick = {
                        viewModel.dismissImagePicker()
                        galleryLauncher.launch("image/*")
                    },
                    onCameraClick = {
                        viewModel.dismissImagePicker()
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }

            current.data.imageError?.let { error ->
                androidx.compose.runtime.LaunchedEffect(error) {
                    kotlinx.coroutines.delay(3000)
                    viewModel.clearImageError()
                }
            }
        }
    }
}

@Composable
private fun HomeContent(
    state: HomeState,
    onNavigateToQuest: () -> Unit,
    onQuestClick: (String) -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val user = state.user ?: return

    val job = try { Job.valueOf(user.job) } catch (_: Exception) { Job.STRENGTH }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "BODY QUEST",
                style = MaterialTheme.typography.titleMedium,
                color = NeonPurple
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = "Lv.${user.level}",
                style = MaterialTheme.typography.labelLarge,
                color = TextSecondary
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Avatar + User Info
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                ProfileImage(
                    profileImageUrl = user.profileImageUrl,
                    avatarIndex = user.avatarIndex,
                    size = 72.dp,
                    isUploading = state.isUploadingImage,
                    onClick = onProfileClick
                )
                if (state.imageError != null) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = state.imageError,
                        style = MaterialTheme.typography.labelSmall,
                        color = com.bodyquest.app.ui.theme.NeonRed
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = user.nickname,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row {
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = job.color.copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = job.displayName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = job.color
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = NeonPurple.copy(alpha = 0.15f)
                    ) {
                        val goalName = when (user.goal) {
                            "DIET" -> "다이어트"
                            "BULK_UP" -> "벌크업"
                            "MAINTAIN" -> "유지"
                            else -> user.goal
                        }
                        Text(
                            text = goalName,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonPurple
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // XP Progress
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                XpProgressBar(level = user.level, currentXp = user.xp)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Stats
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "스탯 현황",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(14.dp))
                StatBar(
                    label = StatType.STRENGTH.displayName,
                    value = user.strengthStat,
                    color = StatType.STRENGTH.color
                )
                Spacer(modifier = Modifier.height(10.dp))
                StatBar(
                    label = StatType.ENDURANCE.displayName,
                    value = user.enduranceStat,
                    color = StatType.ENDURANCE.color
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Today's completed quests
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "오늘의 퀘스트",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (state.todaysQuests.isEmpty()) {
                    Text(
                        text = "아직 완료한 퀘스트가 없어요.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onNavigateToQuest,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(44.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
                    ) {
                        Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("퀘스트 시작하기", style = MaterialTheme.typography.labelLarge)
                    }
                } else {
                    state.todaysQuests.forEach { quest ->
                        TodayQuestCard(
                            questName = quest.questName,
                            xpEarned = quest.xpEarned
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Recommended quests
        if (state.recommendedQuests.isNotEmpty()) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceVariant
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "오늘의 추천 퀘스트",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    state.recommendedQuests.forEach { quest ->
                        val questJob = try { Job.valueOf(quest.category) } catch (_: Exception) { Job.STRENGTH }
                        val difficultyLabel = when (quest.difficulty) {
                            1 -> "초급"
                            2 -> "중급"
                            3 -> "고급"
                            else -> ""
                        }
                        Surface(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp)
                                .clickable { onQuestClick(quest.id) },
                            shape = RoundedCornerShape(12.dp),
                            color = questJob.color.copy(alpha = 0.08f)
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = questJob.icon,
                                    fontSize = 24.sp
                                )
                                Spacer(modifier = Modifier.width(12.dp))
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = quest.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Text(
                                        text = buildAnnotatedString {
                                            withStyle(SpanStyle(color = TextMuted)) {
                                                append("$difficultyLabel · ${quest.durationMinutes}분 · ")
                                            }
                                            withStyle(SpanStyle(color = NeonPurple)) {
                                                append("+ ${quest.xpReward} XP")
                                            }
                                        },
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Text(
                                    text = "→",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
        }

        // Weekly summary
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant
        ) {
            Column(
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = "주간 활동",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(12.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
                    val calendarDays = listOf(
                        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
                    )
                    dayLabels.forEachIndexed { index, label ->
                        val isActive = calendarDays[index] in state.weekWorkoutDays
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(
                                        if (isActive) NeonGreen else DarkSurface
                                    ),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isActive) {
                                    Icon(
                                        imageVector = Icons.Default.Check,
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.background,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = label,
                                style = MaterialTheme.typography.labelSmall,
                                color = if (isActive) NeonGreen else TextMuted,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}
