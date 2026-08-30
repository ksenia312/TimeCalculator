package com.xenikii.timecalculator.domain.repository

import kotlinx.coroutines.flow.StateFlow

interface SyncStateProvider {
    val isSyncing: StateFlow<Boolean>
}
