package com.bodyquest.app.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.local.entity.UserEntity
import com.bodyquest.app.data.repository.UserRepository
import com.bodyquest.app.domain.model.Goal
import com.bodyquest.app.domain.model.Job
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class OnboardingState(
    val currentStep: Int = 0,
    val selectedJob: Job? = null,
    val selectedGoal: Goal? = null,
    val nickname: String = "",
    val avatarIndex: Int = 0,
    val strengthStat: Int = 0,
    val enduranceStat: Int = 0,
    val balanceStat: Int = 0,
    val isCompleted: Boolean = false,
    val isSaving: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow(OnboardingState())
    val state: StateFlow<OnboardingState> = _state

    fun selectJob(job: Job) {
        _state.value = _state.value.copy(selectedJob = job)
    }

    fun selectGoal(goal: Goal) {
        _state.value = _state.value.copy(selectedGoal = goal)
    }

    fun setNickname(nickname: String) {
        _state.value = _state.value.copy(nickname = nickname)
    }

    fun setAvatarIndex(index: Int) {
        _state.value = _state.value.copy(avatarIndex = index)
    }

    fun setStrengthStat(value: Int) {
        _state.value = _state.value.copy(strengthStat = value)
    }

    fun setEnduranceStat(value: Int) {
        _state.value = _state.value.copy(enduranceStat = value)
    }

    fun setBalanceStat(value: Int) {
        _state.value = _state.value.copy(balanceStat = value)
    }

    fun nextStep() {
        _state.value = _state.value.copy(currentStep = _state.value.currentStep + 1)
    }

    fun previousStep() {
        if (_state.value.currentStep > 0) {
            _state.value = _state.value.copy(currentStep = _state.value.currentStep - 1)
        }
    }

    fun completeOnboarding() {
        val s = _state.value
        if (s.selectedJob == null || s.selectedGoal == null || s.nickname.isBlank()) return

        _state.value = s.copy(isSaving = true, error = null)

        viewModelScope.launch {
            try {
                userRepository.createUser(
                    UserEntity(
                        nickname = s.nickname,
                        job = s.selectedJob.name,
                        goal = s.selectedGoal.name,
                        avatarIndex = s.avatarIndex,
                        strengthStat = s.strengthStat,
                        enduranceStat = s.enduranceStat,
                        balanceStat = s.balanceStat
                    )
                )
                _state.value = _state.value.copy(isCompleted = true, isSaving = false)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isSaving = false,
                    error = e.message ?: "프로필 생성에 실패했습니다"
                )
            }
        }
    }

    fun clearError() {
        _state.value = _state.value.copy(error = null)
    }
}
