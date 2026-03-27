package com.bodyquest.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import com.bodyquest.app.ui.common.UiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

data class CompletedQuestInfo(
    val questName: String,
    val xpEarned: Int
)

data class HomeState(
    val user: UserEntity? = null,
    val todaysQuests: List<CompletedQuestInfo> = emptyList(),
    val recommendedQuests: List<QuestEntity> = emptyList(),
    val weekWorkoutDays: Set<Int> = emptySet()
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeState>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeState>> = _uiState

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
                userRepository.getUser(uid).collectLatest { user ->
                    if (user != null) {
                        val currentData = (_uiState.value as? UiState.Success)?.data ?: HomeState()
                        _uiState.value = UiState.Success(currentData.copy(user = user))
                        loadTodaysQuests(user.id)
                        loadWeekWorkouts(user.id)
                        loadRecommendedQuests(user.job)
                    } else {
                        _uiState.value = UiState.Success(HomeState())
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "데이터를 불러올 수 없습니다")
            }
        }
    }

    private fun loadTodaysQuests(userId: Long) {
        val startOfDay = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            try {
                workoutRepository.getTodaysCompletedWorkouts(userId, startOfDay).collectLatest { workouts ->
                    val questInfos = workouts.map { workout ->
                        val quest = questRepository.getQuestById(workout.questId)
                        CompletedQuestInfo(
                            questName = quest?.name ?: "알 수 없는 퀘스트",
                            xpEarned = workout.xpEarned
                        )
                    }
                    updateSuccessState { it.copy(todaysQuests = questInfos) }
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadRecommendedQuests(userJob: String) {
        viewModelScope.launch {
            try {
                val allCategories = listOf("STRENGTH", "ENDURANCE", "BALANCE")
                val otherCategory = allCategories.filter { it != userJob }.random()

                combine(
                    questRepository.getQuestsByCategory(userJob),
                    questRepository.getQuestsByCategory(otherCategory)
                ) { jobQuests, otherQuests ->
                    val mainPicks = jobQuests.shuffled().take(2)
                    val otherPick = otherQuests.shuffled().take(1)
                    mainPicks + otherPick
                }.collectLatest { recommended ->
                    updateSuccessState { it.copy(recommendedQuests = recommended) }
                }
            } catch (_: Exception) { }
        }
    }

    private fun loadWeekWorkouts(userId: Long) {
        val weekStart = Calendar.getInstance().apply {
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            try {
                workoutRepository.getWeekWorkouts(userId, weekStart).collectLatest { workouts ->
                    val days = workouts.map { workout ->
                        Calendar.getInstance().apply {
                            timeInMillis = workout.startTime
                        }.get(Calendar.DAY_OF_WEEK)
                    }.toSet()
                    updateSuccessState { it.copy(weekWorkoutDays = days) }
                }
            } catch (_: Exception) { }
        }
    }

    private fun updateSuccessState(update: (HomeState) -> HomeState) {
        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(update(current.data))
        }
    }
}
