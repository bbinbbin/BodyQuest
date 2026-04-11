package com.bodyquest.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.SkinInventoryRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.domain.model.ALL_SKINS
import com.bodyquest.app.domain.model.SkinCategory
import com.bodyquest.app.domain.model.SkinItem

private fun String?.isSetSkin() =
    this != null && ALL_SKINS.find { it.id == this }?.category == SkinCategory.SET
import com.bodyquest.app.util.AppLogger
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class InventoryViewModel @Inject constructor(
    private val skinInventoryRepository: SkinInventoryRepository,
    private val userRepository: UserRepository,
    private val syncManager: SyncManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    private val uid = auth.currentUser?.uid

    val inventory: StateFlow<List<Pair<SkinItem, Int>>> = run {
        if (uid != null) {
            skinInventoryRepository.getInventory(uid)
                .map { entities ->
                    entities.mapNotNull { entity ->
                        val skin = ALL_SKINS.find { it.id == entity.skinId } ?: return@mapNotNull null
                        skin to entity.count
                    }
                }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        } else {
            flowOf(emptyList<Pair<SkinItem, Int>>())
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
        }
    }

    /** TOP 슬롯에 장착된 스킨 ID (equippedSkinId) */
    val equippedTopId: StateFlow<String?> = run {
        if (uid != null) {
            userRepository.getUser(uid)
                .map { it?.equippedSkinId }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } else {
            flowOf(null).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    /** BOTTOM 슬롯에 장착된 스킨 ID (equippedBottomId) */
    val equippedBottomId: StateFlow<String?> = run {
        if (uid != null) {
            userRepository.getUser(uid)
                .map { it?.equippedBottomId }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } else {
            flowOf(null).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    /** HAT 슬롯에 장착된 스킨 ID (equippedHatId) */
    val equippedHatId: StateFlow<String?> = run {
        if (uid != null) {
            userRepository.getUser(uid)
                .map { it?.equippedHatId }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } else {
            flowOf(null).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    fun isEquipped(skin: SkinItem, topId: String?, bottomId: String?, hatId: String?): Boolean {
        // SET 스킨이 활성화 중이면 개별 스킨은 장착중으로 표시하지 않음
        val setIsActive = topId.isSetSkin()
        if (setIsActive && skin.category != SkinCategory.SET) return false
        return when (skin.category) {
            SkinCategory.TOP -> skin.id == topId
            SkinCategory.BOTTOM -> skin.id == bottomId
            SkinCategory.HAT -> skin.id == hatId
            SkinCategory.SET -> skin.id == topId  // 세트는 equippedSkinId(TOP 슬롯)에 저장
            else -> false
        }
    }

    fun equipSkin(skin: SkinItem) {
        val uid = uid ?: return
        viewModelScope.launch {
            when (skin.category) {
                SkinCategory.TOP -> {
                    // equippedSkinId 덮어쓰기 → SET 스킨이 있어도 자동으로 교체됨
                    userRepository.updateEquippedSkin(uid, skin.id)
                }
                SkinCategory.BOTTOM -> {
                    // SET 스킨이 활성 중이면 먼저 해제
                    if (userRepository.getUserOnce(uid)?.equippedSkinId.isSetSkin()) {
                        userRepository.updateEquippedSkin(uid, null)
                    }
                    userRepository.updateEquippedBottom(uid, skin.id)
                }
                SkinCategory.HAT -> {
                    // SET 스킨이 활성 중이면 먼저 해제
                    if (userRepository.getUserOnce(uid)?.equippedSkinId.isSetSkin()) {
                        userRepository.updateEquippedSkin(uid, null)
                    }
                    userRepository.updateEquippedHat(uid, skin.id)
                }
                SkinCategory.SET -> {
                    // 세트 장착: 모든 슬롯 초기화 후 equippedSkinId에만 저장
                    userRepository.updateEquippedSkin(uid, skin.id)
                    userRepository.updateEquippedBottom(uid, null)
                    userRepository.updateEquippedHat(uid, null)
                }
                else -> {}
            }
            pushToCloud(uid)
        }
    }

    fun unequipSkin(skin: SkinItem) {
        val uid = uid ?: return
        viewModelScope.launch {
            when (skin.category) {
                SkinCategory.TOP -> userRepository.updateEquippedSkin(uid, null)
                SkinCategory.BOTTOM -> userRepository.updateEquippedBottom(uid, null)
                SkinCategory.HAT -> userRepository.updateEquippedHat(uid, null)
                SkinCategory.SET -> userRepository.updateEquippedSkin(uid, null)
                else -> {}
            }
            pushToCloud(uid)
        }
    }

    private suspend fun pushToCloud(uid: String) {
        try {
            val user = userRepository.getUserOnce(uid)
            if (user != null) syncManager.pushUserToCloud(user)
        } catch (e: Exception) {
            AppLogger.w("InventoryViewModel", "장착 클라우드 push 실패", e)
        }
    }
}
