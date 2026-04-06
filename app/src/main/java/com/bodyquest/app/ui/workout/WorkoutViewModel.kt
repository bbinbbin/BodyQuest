package com.bodyquest.app.ui.workout

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.local.entity.WorkoutEntity
import com.bodyquest.app.data.local.entity.WorkoutSetEntity
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import com.bodyquest.app.util.XpCalculator
import dagger.hilt.android.lifecycle.HiltViewModel
import android.util.Log
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class WorkoutState(
    val quest: QuestEntity? = null,
    val workoutId: Long = 0,
    val elapsedSeconds: Int = 0,
    val currentSet: Int = 1,
    val completedSets: Int = 0,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val heartRate: Int = 0,
    val caloriesBurned: Int = 0,
    val rewardError: String? = null
)

data class WorkoutCompleteState(
    val questName: String = "",
    val questCategory: String = "",
    val elapsedSeconds: Int = 0,
    val totalSets: Int = 0,
    val heartRateAvg: Int = 0,
    val caloriesBurned: Int = 0,
    val xpEarned: Int = 0,
    val statType: String = "",
    val baseStatReward: Int = 0,   // 직업 효과 적용 전 기본값
    val statReward: Int = 0,       // 직업 효과 적용 후 최종값
    val leveledUp: Boolean = false,
    val newLevel: Int = 1
)

