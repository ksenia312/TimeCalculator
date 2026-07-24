package com.xenikii.timecalculator.data.sync

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

class SyncTrigger {
    private val trigger = MutableSharedFlow<Unit>(extraBufferCapacity = 1)

    fun emit() {
        trigger.tryEmit(Unit)
    }

    fun observe(): SharedFlow<Unit> = trigger
}
