package com.bodyquest.app.ui.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.BossRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.domain.model.BossResult
import com.bodyquest.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BossState(
    val bosses: List<BossEntity> = emptyList(),
    val user: UserEntity? = null,
    val challengeResult: BossResult? = null
)

@HiltViewModel
class BossViewModel @Inject constructor(
    private val bossRepository: BossRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<BossState>>(UiState.Loading)
    val uiState: StateFlow<UiState<BossState>> = _uiState

    init {
        loadData()
    }

    fun retry() {
        _uiState.value = UiState.Loading
        loadData()
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                val uid = authRepository.currentUserId
                if (uid == null) {
                    _uiState.value = UiState.Error("로그인이 필요합니다")
                    return@launch
                }
                val user = userRepository.getUserOnce(uid)
                bossRepository.getAllBosses().collectLatest { bosses ->
                    val current = (_uiState.value as? UiState.Success)?.data ?: BossState()
                    _uiState.value = UiState.Success(current.copy(bosses = bosses, user = user))
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "보스 데이터를 불러올 수 없습니다")
            }
        }
    }

    fun challengeBoss(boss: BossEntity) {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val user = current.user ?: return

        val missingStr = maxOf(0, boss.requiredStrength - user.strengthStat)
        val missingEnd = maxOf(0, boss.requiredEndurance - user.enduranceStat)
        val missingLvl = maxOf(0, boss.requiredLevel - user.level)
        val success = (missingStr == 0 && missingEnd == 0 && missingLvl == 0)

        val result = BossResult(
            bossId = boss.id,
            bossName = boss.name,
            success = success,
            missingStrength = missingStr,
            missingEndurance = missingEnd,
            missingLevel = missingLvl
        )
        _uiState.value = UiState.Success(current.copy(challengeResult = result))
    }

    fun dismissResult() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(challengeResult = null))
    }
}
