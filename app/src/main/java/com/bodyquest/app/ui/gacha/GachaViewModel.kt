package com.bodyquest.app.ui.gacha

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.repository.SkinInventoryRepository
import com.bodyquest.app.data.remote.SyncManager
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GachaViewModel @Inject constructor(
    private val skinInventoryRepository: SkinInventoryRepository,
    private val syncManager: SyncManager,
    private val auth: FirebaseAuth
) : ViewModel() {

    fun onGachaResolved(skinId: String) {
        val uid = auth.currentUser?.uid ?: return
        viewModelScope.launch {
            skinInventoryRepository.addOrIncrement(uid, skinId)
            try {
                val item = skinInventoryRepository.getItem(uid, skinId)
                if (item != null) {
                    syncManager.pushSkinInventoryToCloud(uid, item)
                }
            } catch (_: Exception) {}
        }
    }
}
