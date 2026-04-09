package com.bodyquest.app.ui.login

import android.app.Activity
import com.bodyquest.app.util.AppLogger
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.scale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.bodyquest.app.R
import com.bodyquest.app.ui.theme.DarkBackground
import com.bodyquest.app.ui.theme.DarkBorder
import com.bodyquest.app.ui.theme.DarkSurface
import com.bodyquest.app.ui.theme.NeonGreen
import com.bodyquest.app.ui.theme.NeonPurple
import com.bodyquest.app.ui.theme.NeonRed
import com.bodyquest.app.ui.theme.TextMuted
import com.bodyquest.app.ui.theme.TextPrimary
import com.bodyquest.app.ui.theme.TextSecondary
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch

@Composable
fun LoginScreen(
    viewModel: LoginViewModel,
    onLoginSuccess: (isNewUser: Boolean) -> Unit
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
    val scope = rememberCoroutineScope()

    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    val emailFocusRequester = remember { androidx.compose.ui.focus.FocusRequester() }

    LaunchedEffect(state.mode) {
        emailFocusRequester.requestFocus()
    }

    LaunchedEffect(state.authSuccess) {
        state.authSuccess?.let { result ->
            onLoginSuccess(result.isNewUser)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(48.dp))

            // Logo
            Text(
                text = "\uD83D\uDEE1\uFE0F\uD83D\uDCAA",
                fontSize = 48.sp,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "BodyQuest",
                style = MaterialTheme.typography.displaySmall,
                color = NeonPurple,
                textAlign = TextAlign.Center
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (state.mode == LoginMode.SIGN_IN) "다시 오신 것을 환영합니다"
                       else "모험을 시작하세요",
                style = MaterialTheme.typography.bodyLarge,
                color = TextSecondary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(40.dp))

            // Email field
            OutlinedTextField(
                value = state.email,
                onValueChange = viewModel::setEmail,
                label = { Text("이메일") },
                leadingIcon = {
                    Icon(Icons.Default.Email, contentDescription = null, tint = TextMuted)
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                ),
                colors = loginTextFieldColors(),
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(emailFocusRequester)
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Password field
            OutlinedTextField(
                value = state.password,
                onValueChange = viewModel::setPassword,
                label = { Text("비밀번호") },
                leadingIcon = {
                    Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
                },
                trailingIcon = {
                    IconButton(onClick = { passwordVisible = !passwordVisible }) {
                        Icon(
                            if (passwordVisible) Icons.Default.VisibilityOff
                            else Icons.Default.Visibility,
                            contentDescription = if (passwordVisible) "비밀번호 숨기기" else "비밀번호 보기",
                            tint = TextMuted
                        )
                    }
                },
                singleLine = true,
                visualTransformation = if (passwordVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = if (state.mode == LoginMode.SIGN_UP) ImeAction.Next
                                else ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) },
                    onDone = {
                        focusManager.clearFocus()
                        if (state.mode == LoginMode.SIGN_IN) viewModel.signInWithEmail()
                    }
                ),
                colors = loginTextFieldColors(),
                modifier = Modifier.fillMaxWidth()
            )

            // Confirm password (sign-up only)
            if (state.mode == LoginMode.SIGN_UP) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = state.confirmPassword,
                    onValueChange = viewModel::setConfirmPassword,
                    label = { Text("비밀번호 확인") },
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = TextMuted)
                    },
                    trailingIcon = {
                        IconButton(onClick = { confirmPasswordVisible = !confirmPasswordVisible }) {
                            Icon(
                                if (confirmPasswordVisible) Icons.Default.VisibilityOff
                                else Icons.Default.Visibility,
                                contentDescription = null,
                                tint = TextMuted
                            )
                        }
                    },
                    singleLine = true,
                    visualTransformation = if (confirmPasswordVisible) VisualTransformation.None
                                           else PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            viewModel.signUpWithEmail()
                        }
                    ),
                    colors = loginTextFieldColors(),
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 아이디 저장 + 비밀번호 찾기 (로그인 모드에서만)
            if (state.mode == LoginMode.SIGN_IN) {
                Spacer(modifier = Modifier.height(4.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.clickable { viewModel.setSaveEmail(!state.saveEmail) }
                    ) {
                        Box(modifier = Modifier.size(16.dp).scale(0.7f)) {
                            Checkbox(
                                checked = state.saveEmail,
                                onCheckedChange = null,
                                colors = CheckboxDefaults.colors(
                                    checkedColor = NeonPurple,
                                    uncheckedColor = TextMuted,
                                    checkmarkColor = TextPrimary
                                )
                            )
                        }
                        Spacer(modifier = Modifier.size(6.dp))
                        Text(
                            text = "아이디 저장",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                    Text(
                        text = "비밀번호 찾기",
                        style = MaterialTheme.typography.bodySmall,
                        color = NeonPurple,
                        modifier = Modifier.clickable { viewModel.sendPasswordReset() }
                    )
                }
            }

            // Password reset sent message
            if (state.passwordResetSent) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "비밀번호 재설정 메일을 보냈습니다. 이메일을 확인해주세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Sign-up completed message
            if (state.signUpCompleted) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "회원가입이 완료되었습니다! 로그인해주세요.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonGreen,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Error message
            if (state.error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = state.error!!,
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonRed,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Primary button
            Button(
                onClick = {
                    if (state.mode == LoginMode.SIGN_IN) viewModel.signInWithEmail()
                    else viewModel.signUpWithEmail()
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NeonPurple)
            ) {
                if (state.isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = TextPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Text(
                        text = if (state.mode == LoginMode.SIGN_IN) "로그인" else "회원가입",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Toggle sign-in / sign-up
            Row(
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = if (state.mode == LoginMode.SIGN_IN) "계정이 없으신가요? "
                           else "이미 계정이 있으신가요? ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted
                )
                Text(
                    text = if (state.mode == LoginMode.SIGN_IN) "회원가입" else "로그인",
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonPurple,
                    modifier = Modifier.clickable { viewModel.toggleMode() }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Divider
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DarkBorder
                )
                Text(
                    text = "  또는  ",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted
                )
                HorizontalDivider(
                    modifier = Modifier.weight(1f),
                    color = DarkBorder
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Google Sign-In button
            OutlinedButton(
                onClick = {
                    scope.launch {
                        launchGoogleSignIn(context, viewModel)
                    }
                },
                enabled = !state.isLoading,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    containerColor = DarkSurface
                )
            ) {
                Text(
                    text = "Google로 계속하기",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimary
                )
            }

            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

private fun findActivity(context: android.content.Context): Activity? {
    var ctx = context
    while (ctx is android.content.ContextWrapper) {
        if (ctx is Activity) return ctx
        ctx = ctx.baseContext
    }
    return null
}

private suspend fun launchGoogleSignIn(context: android.content.Context, viewModel: LoginViewModel) {
    val activity = findActivity(context)
    if (activity == null) {
        AppLogger.e("LoginScreen", "Activity not found from context")
        return
    }
    try {
        val credentialManager = CredentialManager.create(activity)
        val googleIdOption = GetGoogleIdOption.Builder()
            .setFilterByAuthorizedAccounts(false)
            .setServerClientId(activity.getString(R.string.default_web_client_id))
            .build()
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()

        val result = credentialManager.getCredential(activity, request)
        val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(result.credential.data)
        viewModel.signInWithGoogle(googleIdTokenCredential.idToken)
    } catch (_: GetCredentialCancellationException) {
        // User cancelled
    } catch (e: Exception) {
        AppLogger.e("LoginScreen", "Google Sign-In failed: ${e.javaClass.simpleName}")
        viewModel.signInWithGoogleError(e.message ?: "Google 로그인에 실패했습니다.")
    }
}

@Composable
private fun loginTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = TextPrimary,
    unfocusedTextColor = TextPrimary,
    focusedBorderColor = NeonPurple,
    unfocusedBorderColor = DarkBorder,
    focusedLabelColor = NeonPurple,
    unfocusedLabelColor = TextMuted,
    cursorColor = NeonPurple
)
