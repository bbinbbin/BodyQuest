package com.bodyquest.app.ui.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.bodyquest.app.data.remote.FirestoreUserService
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
    private val userRepository: UserRepository,
    private val firestoreService: FirestoreUserService
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
            try {
                // 1. Firestore 데이터 삭제 (인증 살아있을 때 해야 권한 있음)
                try {
                    firestoreService.deleteUser(uid)
                } catch (_: Exception) { }

                // 2. Room 로컬 DB 삭제
                userRepository.deleteUserByFirebaseUid(uid)

                // 3. Firebase Auth 계정 삭제 (마지막에 해야 위 작업들이 권한 문제 없음)
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
}

sealed interface DeleteState {
    data object Idle : DeleteState
    data object Loading : DeleteState
    data object Success : DeleteState
    data class Error(val message: String) : DeleteState
}
