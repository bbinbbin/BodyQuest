package com.bodyquest.app.ui.avatar

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bodyquest.app.R
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.ui.common.ErrorScreen
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.common.UiState
import com.bodyquest.app.ui.home.HomeViewModel
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary
import kotlinx.coroutines.launch

@Composable
fun AvatarScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (val current = uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = current.message, onRetry = { viewModel.retry() })
        is UiState.Success -> {
            val user = current.data.user ?: return
            val job = try { Job.valueOf(user.job) } catch (_: Exception) { Job.STRENGTH }
            val avatarRes = if (user.avatarIndex == 0) R.drawable.avatar_male else R.drawable.avatar_female
            val goalName = when (user.goal) {
                "DIET" -> "다이어트"
                "BULK_UP" -> "벌크업"
                "MAINTAIN" -> "유지"
                else -> user.goal
            }

            val rotationY = remember { Animatable(0f) }
            val scope = rememberCoroutineScope()

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 아바타 이미지 영역 — 드래그로 좌우 회전
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(DarkSurfaceVariant)
                        .pointerInput(Unit) {
                            detectDragGestures(
                                onDragEnd = {
                                    // 손 떼면 스프링으로 0도로 복귀
                                    scope.launch {
                                        rotationY.animateTo(
                                            targetValue = 0f,
                                            animationSpec = spring(dampingRatio = 0.6f, stiffness = 200f)
                                        )
                                    }
                                }
                            ) { change, dragAmount ->
                                change.consume()
                                scope.launch {
                                    // 드래그 민감도: 0.25f (px → 회전각도)
                                    val newRotation = (rotationY.value + dragAmount.x * 0.25f)
                                        .coerceIn(-75f, 75f)
                                    rotationY.snapTo(newRotation)
                                }
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(avatarRes),
                        contentDescription = user.nickname,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer {
                                this.rotationY = rotationY.value
                                cameraDistance = 10f * density
                            }
                    )

                    // 드래그 힌트
                    Text(
                        text = "← 좌우로 드래그 →",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted,
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 12.dp)
                    )
                }

                // 유저 정보 카드
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
                    color = DarkSurfaceVariant
                ) {
                    Column(
                        modifier = Modifier.padding(28.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = user.nickname,
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = job.color.copy(alpha = 0.15f)
                        ) {
                            Text(
                                text = "${job.displayName}  ·  $goalName",
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
                                style = MaterialTheme.typography.labelMedium,
                                color = job.color
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Lv.${user.level}",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextSecondary
                        )
                    }
                }
            }
        }
    }
}
