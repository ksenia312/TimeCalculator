package com.example.morningcalculator

import android.app.Application
import com.example.morningcalculator.di.AppModule
import org.koin.core.context.GlobalContext.startKoin

class MorningCalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        startKoin {
            modules(AppModule.registerModules(context = applicationContext))
        }
    }
}