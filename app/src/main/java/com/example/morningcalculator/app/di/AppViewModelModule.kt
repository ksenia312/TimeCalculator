package com.example.morningcalculator.app.di

import com.example.morningcalculator.app.presentation.MainViewModel
import com.example.morningcalculator.data.sync.SyncManager
import org.koin.dsl.module

val appModule = module {
    single { MainViewModel(authRepository = get()) }
    single { SyncManager(syncEngine = get(), syncTrigger = get(), authRepository = get()) }
}
