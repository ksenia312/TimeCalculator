package com.xenikii.timecalculator.data.sync

import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.withTimeout
import kotlin.time.Duration.Companion.milliseconds

class LogoutUseCase(
    private val syncEngine: SyncEngine,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        android.util.Log.d("LOGOUT_DEBUG", "LogoutUseCase: calling syncEngine.sync() @ ${System.currentTimeMillis()}")
        val syncResult = runCatching {
            withTimeout(5_000.milliseconds) {
                syncEngine.sync().getOrThrow()
            }
        }
        // Best-effort: a failed or slow sync must not block the user from logging out
        android.util.Log.d("LOGOUT_DEBUG", "LogoutUseCase: sync step done, isFailure=${syncResult.isFailure} @ ${System.currentTimeMillis()}")

        android.util.Log.d("LOGOUT_DEBUG", "LogoutUseCase: calling authRepository.logout() @ ${System.currentTimeMillis()}")
        val result = authRepository.logout()
        android.util.Log.d("LOGOUT_DEBUG", "LogoutUseCase: authRepository.logout() returned $result @ ${System.currentTimeMillis()}")
        return result
    }
}
