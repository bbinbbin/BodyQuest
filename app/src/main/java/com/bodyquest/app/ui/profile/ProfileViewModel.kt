package com.bodyquest.app.ui.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.remote.FirestoreUserService
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.BossRepository
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import com.bodyquest.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

data class WorkoutHistoryItem(
    val questName: String,
    val date: String,
    val xpEarned: Int
)

data class CumulativeStats(
    val totalWorkouts: Int = 0,
    val totalXp: Int = 0,
    val totalElapsedSeconds: Int = 0,
    val bossClears: Int = 0
)

data class AccountInfo(
    val email: String?,
    val createdAt: Long,
    val authProvider: String?
)

data class ProfileState(
    val cumulativeStats: CumulativeStats = CumulativeStats(),
    val workoutHistory: List<WorkoutHistoryItem> = emptyList(),
    val accountInfo: AccountInfo? = null
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val workoutRepository: WorkoutRepository,
    private val bossRepository: BossRepository,
    private val questRepository: QuestRepository,
    private val firestoreService: FirestoreUserService
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<ProfileState>>(UiState.Loading)
    val uiState: StateFlow<UiState<ProfileState>> = _uiState

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    private var loadJob: Job? = null
    private var subJobs = mutableListOf<Job>()
    private val dateFormat = SimpleDateFormat("yyyy.MM.dd", Locale.KOREA)

    init {
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        subJobs.forEach { it.cancel() }
        subJobs.clear()

        loadJob = viewModelScope.launch {
            try {
                val firebaseUid = authRepository.currentUserId
                if (firebaseUid == null) {
                    _uiState.value = UiState.Error("로그인이 필요합니다")
                    return@launch
                }

                userRepository.getUser(firebaseUid).collectLatest { user ->
                    subJobs.forEach { it.cancel() }
                    subJobs.clear()

                    if (user != null) {
                        _uiState.value = UiState.Success(ProfileState(
                            accountInfo = AccountInfo(
                                email = user.email,
                                createdAt = user.createdAt,
                                authProvider = user.authProvider
                            )
                        ))
                        loadCumulativeStats(user.id, firebaseUid)
                        loadWorkoutHistory(user.id)
                    } else {
                        _uiState.value = UiState.Success(ProfileState())
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "데이터를 불러올 수 없습니다")
            }
        }
    }

    private fun loadCumulativeStats(userId: Long, firebaseUid: String) {
        val job = viewModelScope.launch {
            try {
                combine(
                    workoutRepository.getCompletedWorkoutCount(userId),
                    workoutRepository.getTotalXpEarned(userId),
                    workoutRepository.getTotalElapsedSeconds(userId),
                    bossRepository.getClearedBossCount(firebaseUid)
                ) { count, xp, seconds, bossClears ->
                    CumulativeStats(
                        totalWorkouts = count,
                        totalXp = xp,
                        totalElapsedSeconds = seconds,
                        bossClears = bossClears
                    )
                }.collectLatest { stats ->
                    updateSuccessState { it.copy(cumulativeStats = stats) }
                }
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "누적 통계 로딩 실패", e)
            }
        }
        subJobs.add(job)
    }

    private fun loadWorkoutHistory(userId: Long) {
        val job = viewModelScope.launch {
            try {
                workoutRepository.getRecentCompletedWorkouts(userId, 20).collectLatest { workouts ->
                    val items = workouts.map { workout ->
                        val quest = questRepository.getQuestById(workout.questId)
                        WorkoutHistoryItem(
                            questName = quest?.name ?: "알 수 없는 퀘스트",
                            date = dateFormat.format(Date(workout.startTime)),
                            xpEarned = workout.xpEarned
                        )
                    }
                    updateSuccessState { it.copy(workoutHistory = items) }
                }
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "운동 히스토리 로딩 실패", e)
            }
        }
        subJobs.add(job)
    }

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount() {
        val uid = authRepository.currentUserId ?: return
        _deleteState.value = DeleteState.Loading

        viewModelScope.launch {
            try {
                try {
                    firestoreService.deleteUser(uid)
                } catch (_: Exception) { }

                userRepository.deleteUserByFirebaseUid(uid)

                val result = authRepository.deleteAccount()
                if (result.isSuccess) {
                    _deleteState.value = DeleteState.Success
                } else {
                    _deleteState.value = DeleteState.Error(
                        result.exceptionOrNull()?.message ?: "계정 삭제에 실패했습니다"
                    )
                }
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(
                    e.message ?: "계정 삭제에 실패했습니다"
                )
            }
        }
    }

    private fun updateSuccessState(update: (ProfileState) -> ProfileState) {
        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(update(current.data))
        }
    }

    companion object {
        fun formatElapsedTime(totalSeconds: Int): String {
            val hours = totalSeconds / 3600
            val minutes = (totalSeconds % 3600) / 60
            return if (hours > 0) "${hours}시간 ${minutes}분" else "${minutes}분"
        }

        fun formatAuthProvider(provider: String?): String = when (provider) {
            "GOOGLE" -> "Google"
            "EMAIL" -> "이메일"
            else -> provider ?: "알 수 없음"
        }
    }
}

sealed interface DeleteState {
    data object Idle : DeleteState
    data object Loading : DeleteState
    data object Success : DeleteState
    data class Error(val message: String) : DeleteState
}
