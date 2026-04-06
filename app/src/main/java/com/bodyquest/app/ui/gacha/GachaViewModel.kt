package com.bodyquest.app.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.repository.SkinInventoryRepository
import com.bodyquest.app.util.AppLogger
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.remote.SyncManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GachaViewModel @Inject constructor(
    private val skinInventoryRepository: SkinInventoryRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val _ticketCount = MutableStateFlow(0)
    val ticketCount: StateFlow<Int> = _ticketCount

    init {
        loadTickets()
    }

    private fun loadTickets() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUser(uid).collectLatest { user ->
                _ticketCount.value = user?.gachaTickets ?: 0
            }
        }
    }

    fun consumeTicket(): Boolean {
        val current = _ticketCount.value
        if (current <= 0) return false
        val uid = auth.currentUser?.uid ?: return false
        _ticketCount.value = current - 1
        viewModelScope.launch {
            userRepository.updateGachaTickets(uid, current - 1)
            try {
                val updatedUser = userRepository.getUserOnce(uid)
                if (updatedUser != null) syncManager.pushUserToCloud(updatedUser)
            } catch (e: Exception) {
                AppLogger.w("GachaViewModel", "티켓 클라우드 push 실패", e)
            }
        }
        return true
    }

    fun onGachaResolved(skinId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            skinInventoryRepository.addOrIncrement(uid, skinId)
            try {
                val item = skinInventoryRepository.getItem(uid, skinId)
                if (item != null) {
                    syncManager.pushSkinInventoryToCloud(uid, item)
                }
            } catch (e: Exception) {
                AppLogger.w("GachaViewModel", "인벤토리 클라우드 push 실패", e)
            }
        }
    }
}
