package com.bodyquest.app.data.repository

import com.bodyquest.app.domain.model.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override val isAuthenticated: Boolean
        get() = firebaseAuth.currentUser != null

    override val currentUserId: String?
        get() = firebaseAuth.currentUser?.uid

    override suspend fun signInWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("로그인에 실패했습니다")
            AuthResult.Success(uid = user.uid, email = user.email, isNewUser = false)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("회원가입에 실패했습니다")
            AuthResult.Success(uid = user.uid, email = user.email, isNewUser = true)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google 로그인에 실패했습니다")
            val isNew = result.additionalUserInfo?.isNewUser ?: false
            AuthResult.Success(uid = user.uid, email = user.email, isNewUser = isNew)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override suspend fun sendPasswordResetEmail(email: String): Result<Unit> {
        return try {
            firebaseAuth.sendPasswordResetEmail(email).await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override fun signOut() {
        firebaseAuth.signOut()
    }

    private fun mapFirebaseError(e: Exception): String {
        val msg = e.message ?: ""
        return when {
            msg.contains("INVALID_LOGIN_CREDENTIALS", ignoreCase = true) ||
                msg.contains("WRONG_PASSWORD", ignoreCase = true) ||
                msg.contains("invalid credential", ignoreCase = true) ||
                msg.contains("password is invalid", ignoreCase = true) ->
                "이메일 또는 비밀번호가 올바르지 않습니다"
            msg.contains("EMAIL_EXISTS", ignoreCase = true) ||
                msg.contains("ALREADY_IN_USE", ignoreCase = true) ||
                msg.contains("already in use", ignoreCase = true) ->
                "이미 사용 중인 이메일입니다"
            msg.contains("WEAK_PASSWORD", ignoreCase = true) ||
                msg.contains("at least 6 characters", ignoreCase = true) ->
                "비밀번호는 6자 이상이어야 합니다"
            msg.contains("INVALID_EMAIL", ignoreCase = true) ||
                msg.contains("badly formatted", ignoreCase = true) ->
                "올바른 이메일 형식이 아닙니다"
            msg.contains("NETWORK", ignoreCase = true) ||
                msg.contains("network error", ignoreCase = true) ->
                "네트워크 연결을 확인해주세요"
            msg.contains("USER_NOT_FOUND", ignoreCase = true) ||
                msg.contains("no user record", ignoreCase = true) ->
                "등록되지 않은 이메일입니다"
            msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
                msg.contains("blocked all requests", ignoreCase = true) ->
                "너무 많은 요청이 발생했습니다. 잠시 후 다시 시도해주세요"
            msg.contains("USER_DISABLED", ignoreCase = true) ->
                "비활성화된 계정입니다"
            msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                "이 로그인 방식은 현재 사용할 수 없습니다"
            msg.contains("CREDENTIAL_TOO_OLD", ignoreCase = true) ||
                msg.contains("recent login", ignoreCase = true) ->
                "보안을 위해 다시 로그인해주세요"
            else -> "오류가 발생했습니다. 다시 시도해주세요"
        }
    }
}
