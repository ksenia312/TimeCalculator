package com.xenikii.timecalculator.data.sync

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import com.xenikii.timecalculator.domain.model.AuthSessionState
import com.xenikii.timecalculator.domain.repository.AuthRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class SyncManager(
    private val syncEngine: SyncEngine,
    private val syncTrigger: SyncTrigger,
    private val authRepository: AuthRepository,
) {
    private val synchronizationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var hasStarted = false

    @OptIn(FlowPreview::class)
    fun start() {
        if (hasStarted) return
        hasStarted = true

        authRepository.observeAuthSessionState()
            .onEach { state ->
                if (state == AuthSessionState.LoggedIn) {
                    synchronizationScope.launch { syncEngine.sync() }
                }
            }
            .launchIn(synchronizationScope)

        syncTrigger.observe()
            .debounce(3_000.milliseconds)
            .onEach { syncEngine.sync() }
            .launchIn(synchronizationScope)

        ProcessLifecycleOwner.get().lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStart(owner: LifecycleOwner) {
                synchronizationScope.launch { syncEngine.sync() }
            }
        })
    }
}
