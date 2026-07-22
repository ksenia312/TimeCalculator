package com.example.morningcalculator.app

import android.app.Application
import com.example.morningcalculator.apphost.BuildConfig
import com.example.morningcalculator.app.di.appModule
import com.example.morningcalculator.di.AppModule
import com.example.morningcalculator.app.schedule.RoutineExactAlarmPermissionRequester
import com.example.morningcalculator.app.schedule.RoutineScheduleInitializer
import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import org.koin.core.context.GlobalContext.startKoin

class MorningCalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        val koinApplication = startKoin {
            modules(
                AppModule.registerModules(
                    context = applicationContext,
                    supabaseUrl = BuildConfig.SUPABASE_URL,
                    supabaseKey = BuildConfig.SUPABASE_KEY,
                ),
                appModule,
            )
        }
        RoutineScheduleInitializer(
            routineRepository = koinApplication.koin.get<RoutineRepository>(),
            scheduleRepository = koinApplication.koin.get<RoutineScheduleRepository>(),
            alarmGateway = koinApplication.koin.get<RoutineAlarmGateway>(),
            permissionRequester = RoutineExactAlarmPermissionRequester(applicationContext),
        ).start()
    }
}