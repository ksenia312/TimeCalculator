package com.xenikii.timecalculator.data.auth

import com.xenikii.timecalculator.data.mapper.mapToUser
import com.xenikii.timecalculator.domain.model.AuthError
import com.xenikii.timecalculator.domain.model.AuthException
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.model.User
import com.xenikii.timecalculator.domain.repository.AuthRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.exception.AuthErrorCode
import io.github.jan.supabase.auth.exception.AuthRestException
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException
import kotlin.coroutines.cancellation.CancellationException

class AuthRepositoryImpl(
    private val client: SupabaseClient,
    private val stateMemoryDataSource: AuthSessionStateMemoryDataSource,
    private val clearLocalUserDataManager: ClearLocalUserDataManager,
    private val userPreferences: AuthUserPreferences,
) : AuthRepository {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    init {
        scope.launch {
            client.auth.sessionStatus.collect { status ->
                when (status) {
                    is SessionStatus.Initializing ->
                        stateMemoryDataSource.set(AuthSessionState.Loading)

                    is SessionStatus.RefreshFailure ->
                        stateMemoryDataSource.set(AuthSessionState.Recovering)

                    is SessionStatus.Authenticated -> {
                        reconcileUser()
                        stateMemoryDataSource.set(AuthSessionState.LoggedIn)
                    }

                    is SessionStatus.NotAuthenticated ->
                        stateMemoryDataSource.set(
                            if (status.isSignOut) AuthSessionState.LoggedOut.UserInitiated
                            else AuthSessionState.LoggedOut.SessionExpired,
                        )
                }
            }
        }
    }

    override fun observeAuthSessionState(): Flow<AuthSessionState> = stateMemoryDataSource.observe()

    override suspend fun hasActiveSession(): Boolean =
        client.auth.currentSessionOrNull() != null

    override fun currentUser(): User? = client.auth.currentUserOrNull()?.mapToUser()
    override fun observeCurrentUser(): Flow<User?> {
        return client.auth.sessionStatus.map { it.mapToUser() }
    }

    override suspend fun signIn(email: String, password: String): Result<Unit> =
        runAuth {
            client.auth.signInWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun signUp(email: String, password: String): Result<Unit> =
        runAuth {
            client.auth.signUpWith(Email) {
                this.email = email
                this.password = password
            }
        }

    override suspend fun logout(): Result<Unit> =
        withContext(NonCancellable) {
            val result = runAuth { client.auth.signOut() }
            clearLocalUserDataManager()
            userPreferences.setLastUserId(null)
            result
        }

    /**
     * When a session becomes active, wipe local data if it belongs to a different user than the
     * one previously seen on this device, then remember the current user.
     */
    private suspend fun reconcileUser() {
        val current = currentUser()?.id ?: return
        val last = userPreferences.getLastUserId()
        if (last != null && last != current) {
            clearLocalUserDataManager()
        }
        userPreferences.setLastUserId(current)
    }

    private suspend fun runAuth(block: suspend () -> Unit): Result<Unit> =
        try {
            block()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Result.failure(AuthException(mapAuthError(e)))
        }

    private fun mapAuthError(e: Exception): AuthError = when (e) {
        is AuthRestException -> when (e.errorCode) {
            AuthErrorCode.InvalidCredentials,
            AuthErrorCode.EmailNotConfirmed,
            AuthErrorCode.UserNotFound -> AuthError.InvalidCredentials

            AuthErrorCode.UserAlreadyExists,
            AuthErrorCode.EmailExists -> AuthError.UserAlreadyExists

            AuthErrorCode.WeakPassword -> AuthError.WeakPassword

            AuthErrorCode.OverRequestRateLimit,
            AuthErrorCode.OverEmailSendRateLimit,
            AuthErrorCode.OverSmsSendRateLimit -> AuthError.RateLimited

            else -> AuthError.Unknown(e)
        }

        is IOException -> AuthError.Network

        else -> AuthError.Unknown(e)
    }
}