@HiltViewModel
class WorkoutViewModel @Inject constructor(
    private val questRepository: QuestRepository,
    private val workoutRepository: WorkoutRepository,
    private val userRepository: UserRepository,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _state = MutableStateFlow(WorkoutState())
    val state: StateFlow<WorkoutState> = _state

    private val _completeState = MutableStateFlow(WorkoutCompleteState())
    val completeState: StateFlow<WorkoutCompleteState> = _completeState

    private var timerJob: Job? = null
    private var heartRateSimJob: Job? = null

    fun loadQuest(questId: String) {
        viewModelScope.launch {
            val quest = questRepository.getQuestById(questId) ?: return@launch
            val uid = authRepository.currentUserId ?: return@launch
            val user = userRepository.getUserOnce(uid) ?: return@launch

            val workout = WorkoutEntity(
                questId = questId,
                userId = user.id,
                startTime = System.currentTimeMillis()
            )
            val workoutId = workoutRepository.startWorkout(workout)

            _state.value = WorkoutState(
                quest = quest,
                workoutId = workoutId
            )
        }
    }

    fun startWorkout() {
        _state.value = _state.value.copy(isRunning = true)
        startTimer()
        startHeartRateSimulation()
    }

    fun pauseWorkout() {
        _state.value = _state.value.copy(isRunning = false)
        timerJob?.cancel()
        heartRateSimJob?.cancel()
    }

    fun resumeWorkout() {
        _state.value = _state.value.copy(isRunning = true)
        startTimer()
        startHeartRateSimulation()
    }

    fun completeSet() {
        val s = _state.value
        val quest = s.quest ?: return

        viewModelScope.launch {
            val set = WorkoutSetEntity(
                workoutId = s.workoutId,
                setNumber = s.currentSet,
                reps = quest.repsPerSet,
                completed = true,
                completedAt = System.currentTimeMillis()
            )
            workoutRepository.insertWorkoutSet(set)

            val newCompleted = s.completedSets + 1
            if (newCompleted >= quest.sets) {
                finishWorkout()
            } else {
                _state.value = s.copy(
                    completedSets = newCompleted,
                    currentSet = s.currentSet + 1
                )
            }
        }
    }

    private fun finishWorkout() {
        timerJob?.cancel()
        heartRateSimJob?.cancel()

        val s = _state.value
        val quest = s.quest ?: return

        viewModelScope.launch {
            try {
                val endTime = System.currentTimeMillis()
                val caloriesBurned = estimateCalories(s.elapsedSeconds, quest.difficulty)
                val heartRateAvg = if (s.heartRate > 0) s.heartRate else simulateAvgHeartRate(quest.difficulty)

                val uid = authRepository.currentUserId
                val user = if (uid != null) userRepository.getUserOnce(uid) else null
                if (user != null) {
                    // Update workout with correct userId
                    val completedWorkout = WorkoutEntity(
                        id = s.workoutId,
                        questId = quest.id,
                        userId = user.id,
                        startTime = endTime - (s.elapsedSeconds * 1000L),
                        endTime = endTime,
                        elapsedSeconds = s.elapsedSeconds,
                        caloriesBurned = caloriesBurned,
                        heartRateAvg = heartRateAvg,
                        completed = true,
                        xpEarned = quest.xpReward
                    )
                    workoutRepository.updateWorkout(completedWorkout)

                    // Calculate new XP and level
                    val (newLevel, remainingXp) = XpCalculator.calculateNewLevel(
                        user.level, user.xp, quest.xpReward
                    )
                    val leveledUp = newLevel > user.level

                    // 직업별 스탯 배율
                    val statMultiplier = when (user.job) {
                        "STRENGTH"  -> if (quest.statType == "STRENGTH") 2.0f else 1.0f
                        "ENDURANCE" -> if (quest.statType == "ENDURANCE") 2.0f else 1.0f
                        "BALANCE"   -> 1.5f
                        else        -> 1.0f
                    }
                    val actualStatReward = (quest.statReward * statMultiplier).roundToInt()

                    // Calculate new stat value
                    val newStatValue = when (quest.statType) {
                        "STRENGTH" -> user.strengthStat + actualStatReward
                        "ENDURANCE" -> user.enduranceStat + actualStatReward
                        else -> 0
                    }

                    // Apply all rewards atomically in a single transaction
                    userRepository.applyWorkoutRewards(
                        userId = user.id,
                        newXp = remainingXp,
                        newLevel = newLevel,
                        statType = quest.statType,
                        newStatValue = newStatValue
                    )

                    _completeState.value = WorkoutCompleteState(
                        questName = quest.name,
                        questCategory = quest.category,
                        elapsedSeconds = s.elapsedSeconds,
                        totalSets = quest.sets,
                        heartRateAvg = heartRateAvg,
                        caloriesBurned = caloriesBurned,
                        xpEarned = quest.xpReward,
                        statType = quest.statType,
                        baseStatReward = quest.statReward,
                        statReward = actualStatReward,
                        leveledUp = leveledUp,
                        newLevel = newLevel
                    )

                    // Push to cloud
                    if (uid != null) {
                        val sets = workoutRepository.getSetsForWorkoutOnce(s.workoutId)
                        syncManager.pushCompletedWorkout(uid, completedWorkout, sets)
                        val updatedUser = userRepository.getUserOnce(uid)
                        if (updatedUser != null) {
                            syncManager.pushUserToCloud(updatedUser)
                        }
                    }
                }

                _state.value = s.copy(
                    isRunning = false,
                    isCompleted = true,
                    completedSets = quest.sets,
                    caloriesBurned = caloriesBurned
                )
            } catch (e: Exception) {
                Log.e("WorkoutViewModel", "보상 저장 실패", e)
                _state.value = s.copy(
                    isRunning = false,
                    isCompleted = true,
                    completedSets = quest.sets,
                    rewardError = "운동은 완료되었으나 보상 저장에 실패했습니다."
                )
            }
        }
    }

    fun loadCompleteData(workoutId: Long) {
        viewModelScope.launch {
            val workout = workoutRepository.getWorkoutById(workoutId) ?: return@launch
            val quest = questRepository.getQuestById(workout.questId) ?: return@launch
            val uid = authRepository.currentUserId
            val user = if (uid != null) userRepository.getUserOnce(uid) else null

            // 직업별 배율 재계산
            val statMultiplier = when (user?.job) {
                "STRENGTH"  -> if (quest.statType == "STRENGTH") 2.0f else 1.0f
                "ENDURANCE" -> if (quest.statType == "ENDURANCE") 2.0f else 1.0f
                "BALANCE"   -> 1.5f
                else        -> 1.0f
            }
            val actualStatReward = (quest.statReward * statMultiplier).roundToInt()

            _completeState.value = WorkoutCompleteState(
                questName = quest.name,
                questCategory = quest.category,
                elapsedSeconds = workout.elapsedSeconds,
                totalSets = quest.sets,
                heartRateAvg = workout.heartRateAvg,
                caloriesBurned = workout.caloriesBurned,
                xpEarned = workout.xpEarned,
                statType = quest.statType,
                baseStatReward = quest.statReward,
                statReward = actualStatReward,
                newLevel = user?.level ?: 1
            )
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _state.value
                if (current.isRunning) {
                    val newElapsed = current.elapsedSeconds + 1
                    val newCalories = estimateCalories(newElapsed, current.quest?.difficulty ?: 1)
                    _state.value = current.copy(
                        elapsedSeconds = newElapsed,
                        caloriesBurned = newCalories
                    )
                }
            }
        }
    }

    private fun startHeartRateSimulation() {
        heartRateSimJob?.cancel()
        heartRateSimJob = viewModelScope.launch {
            while (true) {
                delay(3000)
                val quest = _state.value.quest ?: continue
                val baseHr = when (quest.difficulty) {
                    1 -> 100
                    2 -> 120
                    3 -> 140
                    else -> 110
                }
                val hr = baseHr + (-10..10).random()
                _state.value = _state.value.copy(heartRate = hr)
            }
        }
    }

    private fun estimateCalories(elapsedSeconds: Int, difficulty: Int): Int {
        val metValue = when (difficulty) {
            1 -> 4.0
            2 -> 6.0
            3 -> 8.0
            else -> 5.0
        }
        // Simplified: calories = MET * weight(kg) * time(hours)
        // Assuming ~70kg user
        return (metValue * 70 * elapsedSeconds / 3600.0).toInt()
    }

    private fun simulateAvgHeartRate(difficulty: Int): Int {
        return when (difficulty) {
            1 -> (95..110).random()
            2 -> (115..130).random()
            3 -> (130..150).random()
            else -> (100..120).random()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        heartRateSimJob?.cancel()
    }
}
