package com.bodyquest.app.ui.login

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.domain.model.AuthResult
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class LoginMode { SIGN_IN, SIGN_UP }

data class LoginState(
    val mode: LoginMode = LoginMode.SIGN_IN,
    val email: String = "",
    val password: String = "",
    val confirmPassword: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val authSuccess: AuthSuccessResult? = null,
    val passwordResetSent: Boolean = false,
    val signUpCompleted: Boolean = false
)

data class AuthSuccessResult(
    val isNewUser: Boolean
)

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _state = MutableStateFlow(LoginState())
    val state: StateFlow<LoginState> = _state

    fun setEmail(email: String) {
        _state.value = _state.value.copy(email = email, error = null)
    }

    fun setPassword(password: String) {
        _state.value = _state.value.copy(password = password, error = null)
    }

    fun setConfirmPassword(confirmPassword: String) {
        _state.value = _state.value.copy(confirmPassword = confirmPassword, error = null)
    }

    fun toggleMode() {
        val current = _state.value
        _state.value = current.copy(
            mode = if (current.mode == LoginMode.SIGN_IN) LoginMode.SIGN_UP else LoginMode.SIGN_IN,
            email = "",
            password = "",
            confirmPassword = "",
            error = null,
            passwordResetSent = false,
            signUpCompleted = false
        )
    }

    fun signInWithEmail() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "이메일과 비밀번호를 입력해주세요")
            return
        }

        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = authRepository.signInWithEmail(s.email.trim(), s.password)) {
                is AuthResult.Success -> handleAuthSuccess(result)
                is AuthResult.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun signUpWithEmail() {
        val s = _state.value
        if (s.email.isBlank() || s.password.isBlank()) {
            _state.value = s.copy(error = "이메일과 비밀번호를 입력해주세요")
            return
        }
        if (s.password.length < 6) {
            _state.value = s.copy(error = "비밀번호는 6자 이상이어야 합니다")
            return
        }
        if (s.password != s.confirmPassword) {
            _state.value = s.copy(error = "비밀번호가 일치하지 않습니다")
            return
        }

        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = authRepository.signUpWithEmail(s.email.trim(), s.password)) {
                is AuthResult.Success -> {
                    authRepository.signOut()
                    _state.value = _state.value.copy(
                        isLoading = false,
                        mode = LoginMode.SIGN_IN,
                        email = "",
                        password = "",
                        confirmPassword = "",
                        signUpCompleted = true
                    )
                }
                is AuthResult.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun signInWithGoogle(idToken: String) {
        _state.value = _state.value.copy(isLoading = true, error = null)
        viewModelScope.launch {
            when (val result = authRepository.signInWithGoogle(idToken)) {
                is AuthResult.Success -> handleAuthSuccess(result)
                is AuthResult.Error -> _state.value = _state.value.copy(
                    isLoading = false,
                    error = result.message
                )
            }
        }
    }

    fun sendPasswordReset() {
        val s = _state.value
        if (s.email.isBlank()) {
            _state.value = s.copy(error = "비밀번호를 재설정할 이메일을 입력해주세요")
            return
        }

        _state.value = s.copy(isLoading = true, error = null)
        viewModelScope.launch {
            authRepository.sendPasswordResetEmail(s.email.trim())
                .onSuccess {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        passwordResetSent = true
                    )
                }
                .onFailure {
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = "비밀번호 재설정 메일 전송에 실패했습니다"
                    )
                }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }

    fun signInWithGoogleError(message: String) {
        _state.value = _state.value.copy(isLoading = false, error = message)
    }

    private suspend fun handleAuthSuccess(result: AuthResult.Success) {
        sharedPreferences.edit().putBoolean("has_logged_in", true).apply()
        syncManager.syncOnLogin(result.uid)
        val existingUser = userRepository.getUserByFirebaseUid(result.uid)
        val isNewUser = existingUser == null
        _state.value = _state.value.copy(
            isLoading = false,
            authSuccess = AuthSuccessResult(isNewUser = isNewUser)
        )
    }
}
