package com.bodyquest.app.ui.home

import android.content.Context
import android.net.Uri
import android.util.Base64
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.QuestEntity
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.remote.SyncManager
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.BossRepository
import com.bodyquest.app.data.repository.QuestRepository
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.data.repository.WorkoutRepository
import com.bodyquest.app.ui.common.UiState
import com.bodyquest.app.util.ImageUtil
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
    val weekWorkoutDays: Set<Int> = emptySet(),
    val showImagePicker: Boolean = false,
    val isUploadingImage: Boolean = false,
    val imageError: String? = null,
    val clearedBossCount: Int = 0,
    val totalBossCount: Int = 0
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val userRepository: UserRepository,
    private val questRepository: QuestRepository,
    private val workoutRepository: WorkoutRepository,
    private val authRepository: AuthRepository,
    private val bossRepository: BossRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<HomeState>>(UiState.Loading)
    val uiState: StateFlow<UiState<HomeState>> = _uiState

    private var loadJob: Job? = null
    private var subJobs = mutableListOf<Job>()

    init {
        loadData()
    }

    fun retry() {
        _uiState.value = UiState.Loading
        loadData()
    }

    private fun loadData() {
        loadJob?.cancel()
        subJobs.forEach { it.cancel() }
        subJobs.clear()

        loadJob = viewModelScope.launch {
            try {
                val uid = authRepository.currentUserId
                if (uid == null) {
                    _uiState.value = UiState.Error("로그인이 필요합니다.")
                    return@launch
                }
                userRepository.getUser(uid).collectLatest { user ->
                    // collectLatest가 새 값 올 때 이전 블록을 취소하므로
                    // 서브 Job들도 함께 정리
                    subJobs.forEach { it.cancel() }
                    subJobs.clear()

                    if (user != null) {
                        val currentData = (_uiState.value as? UiState.Success)?.data ?: HomeState()
                        _uiState.value = UiState.Success(currentData.copy(user = user))
                        loadTodaysQuests(user.id)
                        loadWeekWorkouts(user.id)
                        loadRecommendedQuests(user.job)
                        loadBossProgress(uid)
                    } else {
                        _uiState.value = UiState.Success(HomeState())
                    }
                }
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "데이터를 불러올 수 없습니다.")
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

        val job = viewModelScope.launch {
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
            } catch (e: Exception) {
                Log.w("HomeViewModel", "오늘의 퀘스트 로딩 실패", e)
            }
        }
        subJobs.add(job)
    }

    private fun loadRecommendedQuests(userJob: String) {
        val job = viewModelScope.launch {
            try {
                val allCategories = listOf("STRENGTH", "ENDURANCE")
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
            } catch (e: Exception) {
                Log.w("HomeViewModel", "추천 퀘스트 로딩 실패", e)
            }
        }
        subJobs.add(job)
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

        val job = viewModelScope.launch {
            try {
                workoutRepository.getWeekWorkouts(userId, weekStart).collectLatest { workouts ->
                    val days = workouts.map { workout ->
                        Calendar.getInstance().apply {
                            timeInMillis = workout.startTime
                        }.get(Calendar.DAY_OF_WEEK)
                    }.toSet()
                    updateSuccessState { it.copy(weekWorkoutDays = days) }
                }
            } catch (e: Exception) {
                Log.w("HomeViewModel", "주간 운동 로딩 실패", e)
            }
        }
        subJobs.add(job)
    }

    private fun loadBossProgress(firebaseUid: String) {
        val job = viewModelScope.launch {
            try {
                combine(
                    bossRepository.getClearedBossCount(firebaseUid),
                    bossRepository.getTotalBossCount()
                ) { cleared, total ->
                    cleared to total
                }.collectLatest { (cleared, total) ->
                    updateSuccessState { it.copy(clearedBossCount = cleared, totalBossCount = total) }
                }
            } catch (e: Exception) {
                Log.w("HomeViewModel", "보스 진행률 로딩 실패", e)
            }
        }
        subJobs.add(job)
    }

    fun showImagePicker() {
        updateSuccessState { it.copy(showImagePicker = true) }
    }

    fun dismissImagePicker() {
        updateSuccessState { it.copy(showImagePicker = false) }
    }

    fun uploadProfileImage(uri: Uri) {
        val uid = authRepository.currentUserId
        Log.d("ProfileImage", "uploadProfileImage called, uid=$uid, uri=$uri")
        if (uid == null) return
        viewModelScope.launch {
            try {
                updateSuccessState { it.copy(isUploadingImage = true, imageError = null) }
                val (bytes, base64) = withContext(Dispatchers.IO) {
                    val b = ImageUtil.compressAndResize(appContext, uri)
                    val encoded = Base64.encodeToString(b, Base64.NO_WRAP)
                    b to encoded
                }
                Log.d("ProfileImage", "Compressed: ${bytes.size} bytes, base64: ${base64.length} chars")
                userRepository.updateProfileImageUrl(uid, base64)
                val updatedUser = userRepository.getUserOnce(uid)
                if (updatedUser != null) {
                    syncManager.pushUserToCloud(updatedUser)
                }
                Log.d("ProfileImage", "Profile image saved successfully")
                updateSuccessState { it.copy(isUploadingImage = false) }
            } catch (e: Exception) {
                Log.e("ProfileImage", "Failed to save profile image", e)
                updateSuccessState {
                    it.copy(
                        isUploadingImage = false,
                        imageError = "프로필 사진 변경에 실패했습니다."
                    )
                }
            }
        }
    }

    fun clearImageError() {
        updateSuccessState { it.copy(imageError = null) }
    }

    private fun updateSuccessState(update: (HomeState) -> HomeState) {
        val current = _uiState.value
        if (current is UiState.Success) {
            _uiState.value = UiState.Success(update(current.data))
        }
    }
}
