package com.bodyquest.app.ui.boss

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.BossEntity
import com.bodyquest.app.data.local.entity.BossProgressEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.BossRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.domain.model.BattleLog
import com.bodyquest.app.domain.model.BossResult
import com.bodyquest.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class BossWithProgress(
    val boss: BossEntity,
    val isLocked: Boolean,      // 이전 보스 미클리어
    val isCleared: Boolean,     // 영구 클리어 여부
    val clearedGrade: String    // "S" | "A" | "B" | ""
)

data class BossState(
    val bossGroups: Map<String, List<BossWithProgress>> = emptyMap(),
    val user: UserEntity? = null,
    val battleLogs: List<BattleLog> = emptyList(),
    val isBattleActive: Boolean = false,
    val isBattleComplete: Boolean = false,
    val battleResult: BossResult? = null,
    val challengeResult: BossResult? = null
)

@HiltViewModel
class BossViewModel @Inject constructor(
    private val bossRepository: BossRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<BossState>>(UiState.Loading)
    val uiState: StateFlow<UiState<BossState>> = _uiState

    private val groupOrder = listOf("STRENGTH", "ENDURANCE", "HYBRID")

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
                    _uiState.value = UiState.Error("로그인이 필요합니다.")
                    return@launch
                }
                val user = userRepository.getUserOnce(uid)

                combine(
                    bossRepository.getAllBosses(),
                    bossRepository.getProgressForUser(uid)
                ) { bosses, progress ->
                    val progressMap = progress.associateBy { it.bossId }
                    val groups = buildBossGroups(bosses, progressMap)
                    val current = (_uiState.value as? UiState.Success)?.data ?: BossState()
                    current.copy(bossGroups = groups, user = user)
                }.collect { newState ->
                    _uiState.value = UiState.Success(newState)
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "보스 데이터를 불러올 수 없습니다.")
            }
        }
    }

    private fun buildBossGroups(
        bosses: List<BossEntity>,
        progressMap: Map<Int, BossProgressEntity>
    ): Map<String, List<BossWithProgress>> {
        return groupOrder.associateWith { type ->
            val sorted = bosses.filter { it.type == type }.sortedBy { it.order }

            sorted.mapIndexed { index, boss ->
                val progress = progressMap[boss.id]
                val isCleared = progress?.isCleared ?: false
                val prevCleared = if (index == 0) true
                else progressMap[sorted[index - 1].id]?.isCleared ?: false
                val grade = when (progress?.performance) {
                    "압도적인 승리" -> "S"
                    "안정적인 승리" -> "A"
                    "간신히 승리"   -> "B"
                    else            -> ""
                }

                BossWithProgress(
                    boss = boss,
                    isLocked = !prevCleared,
                    isCleared = isCleared,
                    clearedGrade = grade
                )
            }
        }.filterValues { it.isNotEmpty() }
    }

    private fun calcPerformance(user: UserEntity, boss: BossEntity): String {
        val margin = (user.strengthStat - boss.requiredStrength) +
                     (user.enduranceStat - boss.requiredEndurance)
        return when {
            margin >= 50 -> "압도적인 승리"
            margin >= 20 -> "안정적인 승리"
            else         -> "간신히 승리"
        }
    }

    fun challengeBoss(bossWithProgress: BossWithProgress) {
        if (bossWithProgress.isLocked) return

        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val user = current.user ?: return
        val boss = bossWithProgress.boss

        val missingStr = maxOf(0, boss.requiredStrength - user.strengthStat)
        val missingEnd = maxOf(0, boss.requiredEndurance - user.enduranceStat)
        val missingLvl = maxOf(0, boss.requiredLevel - user.level)
        val success = (missingStr == 0 && missingEnd == 0 && missingLvl == 0)

        val performance = if (success) calcPerformance(user, boss) else ""

        val result = BossResult(
            bossId = boss.id,
            bossName = boss.name,
            success = success,
            missingStrength = missingStr,
            missingEndurance = missingEnd,
            missingLevel = missingLvl,
            performance = performance
        )

        val logs = generateBattleLogs(
            userName = user.nickname,
            bossName = boss.name,
            isSuccess = success,
            performance = performance
        )

        _uiState.value = UiState.Success(
            current.copy(
                battleLogs = emptyList(),
                isBattleActive = true,
                isBattleComplete = false,
                battleResult = result,
                challengeResult = null
            )
        )

        viewModelScope.launch {
            for (log in logs) {
                val s = (_uiState.value as? UiState.Success)?.data ?: break
                _uiState.value = UiState.Success(s.copy(battleLogs = s.battleLogs + log))
                delay(700L)
            }
            val s = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.value = UiState.Success(s.copy(isBattleComplete = true))
        }
    }

    fun confirmBattle() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        val result = current.battleResult ?: return

        viewModelScope.launch {
            var ticketsEarned = 0
            if (result.success) {
                val uid = authRepository.currentUserId
                if (uid != null) {
                    val clearResult = bossRepository.recordClear(uid, result.bossId, result.performance)

                    // 등급별 티켓 지급 (S=3, A=2, B=1), 재도전 시 차이분만 추가
                    val newTickets = performanceToTickets(clearResult.bestPerformance)
                    val oldTickets = clearResult.previousPerformance?.let { performanceToTickets(it) } ?: 0
                    ticketsEarned = maxOf(0, newTickets - oldTickets)

                    if (ticketsEarned > 0) {
                        val user = userRepository.getUserOnce(uid)
                        if (user != null) {
                            userRepository.updateGachaTickets(uid, user.gachaTickets + ticketsEarned)
                            val updatedUser = userRepository.getUserOnce(uid)
                            if (updatedUser != null) syncManager.pushUserToCloud(updatedUser)
                        }
                    }

                    // Push boss progress to Firestore
                    syncManager.pushBossProgressToCloud(
                        uid,
                        BossProgressEntity(
                            bossId = result.bossId,
                            userId = uid,
                            isCleared = true,
                            performance = clearResult.bestPerformance
                        )
                    )
                }
            }

            val s = (_uiState.value as? UiState.Success)?.data ?: return@launch
            _uiState.value = UiState.Success(
                s.copy(
                    isBattleActive = false,
                    isBattleComplete = false,
                    battleLogs = emptyList(),
                    battleResult = null,
                    challengeResult = if (result.success) {
                        if (ticketsEarned > 0) result.copy(ticketsEarned = ticketsEarned) else null
                    } else result
                )
            )
        }
    }

    private fun performanceToTickets(performance: String): Int = when (performance) {
        "압도적인 승리" -> 3  // S
        "안정적인 승리" -> 2  // A
        "간신히 승리"   -> 1  // B
        else            -> 0
    }

    fun dismissResult() {
        val current = (_uiState.value as? UiState.Success)?.data ?: return
        _uiState.value = UiState.Success(current.copy(challengeResult = null))
    }
}
