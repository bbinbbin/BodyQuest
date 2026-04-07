package com.bodyquest.app.ui.avatar

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.R
import com.bodyquest.app.domain.model.Job
import com.bodyquest.app.ui.common.ErrorScreen
import com.bodyquest.app.ui.common.LoadingScreen
import com.bodyquest.app.ui.common.UiState
import com.bodyquest.app.ui.home.HomeViewModel
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.TextSecondary

/**
 * TOP + BOTTOM + HAT 조합에 따라 미리 렌더링된 결과 이미지를 반환.
 * null → 기본 아바타 표시.
 */
private fun femaleAvatarRes(topId: String?, bottomId: String?, hatId: String?): Int {
    val hasHat = hatId == "skin_f_headband"
    val hasPants = bottomId == "skin_f_yellow_pants"
    return when {
        // 헤어밴드 + TOP + BOTTOM
        hasHat && topId == "skin_f_white_tshirt" && hasPants -> R.drawable.result_f_headband_white_tshirt_yellow_pants
        hasHat && topId == "skin_f_blue_bra"     && hasPants -> R.drawable.result_f_headband_blue_bra_yellow_pants
        // 헤어밴드 + TOP (바지 없음)
        hasHat && topId == "skin_f_blue_bra"     -> R.drawable.result_f_headband_blue_bra
        hasHat && topId == "skin_f_white_tshirt" -> R.drawable.result_f_headband_white_tshirt
        // 헤어밴드 + BOTTOM (상의 없음)
        hasHat && hasPants -> R.drawable.result_f_headband_yellow_pants
        // 헤어밴드 단독
        hasHat -> R.drawable.result_f_headband
        // TOP + BOTTOM (헤어밴드 없음)
        topId == "skin_f_white_tshirt" && hasPants -> R.drawable.result_f_white_tshirt_yellow_pants
        topId == "skin_f_blue_bra"     && hasPants -> R.drawable.result_f_blue_bra_yellow_pants
        topId == "skin_f_white_tshirt" -> R.drawable.result_f_white_tshirt
        topId == "skin_f_blue_bra"     -> R.drawable.result_f_blue_bra
        hasPants -> R.drawable.result_f_yellow_pants
        else -> R.drawable.avatar_female
    }
}

@Composable
fun AvatarScreen(
    viewModel: HomeViewModel,
    onNavigateToGacha: () -> Unit = {},
    onNavigateToInventory: () -> Unit = {}
) {
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
                    val avatarRes = if (user.avatarIndex == 0) {
                        R.drawable.avatar_male
                    } else {
                        femaleAvatarRes(user.equippedSkinId, user.equippedBottomId, user.equippedHatId)
                    }
                    Image(
                        painter = painterResource(avatarRes),
                        contentDescription = null,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                }

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
                        Spacer(modifier = Modifier.height(20.dp))
                        Button(
                            onClick = onNavigateToGacha,
                            colors = ButtonDefaults.buttonColors(containerColor = NeonPurple),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "✨ 스킨 뽑기", fontWeight = FontWeight.Bold)
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = onNavigateToInventory,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text(text = "인벤토리", fontWeight = FontWeight.Bold, color = NeonPurple)
                        }
                    }
                }
            }
        }
    }
}
