package com.bodyquest.app.ui.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed interface SplashDestination {
    data object None : SplashDestination
    data object Intro : SplashDestination
    data object Login : SplashDestination
    data object Onboarding : SplashDestination
    data object Home : SplashDestination
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager,
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination: StateFlow<SplashDestination> = _destination

    companion object {
        private const val SESSION_TIMEOUT_MS = 15 * 60 * 1000L // 15분
    }

    fun checkUser() {
        val hasLoggedIn = sharedPreferences.getBoolean("has_logged_in", false)

        if (!hasLoggedIn) {
            _destination.value = SplashDestination.Intro
            return
        }

        // 세션 유효성 확인: Firebase 인증 + 15분 이내
        val lastActive = sharedPreferences.getLong("last_active_time", 0L)
        val elapsed = System.currentTimeMillis() - lastActive
        val sessionValid = authRepository.isAuthenticated && lastActive > 0 && elapsed < SESSION_TIMEOUT_MS

        if (sessionValid) {
            // 세션 유효 → 로그인 스킵
            viewModelScope.launch {
                val uid = authRepository.currentUserId
                if (uid != null && userRepository.getUserOnce(uid) != null) {
                    syncManager.syncOnLogin(uid)
                    _destination.value = SplashDestination.Home
                } else {
                    authRepository.signOut()
                    _destination.value = SplashDestination.Login
                }
            }
        } else {
            // 세션 만료 → 로그아웃 후 로그인 화면
            authRepository.signOut()
            _destination.value = SplashDestination.Login
        }
    }
}
