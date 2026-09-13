package com.xenikii.timecalculator.app

import android.app.Application
import com.revenuecat.purchases.LogLevel
import com.revenuecat.purchases.Purchases
import com.revenuecat.purchases.PurchasesAreCompletedBy
import com.revenuecat.purchases.PurchasesConfiguration
import com.xenikii.timecalculator.app.di.appModule
import com.xenikii.timecalculator.app.schedule.RoutineExactAlarmPermissionRequester
import com.xenikii.timecalculator.app.schedule.RoutineScheduleInitializer
import com.xenikii.timecalculator.apphost.BuildConfig
import com.xenikii.timecalculator.data.premium.PremiumIdentityCoordinator
import com.xenikii.timecalculator.data.schedule.RefreshRoutineNotificationsUseCase
import com.xenikii.timecalculator.data.sync.SyncManager
import com.xenikii.timecalculator.di.AppModule
import com.xenikii.timecalculator.domain.repository.NotificationSettingsRepository
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import org.koin.core.context.GlobalContext.startKoin

class TimeCalculatorApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        // Configured before Koin so Purchases.sharedInstance is available to every module,
        // matching the SDK's own singleton-initialization requirement.
        Purchases.logLevel = if (BuildConfig.DEBUG) LogLevel.DEBUG else LogLevel.INFO
        Purchases.configure(
            PurchasesConfiguration.Builder(this, BuildConfig.REVENUECAT_API_KEY)
                .purchasesAreCompletedBy(PurchasesAreCompletedBy.REVENUECAT)
                .appUserID(null)
                .diagnosticsEnabled(BuildConfig.DEBUG)
                .build()
        )

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
        koinApplication.koin.get<PremiumIdentityCoordinator>().start()
        RoutineScheduleInitializer(
            context = applicationContext,
            routineRepository = koinApplication.koin.get<RoutineRepository>(),
            scheduleRepository = koinApplication.koin.get<RoutineScheduleRepository>(),
            alarmGateway = koinApplication.koin.get<RoutineAlarmGateway>(),
            permissionRequester = RoutineExactAlarmPermissionRequester(applicationContext),
            notificationSettingsRepository = koinApplication.koin.get<NotificationSettingsRepository>(),
            refreshRoutineNotifications = koinApplication.koin.get<RefreshRoutineNotificationsUseCase>(),
        ).start()
    }
}