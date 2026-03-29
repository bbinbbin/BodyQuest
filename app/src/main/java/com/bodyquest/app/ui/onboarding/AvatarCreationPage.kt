package com.bodyquest.app.ui.onboarding

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bodyquest.app.R
import com.bodyquest.app.ui.theme.DarkSurfaceVariant
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextSecondary

// avatarIndex: 0 = 남성, 1 = 여성
private val avatarOptions = listOf(
    Pair("남성", R.drawable.avatar_male),
    Pair("여성", R.drawable.avatar_female)
)

@Composable
fun AvatarCreationPage(
    nickname: String,
    avatarIndex: Int,
    nicknameError: String? = null,
    onNicknameChange: (String) -> Unit,
    onAvatarSelect: (Int) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "캐릭터를 만들어보세요",
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = "닉네임과 아바타를 선택하세요",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(28.dp))

        OutlinedTextField(
            value = nickname,
            onValueChange = onNicknameChange,
            label = { Text("닉네임") },
            singleLine = true,
            isError = nicknameError != null,
            supportingText = if (nicknameError != null) {
                { Text(nicknameError, color = NeonRed) }
            } else null,
            modifier = Modifier.fillMaxWidth(),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonPurple,
                focusedLabelColor = NeonPurple,
                cursorColor = NeonPurple,
                errorBorderColor = NeonRed,
                errorLabelColor = NeonRed,
                errorCursorColor = NeonRed
            )
        )

        Spacer(modifier = Modifier.height(28.dp))

        Text(
            text = "아바타 선택",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onBackground
        )
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            avatarOptions.forEachIndexed { index, (label, drawableRes) ->
                val isSelected = avatarIndex == index
                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .clickable { onAvatarSelect(index) },
                    shape = RoundedCornerShape(20.dp),
                    color = if (isSelected) NeonPurple.copy(alpha = 0.15f) else DarkSurfaceVariant,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) NeonPurple else TextMuted.copy(alpha = 0.3f)
                    )
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Image(
                            painter = painterResource(drawableRes),
                            contentDescription = label,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(220.dp)
                                .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = label,
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSelected) NeonPurple
                            else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                    }
                }
            }
        }
    }
}
