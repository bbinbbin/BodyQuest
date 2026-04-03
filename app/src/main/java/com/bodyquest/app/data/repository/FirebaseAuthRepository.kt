package com.bodyquest.app.data.repository

import android.util.Log
import com.bodyquest.app.domain.model.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthInvalidUserException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import java.io.IOException
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
            val user = result.user ?: return AuthResult.Error("로그인에 실패했습니다.")
            AuthResult.Success(uid = user.uid, email = user.email, isNewUser = false)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override suspend fun signUpWithEmail(email: String, password: String): AuthResult {
        return try {
            val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
            val user = result.user ?: return AuthResult.Error("회원가입에 실패했습니다.")
            AuthResult.Success(uid = user.uid, email = user.email, isNewUser = true)
        } catch (e: Exception) {
            AuthResult.Error(mapFirebaseError(e))
        }
    }

    override suspend fun signInWithGoogle(idToken: String): AuthResult {
        return try {
            val credential = GoogleAuthProvider.getCredential(idToken, null)
            val result = firebaseAuth.signInWithCredential(credential).await()
            val user = result.user ?: return AuthResult.Error("Google 로그인에 실패했습니다.")
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

    override suspend fun deleteAccount(): Result<Unit> {
        return try {
            firebaseAuth.currentUser?.delete()?.await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun mapFirebaseError(e: Exception): String {
        Log.e("FirebaseAuth", "Firebase error [${e.javaClass.simpleName}]: ${e.message}", e)
        return when (e) {
            is FirebaseAuthInvalidUserException -> when (e.errorCode) {
                "ERROR_USER_NOT_FOUND" -> "가입되지 않은 이메일입니다."
                "ERROR_USER_DISABLED" -> "비활성화된 계정입니다. 관리자에게 문의해주세요."
                else -> "존재하지 않는 계정입니다."
            }
            is FirebaseAuthInvalidCredentialsException -> when (e.errorCode) {
                "ERROR_WRONG_PASSWORD" -> "비밀번호가 올바르지 않습니다."
                "ERROR_INVALID_EMAIL" -> "올바른 이메일 형식이 아닙니다."
                else -> "이메일 또는 비밀번호가 올바르지 않습니다."
            }
            is FirebaseAuthWeakPasswordException -> "비밀번호는 6자 이상이어야 합니다."
            is FirebaseAuthUserCollisionException -> "이미 가입된 이메일입니다."
            is IOException -> "네트워크 연결을 확인해주세요."
            else -> {
                val msg = e.message ?: ""
                when {
                    msg.contains("TOO_MANY_REQUESTS", ignoreCase = true) ||
                        msg.contains("blocked all requests", ignoreCase = true) ->
                        "로그인 시도가 너무 많습니다. 잠시 후 다시 시도해주세요."
                    msg.contains("OPERATION_NOT_ALLOWED", ignoreCase = true) ->
                        "이 로그인 방식은 현재 사용할 수 없습니다."
                    msg.contains("CREDENTIAL_TOO_OLD", ignoreCase = true) ||
                        msg.contains("recent login", ignoreCase = true) ->
                        "보안을 위해 다시 로그인해주세요."
                    msg.contains("NETWORK", ignoreCase = true) ->
                        "네트워크 연결을 확인해주세요."
                    else -> "오류가 발생했습니다. 다시 시도해주세요."
                }
            }
        }
    }
}
