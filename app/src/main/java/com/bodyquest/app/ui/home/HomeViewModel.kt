package com.bodyquest.app.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
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
    val weekWorkoutDays: Set<Int> = emptySet() // 0=Sun, 1=Mon, ... 6=Sat
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val workoutRepository: WorkoutRepository
) : ViewModel() {

    private val _state = MutableStateFlow(HomeState())
    val state: StateFlow<HomeState> = _state

    init {
        loadUser()
    }

    private fun loadUser() {
        viewModelScope.launch {
            userRepository.getUser().collectLatest { user ->
                _state.value = _state.value.copy(user = user)
                if (user != null) {
                    loadTodaysQuests(user.id)
                    loadWeekWorkouts(user.id)
                    loadRecommendedQuests(user.job)
                }
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
            workoutRepository.getTodaysCompletedWorkouts(userId, startOfDay).collectLatest { workouts ->
                val questInfos = workouts.map { workout ->
                    val quest = questRepository.getQuestById(workout.questId)
                    CompletedQuestInfo(
                        questName = quest?.name ?: "알 수 없는 퀘스트",
                        xpEarned = workout.xpEarned
                    )
                }
                _state.value = _state.value.copy(todaysQuests = questInfos)
            }
        }
    }

    private fun loadRecommendedQuests(userJob: String) {
        viewModelScope.launch {
            questRepository.getQuestsByCategory(userJob).collectLatest { jobQuests ->
                // Pick 2 from user's main job + 1 from another category
                val allCategories = listOf("STRENGTH", "ENDURANCE", "BALANCE")
                val otherCategory = allCategories.filter { it != userJob }.random()

                val mainPicks = jobQuests.shuffled().take(2)

                questRepository.getQuestsByCategory(otherCategory).collectLatest { otherQuests ->
                    val otherPick = otherQuests.shuffled().take(1)
                    _state.value = _state.value.copy(
                        recommendedQuests = (mainPicks + otherPick)
                    )
                }
            }
        }
    }

    private fun loadWeekWorkouts(userId: Long) {
        val weekStart = Calendar.getInstance().apply {
            // Go back to most recent Monday (locale-independent)
            while (get(Calendar.DAY_OF_WEEK) != Calendar.MONDAY) {
                add(Calendar.DAY_OF_YEAR, -1)
            }
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        viewModelScope.launch {
            workoutRepository.getWeekWorkouts(userId, weekStart).collectLatest { workouts ->
                val days = workouts.map { workout ->
                    Calendar.getInstance().apply {
                        timeInMillis = workout.startTime
                    }.get(Calendar.DAY_OF_WEEK)
                }.toSet()
                _state.value = _state.value.copy(weekWorkoutDays = days)
            }
        }
    }
}
