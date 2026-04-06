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
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import kotlin.math.roundToInt

data class WorkoutHistoryItem(
    val workoutId: Long,
    val questName: String,
    val dateKey: String,        // "yyyy-MM-dd" (달력 lookup 키)
    val xpEarned: Int,
    val elapsedSeconds: Int,
    val statType: String,       // "STRENGTH" | "ENDURANCE"
    val statGained: Int         // 직업 배율 적용 후 실제 스탯 획득량
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

data class DailyWorkoutStat(
    val label: String,
    val count: Int
)

data class ProfileState(
    val cumulativeStats: CumulativeStats = CumulativeStats(),
    val calendarData: Map<String, List<WorkoutHistoryItem>> = emptyMap(),
    val accountInfo: AccountInfo? = null,
    val userJob: String = "",
    val weeklyStats: List<DailyWorkoutStat> = emptyList()
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
                    _uiState.value = UiState.Error("로그인이 필요합니다.")
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
                            ),
                            userJob = user.job
                        ))
                        loadCumulativeStats(user.id, firebaseUid)
                        loadCalendarData(user.id, user.job)
                        loadWeeklyStats(user.id)
                    } else {
                        _uiState.value = UiState.Success(ProfileState())
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "데이터를 불러올 수 없습니다.")
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

    private fun loadCalendarData(userId: Long, userJob: String) {
        val sixMonthsAgo = Calendar.getInstance().apply { add(Calendar.MONTH, -6) }.timeInMillis
        val job = viewModelScope.launch {
            try {
                workoutRepository.getCompletedWorkoutsSince(userId, sixMonthsAgo).collectLatest { workouts ->
                    val dateKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                    val questIds = workouts.map { it.questId }.distinct()
                    val questMap = questRepository.getQuestsByIds(questIds).associateBy { it.id }
                    val grouped = mutableMapOf<String, MutableList<WorkoutHistoryItem>>()
                    for (workout in workouts) {
                        val quest = questMap[workout.questId]
                        val statType = quest?.statType ?: "STRENGTH"
                        val statMultiplier = when (userJob) {
                            "STRENGTH" -> if (statType == "STRENGTH") 2.0f else 1.0f
                            "ENDURANCE" -> if (statType == "ENDURANCE") 2.0f else 1.0f
                            "BALANCE" -> 1.5f
                            else -> 1.0f
                        }
                        val statGained = ((quest?.statReward ?: 0) * statMultiplier).roundToInt()
                        val item = WorkoutHistoryItem(
                            workoutId = workout.id,
                            questName = quest?.name ?: "알 수 없는 퀘스트",
                            dateKey = dateKeyFormat.format(Date(workout.startTime)),
                            xpEarned = workout.xpEarned,
                            elapsedSeconds = workout.elapsedSeconds,
                            statType = statType,
                            statGained = statGained
                        )
                        grouped.getOrPut(item.dateKey) { mutableListOf() }.add(item)
                    }
                    updateSuccessState { it.copy(calendarData = grouped) }
                }
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "달력 데이터 로딩 실패", e)
            }
        }
        subJobs.add(job)
    }

    private fun loadWeeklyStats(userId: Long) {
        val cal = Calendar.getInstance()
        while (cal.get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
            cal.add(Calendar.DAY_OF_YEAR, -1)
        }
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val weekStart = cal.timeInMillis

        val job = viewModelScope.launch {
            try {
                workoutRepository.getWeekWorkouts(userId, weekStart).collectLatest { workouts ->
                    val dayLabels = listOf("월", "화", "수", "목", "금", "토", "일")
                    val calDays = listOf(
                        Calendar.MONDAY, Calendar.TUESDAY, Calendar.WEDNESDAY,
                        Calendar.THURSDAY, Calendar.FRIDAY, Calendar.SATURDAY, Calendar.SUNDAY
                    )
                    val countByDay = workouts.groupBy { w ->
                        Calendar.getInstance().apply { timeInMillis = w.startTime }
                            .get(Calendar.DAY_OF_WEEK)
                    }
                    val stats = dayLabels.mapIndexed { index, label ->
                        DailyWorkoutStat(
                            label = label,
                            count = countByDay[calDays[index]]?.size ?: 0
                        )
                    }
                    updateSuccessState { it.copy(weeklyStats = stats) }
                }
            } catch (e: Exception) {
                Log.w("ProfileViewModel", "주간 통계 로딩 실패", e)
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
                        result.exceptionOrNull()?.message ?: "계정 삭제에 실패했습니다."
                    )
                }
            } catch (e: Exception) {
                _deleteState.value = DeleteState.Error(
                    e.message ?: "계정 삭제에 실패했습니다."
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
