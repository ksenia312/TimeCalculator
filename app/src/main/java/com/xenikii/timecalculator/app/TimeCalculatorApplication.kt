package com.xenikii.timecalculator.app

import android.app.Application
import com.xenikii.timecalculator.apphost.BuildConfig
import com.xenikii.timecalculator.app.di.appModule
import com.xenikii.timecalculator.data.sync.SyncManager
import com.xenikii.timecalculator.di.AppModule
import com.xenikii.timecalculator.app.schedule.RoutineExactAlarmPermissionRequester
import com.xenikii.timecalculator.app.schedule.RoutineScheduleInitializer
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import org.koin.core.context.GlobalContext.startKoin

class TimeCalculatorApplication : Application() {
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
        koinApplication.koin.get<SyncManager>().start()
        RoutineScheduleInitializer(
            routineRepository = koinApplication.koin.get<RoutineRepository>(),
            scheduleRepository = koinApplication.koin.get<RoutineScheduleRepository>(),
            alarmGateway = koinApplication.koin.get<RoutineAlarmGateway>(),
            permissionRequester = RoutineExactAlarmPermissionRequester(applicationContext),
        ).start()
    }
}