package com.bodyquest.app.ui.home.components

import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.bodyquest.app.R
import com.bodyquest.app.ui.theme.DarkSurface
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.TextPrimary

@Composable
fun ProfileImage(
    profileImageUrl: String?,
    avatarIndex: Int,
    size: Dp = 72.dp,
    isUploading: Boolean = false,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier.clickable(
            indication = null,
            interactionSource = remember { androidx.compose.foundation.interaction.MutableInteractionSource() }
        ) { onClick() }
    ) {
        val bitmap = remember(profileImageUrl) {
            profileImageUrl?.let {
                try {
                    val bytes = Base64.decode(it, Base64.NO_WRAP)
                    BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                } catch (_: Exception) {
                    null
                }
            }
        }

        if (bitmap != null) {
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "프로필 사진",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, NeonPurple, CircleShape)
            )
        } else {
            val avatarRes = if (avatarIndex == 0) R.drawable.avatar_male else R.drawable.avatar_female
            Image(
                painter = painterResource(id = avatarRes),
                contentDescription = "프로필 사진",
                contentScale = ContentScale.Crop,
                modifier = Modifier
                    .size(size)
                    .clip(CircleShape)
                    .border(2.dp, NeonPurple, CircleShape)
            )
        }

        if (isUploading) {
            CircularProgressIndicator(
                modifier = Modifier.size(size),
                color = NeonPurple,
                strokeWidth = 3.dp
            )
        }

        // Camera icon
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .offset(x = 2.dp, y = 2.dp)
                .size(24.dp)
                .clip(CircleShape)
                .background(DarkSurface)
                .border(1.dp, NeonPurple, CircleShape)
        ) {
            Icon(
                imageVector = Icons.Default.CameraAlt,
                contentDescription = "사진 변경",
                tint = TextPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}
