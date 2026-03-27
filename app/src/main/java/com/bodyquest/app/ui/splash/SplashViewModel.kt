package com.bodyquest.app.ui.splash

import android.content.SharedPreferences
import androidx.lifecycle.ViewModel
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
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
    private val sharedPreferences: SharedPreferences
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination: StateFlow<SplashDestination> = _destination

    fun checkUser() {
        authRepository.signOut()
        val hasLoggedIn = sharedPreferences.getBoolean("has_logged_in", false)
        _destination.value = if (hasLoggedIn) SplashDestination.Login else SplashDestination.Intro
    }
}
