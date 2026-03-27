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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
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

        Spacer(modifier = Modifier.height(32.dp))

        // Timer
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

        Spacer(modifier = Modifier.weight(1f))

        // Control buttons
        if (!state.isRunning && state.elapsedSeconds == 0) {
            // Not started yet
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
            // Paused
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
}

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
