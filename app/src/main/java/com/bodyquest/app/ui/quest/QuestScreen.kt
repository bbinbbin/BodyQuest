package com.bodyquest.app.ui.quest

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.ui.theme.NeonBlue
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextSecondary

private data class WorkoutType(
    val category: String,
    val displayName: String,
    val description: String,
    val icon: String,
    val color: Color
)

private val workoutTypes = listOf(
    WorkoutType(
        category = "STRENGTH",
        displayName = "근력운동",
        description = "웨이트 트레이닝 위주\n근력과 근육량 향상",
        icon = "💪",
        color = NeonRed
    ),
    WorkoutType(
        category = "ENDURANCE",
        displayName = "유산소운동",
        description = "러닝, 사이클 등 유산소 위주\n심폐 지구력 향상",
        icon = "🏃",
        color = NeonBlue
    )
)

@Composable
fun QuestScreen(
    onCategorySelect: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp)
    ) {
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "오늘 어떤 운동을 할까요?",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "운동 종류를 선택하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(24.dp))

        workoutTypes.forEach { type ->
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
                    .clickable { onCategorySelect(type.category) },
                shape = RoundedCornerShape(16.dp),
                color = type.color.copy(alpha = 0.1f),
                border = androidx.compose.foundation.BorderStroke(1.dp, type.color.copy(alpha = 0.3f))
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(text = type.icon, fontSize = 36.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = type.displayName,
                        style = MaterialTheme.typography.titleLarge,
                        color = type.color
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = type.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
