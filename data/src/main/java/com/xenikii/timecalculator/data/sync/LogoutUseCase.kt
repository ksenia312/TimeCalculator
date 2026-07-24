package com.xenikii.timecalculator.data.sync

import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.withTimeout

class LogoutUseCase(
    private val syncEngine: SyncEngine,
    private val authRepository: AuthRepository,
) {
    suspend operator fun invoke(): Result<Unit> {
        val syncResult = runCatching {
            withTimeout(5_000) {
                syncEngine.sync().getOrThrow()
            }
        }
        if (syncResult.isFailure) return Result.failure(syncResult.exceptionOrNull()!!)
        return authRepository.logout()
    }
}
