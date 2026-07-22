package com.example.morningcalculator.domain.repository

import com.example.morningcalculator.domain.model.AuthSessionState
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthSessionState(): Flow<AuthSessionState>
    suspend fun hasActiveSession(): Boolean
    fun currentUserId(): String?
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
