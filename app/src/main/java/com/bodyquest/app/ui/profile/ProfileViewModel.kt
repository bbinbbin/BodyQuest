package com.bodyquest.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.repository.AuthRepository
import com.bodyquest.app.data.repository.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository
) : ViewModel() {

    private val _deleteState = MutableStateFlow<DeleteState>(DeleteState.Idle)
    val deleteState: StateFlow<DeleteState> = _deleteState

    fun signOut() {
        authRepository.signOut()
    }

    fun deleteAccount() {
        val uid = authRepository.currentUserId ?: return
        _deleteState.value = DeleteState.Loading

        viewModelScope.launch {
            val result = authRepository.deleteAccount()
            if (result.isSuccess) {
                userRepository.deleteUserByFirebaseUid(uid)
                _deleteState.value = DeleteState.Success
            } else {
                _deleteState.value = DeleteState.Error(
                    result.exceptionOrNull()?.message ?: "계정 삭제에 실패했습니다"
                )
            }
        }
    }
}

sealed interface DeleteState {
    data object Idle : DeleteState
    data object Loading : DeleteState
    data object Success : DeleteState
    data class Error(val message: String) : DeleteState
}
