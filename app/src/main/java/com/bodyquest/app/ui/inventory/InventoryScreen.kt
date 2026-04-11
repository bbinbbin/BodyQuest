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
import com.bodyquest.app.domain.model.SkinCategory
import com.bodyquest.app.domain.model.SkinItem
import com.bodyquest.app.ui.theme.DarkBackground
import com.bodyquest.app.ui.theme.DarkSurface
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonOrange
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextPrimary
import com.bodyquest.app.ui.theme.TextSecondary
import com.bodyquest.app.ui.theme.XpGold

private fun skinDrawableRes(skinId: String): Int? = when (skinId) {
    // 여성 개별 스킨
    "skin_f_white_tshirt"   -> R.drawable.skin_f_white_tshirt
    "skin_f_blue_bra"       -> R.drawable.skin_f_blue_bra
    "skin_f_yellow_pants"   -> R.drawable.skin_f_yellow_pants
    "skin_f_headband"       -> R.drawable.skin_f_headband
    // 여성 세트 스킨
    "skin_f_dinosaur_set"   -> R.drawable.skin_f_dinosaur_set
    "skin_f_bunny_set"      -> R.drawable.skin_f_bunny_set
    // 남성 개별 스킨
    "skin_m_black_tank"     -> R.drawable.skin_m_black_tank
    "skin_m_white_tshirt"   -> R.drawable.skin_m_white_tshirt
    "skin_m_yellow_pants"   -> R.drawable.skin_m_yellow_pants
    // 남성 세트 스킨
    "skin_m_dinosaur_set"   -> R.drawable.skin_m_dinosaur_set
    "skin_m_bunny_set"      -> R.drawable.skin_m_bunny_set
    "skin_m_suit_set"       -> R.drawable.skin_m_suit_set
    else -> null
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(viewModel: InventoryViewModel, onBack: () -> Unit) {
    val inventory by viewModel.inventory.collectAsState()
    val equippedTopId by viewModel.equippedTopId.collectAsState()
    val equippedBottomId by viewModel.equippedBottomId.collectAsState()
    val equippedHatId by viewModel.equippedHatId.collectAsState()
    val ticketCount by viewModel.ticketCount.collectAsState()

    var dialogSkin by remember { mutableStateOf<SkinItem?>(null) }
    var confirmDisassembleSkin by remember { mutableStateOf<SkinItem?>(null) }
    var disassembleResult by remember { mutableStateOf<Boolean?>(null) }

    // 1단계: 장착/해제/분해 선택 다이얼로그
    dialogSkin?.let { skin ->
        val isEquipped = viewModel.isEquipped(skin, equippedTopId, equippedBottomId, equippedHatId)
        AlertDialog(
            onDismissRequest = { dialogSkin = null },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(text = skin.name, fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Text(
                    text = when {
                        isEquipped -> "현재 장착 중인 스킨입니다."
                        skin.category == SkinCategory.SET -> "세트 스킨입니다.\n장착 시 현재 착용 중인 모든 스킨이 해제됩니다."
                        else -> "${skin.category.displayName} 스킨입니다."
                    },
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(onClick = {
                    if (isEquipped) viewModel.unequipSkin(skin) else viewModel.equipSkin(skin)
                    dialogSkin = null
                }) {
                    Text(
                        text = if (isEquipped) "해제하기" else "장착하기",
                        color = NeonPurple,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            dismissButton = {
                Row {
                    TextButton(onClick = {
                        confirmDisassembleSkin = skin
                        dialogSkin = null
                    }) {
                        Text(text = "분해하기", color = NeonOrange)
                    }
                    TextButton(onClick = { dialogSkin = null }) {
                        Text(text = "취소", color = TextMuted)
                    }
                }
            }
        )
    }

    // 2단계: 분해 확인 다이얼로그
    confirmDisassembleSkin?.let { skin ->
        AlertDialog(
            onDismissRequest = { confirmDisassembleSkin = null },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(text = "스킨 분해", fontWeight = FontWeight.Bold, color = TextPrimary)
            },
            text = {
                Column {
                    Text(
                        text = "'${skin.name}'을(를) 분해하시겠습니까?",
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "분해하면 스킨이 사라집니다.\n60% 확률로 🎫 뽑기 티켓 1장을 획득합니다.",
                        color = TextSecondary,
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = {
                    val target = confirmDisassembleSkin
                    confirmDisassembleSkin = null
                    if (target != null) {
                        viewModel.disassemble(target) { gotTicket ->
                            disassembleResult = gotTicket
                        }
                    }
                }) {
                    Text(text = "분해하기", color = NeonOrange, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDisassembleSkin = null }) {
                    Text(text = "취소", color = TextMuted)
                }
            }
        )
    }

    // 3단계: 분해 결과 다이얼로그
    disassembleResult?.let { gotTicket ->
        AlertDialog(
            onDismissRequest = { disassembleResult = null },
            containerColor = DarkSurfaceVariant,
            title = {
                Text(
                    text = if (gotTicket) "티켓 획득!" else "분해 완료",
                    fontWeight = FontWeight.Bold,
                    color = if (gotTicket) XpGold else TextPrimary
                )
            },
            text = {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = if (gotTicket) "🎫" else "💨",
                        fontSize = 40.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (gotTicket) "뽑기 티켓 1장을 획득했습니다!" else "아쉽게도 티켓을 얻지 못했습니다.",
                        color = if (gotTicket) NeonGreen else TextSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            },
            confirmButton = {
                TextButton(onClick = { disassembleResult = null }) {
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
            actions = {
                Surface(
                    shape = RoundedCornerShape(20.dp),
                    color = XpGold.copy(alpha = 0.15f),
                    modifier = Modifier.padding(end = 12.dp)
                ) {
                    Text(
                        text = "🎫 $ticketCount 장",
                        color = XpGold,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
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
                        isEquipped = viewModel.isEquipped(skin, equippedTopId, equippedBottomId, equippedHatId),
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
    isEquipped: Boolean,
    onClick: () -> Unit
) {
    Surface(
        shape = RoundedCornerShape(12.dp),
        color = DarkSurfaceVariant,
        border = BorderStroke(
            if (isEquipped) 2.dp else 1.dp,
            if (isEquipped) NeonPurple else skin.category.color.copy(alpha = 0.4f)
        ),
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
                    color = if (isEquipped) TextPrimary else TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (isEquipped) {
                    Surface(
                        shape = RoundedCornerShape(6.dp),
                        color = NeonPurple.copy(alpha = 0.2f)
                    ) {
                        Text(
                            text = "장착중",
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold,
                            color = NeonPurple,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
        }
    }
}
