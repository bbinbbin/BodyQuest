package com.bodyquest.app.ui.test

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun TestScreen(onBack: () -> Unit = {}) {
    val context = LocalContext.current
    var model by remember { mutableStateOf<ObjModel?>(null) }
    var errorMsg by remember { mutableStateOf<String?>(null) }
    var loadingFileName by remember { mutableStateOf("") }

    LaunchedEffect(Unit) {
        withContext(Dispatchers.IO) {
            val glbFile = context.assets.list("")?.firstOrNull { it.endsWith(".glb") }

            if (glbFile != null) {
                loadingFileName = glbFile
                val glbResult = runCatching { GlbParser.parse(context, glbFile) }
                val glbModel = glbResult.getOrNull()
                if (glbModel != null) {
                    model = glbModel
                    return@withContext
                }
            }

            // GLB 없거나 파싱 실패 시 OBJ fallback
            loadingFileName = "testbear.obj"
            val result = runCatching { ObjParser.parse(context, "testbear.obj") }
            result.fold(
                onSuccess = { m ->
                    if (m != null) model = m
                    else errorMsg = "파싱 실패\nassets/${loadingFileName} 파일을 확인하세요."
                },
                onFailure = { e ->
                    errorMsg = "로딩 오류: ${e.message}"
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        when {
            errorMsg != null -> Text(
                text = errorMsg!!,
                style = MaterialTheme.typography.bodyMedium,
                color = NeonRed,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )

            model == null -> Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = NeonPurple)
                Spacer(modifier = Modifier.height(14.dp))
                Text(
                    text = "${if (loadingFileName.isNotEmpty()) "$loadingFileName\n" else ""}모델 로딩 중...\n(대용량 파일은 수 초 소요될 수 있습니다)",
                    style = MaterialTheme.typography.labelMedium,
                    color = TextMuted,
                    textAlign = TextAlign.Center
                )
            }

            else -> {
                val loadedModel = model!!
                val renderer = remember(loadedModel) { ModelRenderer(loadedModel) }

                AndroidView(
                    factory = { ctx -> ModelGLSurfaceView(ctx, renderer) },
                    modifier = Modifier.fillMaxSize()
                )

                Column(
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "← → 좌우  /  ↑ ↓ 상하 드래그하여 회전",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "${"%,d".format(loadedModel.vertexCount / 3)}개 폴리곤",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextMuted.copy(alpha = 0.45f)
                    )
                }
            }
        }

        IconButton(
            onClick = onBack,
            modifier = Modifier
                .align(Alignment.TopStart)
                .statusBarsPadding()
                .padding(4.dp)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "뒤로가기",
                tint = TextMuted
            )
        }
    }
}
