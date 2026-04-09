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

    private val _avatarIndex = MutableStateFlow(0)
    val avatarIndex: StateFlow<Int> = _avatarIndex

    init {
        loadUserData()
    }

    private fun loadUserData() {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            userRepository.getUser(uid).collectLatest { user ->
                _ticketCount.value = user?.gachaTickets ?: 0
                _avatarIndex.value = user?.avatarIndex ?: 0
            }
        }
    }

    fun canConsume(): Boolean =
        _ticketCount.value > 0 && auth.currentUser?.uid != null

    /**
     * 티켓 차감 + 스킨 인벤토리 추가를 하나의 코루틴에서 순차 실행.
     * DB 티켓 차감 성공 후에만 스킨 추가 → UI 반영.
     */
    fun consumeAndReward(skinId: String) {
        val uid = auth.currentUser?.uid ?: return
        val current = _ticketCount.value
        if (current <= 0) return
        viewModelScope.launch {
            try {
                // 1) 로컬 DB 티켓 차감
                userRepository.updateGachaTickets(uid, current - 1)
                // 2) 로컬 DB 스킨 추가
                skinInventoryRepository.addOrIncrement(uid, skinId)
                // 3) 둘 다 성공 시 UI 반영
                _ticketCount.value = current - 1
                // 4) 클라우드 동기화 (실패해도 로컬은 이미 반영됨)
                try {
                    val updatedUser = userRepository.getUserOnce(uid)
                    if (updatedUser != null) syncManager.pushUserToCloud(updatedUser)
                    val item = skinInventoryRepository.getItem(uid, skinId)
                    if (item != null) syncManager.pushSkinInventoryToCloud(uid, item)
                } catch (e: Exception) {
                    AppLogger.w("GachaViewModel", "클라우드 push 실패", e)
                }
            } catch (e: Exception) {
                AppLogger.e("GachaViewModel", "뽑기 처리 실패 — 티켓/스킨 미반영", e)
            }
        }
    }
}
