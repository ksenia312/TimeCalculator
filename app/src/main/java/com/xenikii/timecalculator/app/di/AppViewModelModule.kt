package com.xenikii.timecalculator.app.di

import com.xenikii.timecalculator.app.presentation.MainViewModel
import com.xenikii.timecalculator.data.sync.SyncManager
import org.koin.dsl.module

val appModule = module {
    single { MainViewModel(authRepository = get()) }
    single { SyncManager(syncEngine = get(), syncTrigger = get(), authRepository = get()) }
}
