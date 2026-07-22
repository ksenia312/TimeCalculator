package com.example.morningcalculator.app.di

import com.example.morningcalculator.app.presentation.MainViewModel
import org.koin.dsl.module

val appModule = module {
    single { MainViewModel(authRepository = get()) }
}
