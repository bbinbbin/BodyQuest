package com.bodyquest.app.ui.avatar

import android.graphics.BitmapFactory
import androidx.compose.foundation.Canvas
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
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import kotlin.math.roundToInt

// 스프라이트 시트: 6열 × 4행, 15도 간격 24프레임
private const val SPRITE_COLS = 6
private const val SPRITE_ROWS = 4
private const val FRAME_COUNT = SPRITE_COLS * SPRITE_ROWS  // 24
private const val DEGREES_PER_FRAME = 360f / FRAME_COUNT   // 15도
private const val DRAG_SENSITIVITY = 0.5f  // px당 회전 각도

@Composable
fun AvatarScreen(viewModel: HomeViewModel) {
    val uiState by viewModel.uiState.collectAsState()

    when (val current = uiState) {
        is UiState.Loading -> LoadingScreen()
        is UiState.Error -> ErrorScreen(message = current.message, onRetry = { viewModel.retry() })
        is UiState.Success -> {
            val user = current.data.user ?: return
            val job = try { Job.valueOf(user.job) } catch (_: Exception) { Job.STRENGTH }
            val goalName = when (user.goal) {
                "DIET" -> "다이어트"
                "BULK_UP" -> "벌크업"
                "MAINTAIN" -> "유지"
                else -> user.goal
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .background(DarkSurfaceVariant)
                ) {
                    if (user.avatarIndex == 0) {
                        MaleAvatarView()
                    } else {
                        FemaleAvatarView()
                    }

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

// 남성 아바타 — 스프라이트 시트 기반 360도 회전
@Composable
private fun MaleAvatarView() {
    val context = LocalContext.current

    // 스프라이트 시트 비트맵 (한 번만 로드)
    val bitmap = remember {
        BitmapFactory.decodeResource(context.resources, R.drawable.avatar_male_360)
    }
    val frameW = bitmap.width / SPRITE_COLS
    val frameH = bitmap.height / SPRITE_ROWS

    var totalDragX by remember { mutableFloatStateOf(0f) }

    // 누적 드래그 → 프레임 인덱스 (0~23)
    val frameIndex = remember(totalDragX) {
        val degrees = totalDragX * DRAG_SENSITIVITY
        val raw = (degrees / DEGREES_PER_FRAME).roundToInt()
        ((raw % FRAME_COUNT) + FRAME_COUNT) % FRAME_COUNT
    }

    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                detectDragGestures { change, dragAmount ->
                    change.consume()
                    totalDragX += dragAmount.x
                }
            }
    ) {
        val row = frameIndex / SPRITE_COLS
        val col = frameIndex % SPRITE_COLS

        val srcLeft = col * frameW
        val srcTop = row * frameH
        val srcRect = android.graphics.Rect(srcLeft, srcTop, srcLeft + frameW, srcTop + frameH)
        val dstRect = android.graphics.RectF(0f, 0f, size.width, size.height)

        drawIntoCanvas { canvas ->
            canvas.nativeCanvas.drawBitmap(bitmap, srcRect, dstRect, null)
        }
    }
}

// 여성 아바타 — 단일 이미지 (기존 rotationY 방식 유지)
@Composable
private fun FemaleAvatarView() {
    Image(
        painter = painterResource(R.drawable.avatar_female),
        contentDescription = null,
        contentScale = ContentScale.Fit,
        modifier = Modifier.fillMaxSize()
    )
}
