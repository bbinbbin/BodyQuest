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
import com.bodyquest.app.util.AppLogger
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.roundToInt

data class SetRowData(
    val setNumber: Int,
    val weight: String = "",
    val reps: String = "",
    val completed: Boolean = false
)

data class WorkoutState(
    val quest: QuestEntity? = null,
    val workoutId: Long = 0,
    val elapsedSeconds: Int = 0,
    val currentSet: Int = 1,
    val completedSets: Int = 0,
    val totalSets: Int = 0,
    val isRunning: Boolean = false,
    val isCompleted: Boolean = false,
    val heartRate: Int = 0,
    val caloriesBurned: Int = 0,
    val rewardError: String? = null,
    val setRows: List<SetRowData> = emptyList(),  // STRENGTH: 세트 테이블
    val showGuide: Boolean = true
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
    val newLevel: Int = 1,
    val syncFailed: Boolean = false
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

            val isStrength = quest.category == "STRENGTH"
            val initialRows = if (isStrength) {
                (1..quest.sets).map { i ->
                    SetRowData(setNumber = i, reps = quest.repsPerSet.toString())
                }
            } else emptyList()

            _state.value = WorkoutState(
                quest = quest,
                workoutId = workoutId,
                totalSets = quest.sets,
                setRows = initialRows
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

    fun toggleGuide() {
        _state.value = _state.value.copy(showGuide = !_state.value.showGuide)
    }

    // ── STRENGTH 세트 테이블 관리 ──

    fun updateSetWeight(setIndex: Int, value: String) {
        val s = _state.value
        val rows = s.setRows.toMutableList()
        if (setIndex in rows.indices) {
            rows[setIndex] = rows[setIndex].copy(weight = value)
            _state.value = s.copy(setRows = rows)
        }
    }

    fun updateSetReps(setIndex: Int, value: String) {
        val s = _state.value
        val rows = s.setRows.toMutableList()
        if (setIndex in rows.indices) {
            rows[setIndex] = rows[setIndex].copy(reps = value)
            _state.value = s.copy(setRows = rows)
        }
    }

    fun addSet() {
        val s = _state.value
        val lastRow = s.setRows.lastOrNull()
        val newRow = SetRowData(
            setNumber = s.setRows.size + 1,
            weight = lastRow?.weight ?: "",
            reps = lastRow?.reps ?: ""
        )
        _state.value = s.copy(
            setRows = s.setRows + newRow,
            totalSets = s.totalSets + 1
        )
    }

    fun removeSet() {
        val s = _state.value
        if (s.setRows.size <= 1) return
        val uncompleted = s.setRows.filter { !it.completed }
        if (uncompleted.isEmpty()) return
        // 마지막 미완료 세트 제거
        val lastUncompleted = s.setRows.indexOfLast { !it.completed }
        val rows = s.setRows.toMutableList()
        rows.removeAt(lastUncompleted)
        // 번호 재정렬
        val renumbered = rows.mapIndexed { i, r -> r.copy(setNumber = i + 1) }
        _state.value = s.copy(
            setRows = renumbered,
            totalSets = renumbered.size
        )
    }

    fun completeSetRow(setIndex: Int) {
        val s = _state.value
        val quest = s.quest ?: return
        val rows = s.setRows.toMutableList()
        if (setIndex !in rows.indices || rows[setIndex].completed) return

        val row = rows[setIndex]
        val reps = row.reps.toIntOrNull() ?: quest.repsPerSet
        val weight = row.weight.toDoubleOrNull() ?: 0.0

        rows[setIndex] = row.copy(completed = true)
        val newCompleted = rows.count { it.completed }

        if (!s.isRunning) {
            // 첫 세트 체크 시 타이머 시작
            _state.value = s.copy(setRows = rows, completedSets = newCompleted, isRunning = true)
            startTimer()
            startHeartRateSimulation()
        } else {
            _state.value = s.copy(setRows = rows, completedSets = newCompleted)
        }

        viewModelScope.launch {
            val set = WorkoutSetEntity(
                workoutId = s.workoutId,
                setNumber = row.setNumber,
                reps = reps,
                weight = weight,
                completed = true,
                completedAt = System.currentTimeMillis()
            )
            workoutRepository.insertWorkoutSet(set)

            if (newCompleted >= rows.size) {
                finishWorkout()
            }
        }
    }

    fun completeSet() {
        // ENDURANCE/BALANCE용 기존 로직
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
                    val newStatValue: Int
                    val newStatValueSecond: Int
                    when (quest.statType) {
                        "STRENGTH" -> {
                            newStatValue = user.strengthStat + actualStatReward
                            newStatValueSecond = 0
                        }
                        "ENDURANCE" -> {
                            newStatValue = user.enduranceStat + actualStatReward
                            newStatValueSecond = 0
                        }
                        "BALANCE" -> {
                            val half = actualStatReward / 2
                            val remainder = actualStatReward % 2
                            newStatValue = user.strengthStat + half + remainder
                            newStatValueSecond = user.enduranceStat + half
                        }
                        else -> {
                            newStatValue = 0
                            newStatValueSecond = 0
                        }
                    }

                    // Apply all rewards atomically in a single transaction
                    userRepository.applyWorkoutRewards(
                        userId = user.id,
                        newXp = remainingXp,
                        newLevel = newLevel,
                        statType = quest.statType,
                        newStatValue = newStatValue,
                        newStatValueSecond = newStatValueSecond
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

                    // Push to cloud (실패해도 로컬은 이미 저장됨)
                    var cloudFailed = false
                    if (uid != null) {
                        val sets = workoutRepository.getSetsForWorkoutOnce(s.workoutId)
                        val workoutPushed = syncManager.pushCompletedWorkout(uid, completedWorkout, sets)
                        val updatedUser = userRepository.getUserOnce(uid)
                        val userPushed = if (updatedUser != null) {
                            syncManager.pushUserToCloud(updatedUser)
                        } else false
                        cloudFailed = !workoutPushed || !userPushed
                    }

                    _completeState.value = _completeState.value.copy(syncFailed = cloudFailed)
                }

                _state.value = s.copy(
                    isRunning = false,
                    isCompleted = true,
                    completedSets = quest.sets,
                    caloriesBurned = caloriesBurned
                )
            } catch (e: Exception) {
                AppLogger.e("WorkoutViewModel", "보상 저장 실패", e)
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
