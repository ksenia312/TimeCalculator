package com.example.morningcalculator.app

import android.app.Application
import com.example.morningcalculator.data.manager.RoutineWorkManagerScheduler
import com.example.morningcalculator.di.AppModule
import org.koin.core.context.GlobalContext.startKoin

class MorningCalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val koinApp = startKoin {
            modules(AppModule.registerModules(context = applicationContext))
        }

        koinApp.koin.get<RoutineWorkManagerScheduler>().schedulePeriodicCheck()
    }
}