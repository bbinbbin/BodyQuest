package com.bodyquest.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.common.ErrorScreen
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.common.UiState
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import com.bodyquest.app.ui.theme.NeonBlue
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary

@Composable
fun QuestTreeScreen(
    category: String,
    viewModel: QuestViewModel,
    onQuestSelect: (String) -> Unit,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(category) {
        viewModel.loadCategory(category)
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
        is UiState.Success -> {}
    }

    val state = (uiState as UiState.Success).data
    val (categoryDisplayName, categoryColor) = when (category) {
        "ENDURANCE" -> "유산소운동" to NeonBlue
        "BALANCE" -> "균형운동" to NeonGreen
        else -> "근력운동" to NeonRed
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .verticalScroll(rememberScrollState())
            .padding(20.dp)
    ) {
        // Header with back button
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = {
                if (state.treeLevel == TreeLevel.QUEST_LIST) {
                    viewModel.goBackToBodyParts()
                } else {
                    onBack()
                }
            }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "뒤로",
                    tint = MaterialTheme.colorScheme.onSurface
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = categoryDisplayName,
                    style = MaterialTheme.typography.titleLarge,
                    color = categoryColor
                )
                if (state.selectedBodyPart != null) {
                    Text(
                        text = state.selectedBodyPart!!,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        when (state.treeLevel) {
            TreeLevel.BODY_PART -> {
                Text(
                    text = "운동 부위를 선택하세요",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                state.bodyParts.forEach { bodyPart ->
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { viewModel.selectBodyPart(bodyPart) },
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = bodyPart,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Spacer(modifier = Modifier.weight(1f))
                            Text(
                                text = "→",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextMuted
                            )
                        }
                    }
                }
            }

            TreeLevel.QUEST_LIST -> {
                Text(
                    text = "퀘스트를 선택하세요",
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onBackground
                )
                Spacer(modifier = Modifier.height(16.dp))

                state.quests.forEach { quest ->
                    val difficultyLabel = when (quest.difficulty) {
                        1 -> "초급"
                        2 -> "중급"
                        3 -> "고급"
                        else -> ""
                    }
                    val difficultyColor = when (quest.difficulty) {
                        1 -> com.bodyquest.app.ui.theme.NeonGreen
                        2 -> com.bodyquest.app.ui.theme.NeonBlue
                        3 -> com.bodyquest.app.ui.theme.NeonRed
                        else -> TextMuted
                    }

                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .clickable { onQuestSelect(quest.id) },
                        shape = RoundedCornerShape(12.dp),
                        color = DarkSurfaceVariant
                    ) {
                        Column(modifier = Modifier.padding(16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = quest.name,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    modifier = Modifier.weight(1f)
                                )
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = difficultyColor.copy(alpha = 0.15f)
                                ) {
                                    Text(
                                        text = difficultyLabel,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                                        style = MaterialTheme.typography.labelSmall,
                                        color = difficultyColor
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = quest.description,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = buildAnnotatedString {
                                    withStyle(SpanStyle(color = TextMuted)) {
                                        append("${quest.durationMinutes}분")
                                        if (quest.sets > 1) {
                                            append(" · ${quest.sets}세트 x ${quest.repsPerSet}회")
                                        }
                                        append(" · ")
                                    }
                                    withStyle(SpanStyle(color = NeonPurple)) {
                                        append("+ ${quest.xpReward} XP")
                                    }
                                },
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
                }
            }
        }
    }
}
