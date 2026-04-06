package com.bodyquest.app.ui.gacha

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.runtime.collectAsState
import com.bodyquest.app.R
import com.bodyquest.app.domain.model.ALL_SKINS
import com.bodyquest.app.domain.model.SkinItem
import com.bodyquest.app.ui.theme.DarkBackground
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonPink
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextPrimary
import com.bodyquest.app.ui.theme.TextSecondary
import com.bodyquest.app.ui.theme.XpGold
import kotlinx.coroutines.delay

/** 스킨 ID → drawable 리소스 매핑 (뽑기 결과 카드 미리보기용) */
private fun skinDrawableRes(skinId: String): Int? = when (skinId) {
    "skin_f_white_tshirt"  -> R.drawable.skin_f_white_tshirt
    "skin_f_blue_bra"      -> R.drawable.skin_f_blue_bra
    "skin_f_yellow_pants"  -> R.drawable.skin_f_yellow_pants
    else -> null
}

private enum class GachaPhase { IDLE, SPINNING, REVEALED }

@Composable
fun GachaScreen(viewModel: GachaViewModel, onBack: () -> Unit) {
    val ticketCount by viewModel.ticketCount.collectAsState()
    val avatarIndex by viewModel.avatarIndex.collectAsState()
    var phase by remember { mutableStateOf(GachaPhase.IDLE) }
    var showFlash by remember { mutableStateOf(false) }
    var revealVisible by remember { mutableStateOf(false) }
    var drawnSkin by remember { mutableStateOf<SkinItem?>(null) }
    // phase가 아닌 별도 트리거로 키를 설정 — phase 변경 시 코루틴이 취소되지 않음
    var animTrigger by remember { mutableStateOf(0) }

    // avatarIndex에 맞는 스킨 풀 (null = 공통, 해당 index = 전용)
    val skinPool = remember(avatarIndex) {
        ALL_SKINS.filter { it.avatarFilter == null || it.avatarFilter == avatarIndex }
    }

    LaunchedEffect(animTrigger) {
        if (animTrigger > 0) {
            delay(2500)
            showFlash = true
            delay(250)
            phase = GachaPhase.REVEALED
            // 결과 확정 시 인벤토리에 저장
            drawnSkin?.let { viewModel.onGachaResolved(it.id) }
            delay(100)
            showFlash = false
            delay(100)
            revealVisible = true
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // 타이틀 — IDLE 때만 표시
            AnimatedVisibility(
                visible = phase == GachaPhase.IDLE,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "스킨 뽑기",
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        text = "어떤 스킨이 나올까요?",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                    Spacer(Modifier.height(12.dp))
                    Text(
                        text = "🎫 보유 티켓: ${ticketCount}장",
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (ticketCount > 0) XpGold else TextMuted
                    )
                    Spacer(Modifier.height(28.dp))
                }
            }

            // 카드 영역
            when (phase) {
                GachaPhase.IDLE -> IdleCard()
                GachaPhase.SPINNING -> SpinningCard()
                GachaPhase.REVEALED -> RevealedCard(visible = revealVisible, skin = drawnSkin)
            }

            Spacer(Modifier.height(40.dp))

            // 하단 영역
            when (phase) {
                GachaPhase.IDLE -> {
                    Button(
                        onClick = {
                            if (skinPool.isNotEmpty() && viewModel.consumeTicket()) {
                                drawnSkin = skinPool.random()
                                phase = GachaPhase.SPINNING
                                animTrigger++
                            }
                        },
                        enabled = ticketCount > 0 && skinPool.isNotEmpty(),
                        modifier = Modifier
                            .fillMaxWidth(0.65f)
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonPurple,
                            disabledContainerColor = NeonPurple.copy(alpha = 0.3f)
                        ),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Text(
                            text = when {
                                skinPool.isEmpty() -> "뽑을 수 있는 스킨이 없습니다"
                                ticketCount > 0 -> "✨ 뽑기 (1장 사용)"
                                else -> "티켓이 없습니다"
                            },
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                GachaPhase.SPINNING -> {
                    Text(
                        text = "뽑는 중...",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextSecondary
                    )
                }
                GachaPhase.REVEALED -> {
                    AnimatedVisibility(
                        visible = revealVisible,
                        enter = fadeIn(tween(400)) + scaleIn(initialScale = 0.8f, animationSpec = tween(400))
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${drawnSkin?.name ?: "스킨"}을 뽑았다!",
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold,
                                color = XpGold
                            )
                            Spacer(Modifier.height(24.dp))
                            Button(
                                onClick = onBack,
                                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                                shape = RoundedCornerShape(12.dp),
                                modifier = Modifier.fillMaxWidth(0.5f)
                            ) {
                                Text("확인", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }
        }

        // 뽑기 순간 화면 플래시
        AnimatedVisibility(
            visible = showFlash,
            enter = fadeIn(tween(80)),
            exit = fadeOut(tween(400))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.White.copy(alpha = 0.9f))
            )
        }
    }
}

// 대기 카드: 정적 "?" 카드
@Composable
private fun IdleCard() {
    Surface(
        modifier = Modifier.size(180.dp, 240.dp),
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(2.dp, NeonPurple.copy(alpha = 0.4f))
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "?",
                    fontSize = 72.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple.copy(alpha = 0.6f)
                )
                Spacer(Modifier.height(8.dp))
                Text(
                    text = "신비한 스킨",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextMuted
                )
            }
        }
    }
}

