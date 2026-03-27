package com.bodyquest.app.data.repository

import com.bodyquest.app.domain.model.AuthResult

interface AuthRepository {
    val isAuthenticated: Boolean
    val currentUserId: String?
    suspend fun signInWithEmail(email: String, password: String): AuthResult
    suspend fun signUpWithEmail(email: String, password: String): AuthResult
    suspend fun signInWithGoogle(idToken: String): AuthResult
    suspend fun sendPasswordResetEmail(email: String): Result<Unit>
    fun signOut()
    suspend fun deleteAccount(): Result<Unit>
}
