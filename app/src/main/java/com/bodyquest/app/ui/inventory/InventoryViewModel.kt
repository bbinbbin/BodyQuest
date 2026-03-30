package com.bodyquest.app.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.SkinInventoryRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.domain.model.ALL_SKINS
import com.bodyquest.app.domain.model.SkinItem
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
    auth: FirebaseAuth
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

    val equippedSkinId: StateFlow<String?> = run {
        if (uid != null) {
            userRepository.getUser(uid)
                .map { it?.equippedSkinId }
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        } else {
            flowOf(null)
                .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)
        }
    }

    fun equipSkin(skinId: String) {
        val uid = uid ?: return
        viewModelScope.launch {
            userRepository.updateEquippedSkin(uid, skinId)
            val user = userRepository.getUserOnce(uid)
            if (user != null) syncManager.pushUserToCloud(user)
        }
    }

    fun unequipSkin() {
        val uid = uid ?: return
        viewModelScope.launch {
            userRepository.updateEquippedSkin(uid, null)
            val user = userRepository.getUserOnce(uid)
            if (user != null) syncManager.pushUserToCloud(user)
        }
    }
}
