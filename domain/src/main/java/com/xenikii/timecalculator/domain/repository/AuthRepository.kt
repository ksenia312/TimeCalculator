package com.xenikii.timecalculator.domain.repository

import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    fun observeAuthSessionState(): Flow<AuthSessionState>
    suspend fun hasActiveSession(): Boolean
    fun currentUser(): User?
    fun observeCurrentUser(): Flow<User?>
    suspend fun signUp(email: String, password: String): Result<Unit>
    suspend fun signIn(email: String, password: String): Result<Unit>
    suspend fun logout(): Result<Unit>
}
