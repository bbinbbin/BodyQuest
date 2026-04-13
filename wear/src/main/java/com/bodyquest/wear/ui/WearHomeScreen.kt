package com.bodyquest.wear.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.wear.compose.foundation.lazy.ScalingLazyColumn
import androidx.wear.compose.foundation.lazy.rememberScalingLazyListState
import androidx.wear.compose.material.CompactChip
import androidx.wear.compose.material.MaterialTheme
import androidx.wear.compose.material.Text
import com.bodyquest.wear.R

@Composable
fun WearHomeScreen(viewModel: WearHomeViewModel = hiltViewModel()) {
    val state by viewModel.state.collectAsState()
    val listState = rememberScalingLazyListState()

    ScalingLazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        state = listState
    ) {
        item {
            Image(
                painter = painterResource(id = R.drawable.ic_app_logo),
                contentDescription = "BodyQuest 로고",
                modifier = Modifier.size(48.dp)
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            Text(
                text = "BodyQuest",
                style = MaterialTheme.typography.title2,
                color = MaterialTheme.colors.primary,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(modifier = Modifier.height(4.dp))
        }
        item {
            val statusText: String
            val statusColor: Color
            when {
                state.isChecking -> {
                    statusText = "연결 확인 중..."
                    statusColor = MaterialTheme.colors.onSurfaceVariant
                }
                state.isPhoneConnected -> {
                    statusText = "폰 연결됨"
                    statusColor = Color(0xFF10B981)
                }
                else -> {
                    statusText = "폰 연결 안됨"
                    statusColor = MaterialTheme.colors.error
                }
            }
            Text(
                text = statusText,
                style = MaterialTheme.typography.body2,
                color = statusColor,
                textAlign = TextAlign.Center
            )
        }
        item {
            Spacer(modifier = Modifier.height(8.dp))
        }
        item {
            CompactChip(
                onClick = { viewModel.sendTestPing() },
                label = { Text("테스트 핑") }
            )
        }
        if (state.lastPingSuccess != null) {
            item {
                Text(
                    text = if (state.lastPingSuccess == true) "핑 성공!" else "핑 실패",
                    style = MaterialTheme.typography.caption3,
                    color = if (state.lastPingSuccess == true) Color(0xFF10B981) else MaterialTheme.colors.error,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}
