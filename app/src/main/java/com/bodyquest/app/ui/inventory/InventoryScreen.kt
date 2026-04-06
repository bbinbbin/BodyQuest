package com.bodyquest.app.ui.inventory

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bodyquest.app.R
import com.bodyquest.app.domain.model.SkinItem
import com.bodyquest.app.ui.theme.DarkBackground
import com.bodyquest.app.ui.theme.DarkSurface
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextPrimary
import com.bodyquest.app.ui.theme.TextSecondary

private fun skinDrawableRes(skinId: String): Int? = when (skinId) {
    "skin_black_t" -> R.drawable.black_t
    "skin_a" -> R.drawable.skin_a
    "skin_hood_t" -> R.drawable.hood_t
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel, onBack: () -> Unit) {
    val inventory by viewModel.inventory.collectAsState()

    var dialogSkin by remember { mutableStateOf<SkinItem?>(null) }
    var showComingSoon by remember { mutableStateOf(false) }

    // 장착 다이얼로그
    dialogSkin?.let { skin ->
        AlertDialog(
            onDismissRequest = { dialogSkin = null },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(text = skin.name, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    text = "${skin.category.emoji} ${skin.category.displayName} 스킨입니다.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    dialogSkin = null
                    showComingSoon = true
                }) {
                    Text(
                        text = "장착하기",
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogSkin = null }) {
                    Text(text = "취소", color = TextMuted)
                }
            }
        )
    }

    // 구현 중 다이얼로그
    if (showComingSoon) {
        AlertDialog(
            onDismissRequest = { showComingSoon = false },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(text = "알림", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    text = "구현 중입니다. 곧 찾아뵙겠습니다.",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = { showComingSoon = false }) {
                    Text(text = "확인", color = NeonPurple, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        TopAppBar(
            title = {
                Text(text = "인벤토리", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            navigationIcon = {
                IconButton(onClick = onBack) {
                    Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "뒤로",
                        tint = TextPrimary
                    )
                }
            },
            colors = TopAppBarDefaults.topAppBarColors(containerColor = DarkSurface)
        )

        if (inventory.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(text = "🎁", fontSize = 48.sp)
                    Text(
                        text = "아직 획득한 스킨이 없습니다.",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 12.dp)
                    )
                    Text(
                        text = "스킨 뽑기로 스킨을 획득해보세요.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }
        } else {
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(inventory) { (skin, count) ->
                    SkinCard(
                        skin = skin,
                        count = count,
                        onClick = { dialogSkin = skin }
                    )
                }
            }
        }
    }
}

@Composable
private fun SkinCard(
    skin: SkinItem,
    count: Int,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(1.dp, skin.category.color.copy(alpha = 0.4f)),
        tonalElevation = 2.dp,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Column {
            // 이미지 또는 이모지 영역
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp)
                    .background(
                        skin.category.color.copy(alpha = 0.1f),
                        RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                val drawableRes = skinDrawableRes(skin.id)
                if (drawableRes != null) {
                    Image(
                        painter = painterResource(drawableRes),
                        contentDescription = skin.name,
                        contentScale = ContentScale.Fit,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(text = skin.category.emoji, fontSize = 36.sp)
                        Spacer(modifier = Modifier.height(4.dp))
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = skin.category.color.copy(alpha = 0.2f)
                        ) {
                            Text(
                                text = skin.category.displayName,
                                fontSize = 11.sp,
                                color = skin.category.color,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                            )
                        }
                    }
                }

                // 개수 배지
                if (count >= 2) {
                    Surface(
                        shape = CircleShape,
                        color = NeonPurple,
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(8.dp)
                            .size(26.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = "×$count",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                        }
                    }
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 10.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = skin.name,
                    style = MaterialTheme.typography.labelMedium,
                    color = TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}
