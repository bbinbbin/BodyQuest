package com.bodyquest.wear.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.wear.data.PhoneConnectionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class WearHomeState(
    val isPhoneConnected: Boolean = false,
    val isChecking: Boolean = true,
    val lastPingSuccess: Boolean? = null
)

@HiltViewModel
class WearHomeViewModel @Inject constructor(
    private val phoneConnectionRepository: PhoneConnectionRepository
) : ViewModel() {

    private val _state = MutableStateFlow(WearHomeState())
    val state: StateFlow<WearHomeState> = _state

    init {
        startConnectionMonitoring()
    }

    private fun startConnectionMonitoring() {
        viewModelScope.launch {
            while (true) {
                val connected = phoneConnectionRepository.checkConnection()
                _state.value = _state.value.copy(
                    isPhoneConnected = connected,
                    isChecking = false
                )
                delay(5000)
            }
        }
    }

    fun sendTestPing() {
        viewModelScope.launch {
            val success = phoneConnectionRepository.sendPingToPhone()
            _state.value = _state.value.copy(lastPingSuccess = success)
        }
    }
}
