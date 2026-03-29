package com.bodyquest.app.ui.onboarding

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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted

private const val TOTAL_STEPS = 3

@Composable
fun OnboardingScreen(
    viewModel: OnboardingViewModel,
    onComplete: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isCompleted) {
        if (state.isCompleted) {
            onComplete()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        // Step indicator
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 48.dp, start = 24.dp, end = 24.dp, bottom = 16.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(TOTAL_STEPS) { index ->
                Box(
                    modifier = Modifier
                        .size(if (index == state.currentStep) 12.dp else 8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index <= state.currentStep) NeonPurple
                            else DarkSurfaceVariant
                        )
                )
                if (index < TOTAL_STEPS - 1) {
                    Spacer(modifier = Modifier.width(8.dp))
                }
            }
        }

        // Content
        Box(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            when (state.currentStep) {
                0 -> JobSelectionPage(
                    selectedJob = state.selectedJob,
                    onSelectJob = { viewModel.selectJob(it) }
                )
                1 -> GoalSelectionPage(
                    selectedGoal = state.selectedGoal,
                    onSelectGoal = { viewModel.selectGoal(it) }
                )
                2 -> AvatarCreationPage(
                    nickname = state.nickname,
                    avatarIndex = state.avatarIndex,
                    nicknameError = state.nicknameError,
                    onNicknameChange = { viewModel.setNickname(it) },
                    onAvatarSelect = { viewModel.setAvatarIndex(it) }
                )
            }
        }

        // Error message
        if (state.error != null) {
            Text(
                text = state.error!!,
                color = NeonRed,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
        }

        // Navigation buttons
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (state.currentStep > 0) {
                OutlinedButton(
                    onClick = { viewModel.previousStep() },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TextMuted
                    )
                ) {
                    Text("이전")
                }
                Spacer(modifier = Modifier.width(12.dp))
            }

            Button(
                onClick = {
                    if (state.currentStep < TOTAL_STEPS - 1) {
                        viewModel.nextStep()
                    } else {
                        viewModel.completeOnboarding()
                    }
                },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(12.dp),
                enabled = !state.isSaving && when (state.currentStep) {
                    0 -> state.selectedJob != null
                    1 -> state.selectedGoal != null
                    2 -> state.nickname.isNotBlank()
                    else -> true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonPurple
                )
            ) {
                Text(
                    text = when (state.currentStep) {
                        0 -> "이 직업으로 시작하기"
                        TOTAL_STEPS - 1 -> "시작하기"
                        else -> "다음"
                    },
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
