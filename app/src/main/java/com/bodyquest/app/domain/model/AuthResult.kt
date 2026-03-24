package com.bodyquest.app.domain.model

sealed class AuthResult {
    data class Success(val uid: String, val email: String?, val isNewUser: Boolean) : AuthResult()
    data class Error(val message: String) : AuthResult()
}