// 뽑기 애니메이션 카드: 글로우 링 + 펄싱 "?" 카드
@Composable
private fun SpinningCard() {
    val infiniteTransition = rememberInfiniteTransition(label = "gacha_spin")

    val cardScale by infiniteTransition.animateFloat(
        initialValue = 0.92f,
        targetValue = 1.07f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cardScale"
    )
    val glowAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "glowAlpha"
    )
    val arcRotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(1600, easing = LinearEasing)
        ),
        label = "arcRotation"
    )

    Box(modifier = Modifier.size(260.dp), contentAlignment = Alignment.Center) {
        // 회전하는 글로우 아크 (카드 주변)
        Spacer(
            modifier = Modifier
                .size(252.dp)
                .rotate(arcRotation)
                .drawBehind {
                    val strokeWidth = 7.dp.toPx()
                    val style = Stroke(strokeWidth)
                    drawArc(
                        color = NeonPurple.copy(alpha = glowAlpha),
                        startAngle = -60f,
                        sweepAngle = 200f,
                        useCenter = false,
                        style = style
                    )
                    drawArc(
                        color = NeonPink.copy(alpha = glowAlpha * 0.55f),
                        startAngle = 160f,
                        sweepAngle = 90f,
                        useCenter = false,
                        style = style
                    )
                }
        )

        // 펄싱 "?" 카드
        Surface(
            modifier = Modifier
                .size(180.dp, 240.dp)
                .scale(cardScale),
            shape = RoundedCornerShape(16.dp),
            color = DarkSurfaceVariant,
            border = BorderStroke(2.dp, NeonPurple.copy(alpha = glowAlpha))
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "?",
                    fontSize = 82.sp,
                    fontWeight = FontWeight.Bold,
                    color = NeonPurple.copy(alpha = glowAlpha)
                )
            }
        }
    }
}

// 뽑기 결과 카드: 이미지 스킨이면 이미지, 아니면 이모지+텍스트
@Composable
private fun RevealedCard(visible: Boolean, skin: SkinItem?) {
    AnimatedVisibility(
        visible = visible,
        enter = scaleIn(
            initialScale = 0.45f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessMedium
            )
        ) + fadeIn(tween(200))
    ) {
        if (skin != null) {
            val drawableRes = skinDrawableRes(skin.id)
            Surface(
                modifier = Modifier.size(180.dp, 240.dp),
                shape = RoundedCornerShape(16.dp),
                color = DarkSurfaceVariant,
                border = BorderStroke(2.dp, skin.category.color)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    if (drawableRes != null) {
                        // 이미지 스킨
                        Image(
                            painter = painterResource(drawableRes),
                            contentDescription = skin.name,
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.fillMaxSize()
                        )
                        // 이름 + 카테고리 배지 (하단 오버레이)
                        Column(
                            modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 10.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = skin.name,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(4.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = skin.category.color.copy(alpha = 0.85f)
                            ) {
                                Text(
                                    text = skin.category.displayName,
                                    fontSize = 11.sp,
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                                )
                            }
                        }
                    } else {
                        // 텍스트/이모지 스킨
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(text = skin.category.emoji, fontSize = 56.sp)
                            Spacer(Modifier.height(12.dp))
                            Text(
                                text = skin.name,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(Modifier.height(6.dp))
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = skin.category.color.copy(alpha = 0.2f)
                            ) {
                                Text(
                                    text = skin.category.displayName,
                                    fontSize = 12.sp,
                                    color = skin.category.color,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
