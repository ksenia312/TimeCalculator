package com.example.morningcalculator.app

import android.app.Application
import com.example.morningcalculator.di.AppModule
import com.example.morningcalculator.app.bootstrap.RoutineAlarmsBootstrapper
import org.koin.core.context.GlobalContext.startKoin

class MorningCalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val koinApp = startKoin {
            modules(AppModule.registerModules(context = applicationContext))
        }

        koinApp.koin.get<RoutineAlarmsBootstrapper>().start()
    }
}