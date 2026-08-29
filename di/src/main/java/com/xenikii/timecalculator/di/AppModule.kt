package com.xenikii.timecalculator.di

import android.content.Context
import androidx.room.Room
import com.xenikii.timecalculator.data.auth.AuthRepositoryImpl
import com.xenikii.timecalculator.data.auth.AuthSessionStateMemoryDataSource
import com.xenikii.timecalculator.data.auth.AuthUserPreferences
import com.xenikii.timecalculator.data.auth.ClearLocalUserDataManager
import com.xenikii.timecalculator.data.auth.SupabaseClientProvider
import com.xenikii.timecalculator.data.db.AppDatabase
import com.xenikii.timecalculator.data.db.MIGRATION_1_2
import com.xenikii.timecalculator.data.db.MIGRATION_2_3
import com.xenikii.timecalculator.data.onboarding.OnboardingRepositoryImpl
import com.xenikii.timecalculator.data.onboarding.persistence.PreferencesOnboardingLocalDataSource
import com.xenikii.timecalculator.data.repository.RoutineRepositoryImpl
import com.xenikii.timecalculator.data.repository.TasksRepositoryImpl
import com.xenikii.timecalculator.data.schedule.alarm.AlarmManagerRoutineAlarmGateway
import com.xenikii.timecalculator.data.schedule.notification.RoutineNotificationPresenter
import com.xenikii.timecalculator.data.schedule.persistence.PreferencesScheduleRecordDataSource
import com.xenikii.timecalculator.data.schedule.repository.RoutineScheduleRepositoryImpl
import com.xenikii.timecalculator.data.sync.LogoutUseCase
import com.xenikii.timecalculator.data.sync.SyncCursorStore
import com.xenikii.timecalculator.data.sync.SyncEngine
import com.xenikii.timecalculator.data.sync.SyncTrigger
import com.xenikii.timecalculator.data.sync.remote.SupabaseRemoteDataSource
import com.xenikii.timecalculator.domain.repository.AuthRepository
import com.xenikii.timecalculator.domain.repository.OnboardingLocalDataSource
import com.xenikii.timecalculator.domain.repository.OnboardingRepository
import com.xenikii.timecalculator.domain.repository.RoutineAlarmGateway
import com.xenikii.timecalculator.domain.repository.RoutineNotificationGateway
import com.xenikii.timecalculator.domain.repository.RoutineRepository
import com.xenikii.timecalculator.domain.repository.RoutineScheduleRepository
import com.xenikii.timecalculator.domain.repository.ScheduleRecordDataSource
import com.xenikii.timecalculator.domain.repository.TasksRepository
import com.xenikii.timecalculator.features.auth.presentation.LoginViewModel
import com.xenikii.timecalculator.features.auth.presentation.RegisterViewModel
import com.xenikii.timecalculator.features.auth.presentation.WelcomeViewModel
import com.xenikii.timecalculator.features.home.presentation.HomeViewModel
import com.xenikii.timecalculator.features.landing.presentation.LandingViewModel
import com.xenikii.timecalculator.features.onboarding.presentation.OnboardingViewModel
import com.xenikii.timecalculator.features.routine.presentation.RoutineViewModel
import com.xenikii.timecalculator.features.routineeditor.presentation.CreateRoutineViewModel
import com.xenikii.timecalculator.features.routineeditor.presentation.EditRoutineViewModel
import com.xenikii.timecalculator.features.routineslist.presentation.RoutinesListViewModel
import com.xenikii.timecalculator.features.settings.presentation.SettingsViewModel
import com.xenikii.timecalculator.features.taskeditor.presentation.CreateTaskViewModel
import com.xenikii.timecalculator.features.taskeditor.presentation.EditTaskViewModel
import com.xenikii.timecalculator.features.tasks.presentation.TasksListViewModel
import com.xenikii.timecalculator.shared.navigator.EditTaskArguments
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {
    fun registerModules(
        context: Context,
        supabaseUrl: String,
        supabaseKey: String,
    ): Module = module {
        val appDatabase = Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "morning-db"
        ).addMigrations(MIGRATION_1_2, MIGRATION_2_3).build()

        single { appDatabase.tasksDao() }
        single { appDatabase.routinesDao() }
        single { appDatabase.syncDao() }

        single { SupabaseClientProvider.create(supabaseUrl, supabaseKey) }
        single { AuthSessionStateMemoryDataSource() }
        single { AuthUserPreferences(context) }
        single<OnboardingLocalDataSource> { PreferencesOnboardingLocalDataSource(context) }
        single<OnboardingRepository> { OnboardingRepositoryImpl(localDataSource = get()) }
        single { SyncCursorStore(context) }
        single { SyncTrigger() }
        single { SupabaseRemoteDataSource(get()) }
        single {
            SyncEngine(
                tasksDao = get(),
                routinesDao = get(),
                syncDao = get(),
                supabaseRemoteDataSource = get(),
                syncCursorStore = get(),
                authRepository = get(),
            )
        }
        single { LogoutUseCase(syncEngine = get(), authRepository = get()) }
        single {
            ClearLocalUserDataManager(
                tasksDao = get(),
                routinesDao = get(),
                syncDao = get(),
                cursorStore = get(),
                alarmGateway = get(),
                notificationGateway = get(),
                scheduleRecordDataSource = get(),
            )
        }
        single<AuthRepository> {
            AuthRepositoryImpl(
                client = get(),
                stateMemoryDataSource = get(),
                clearLocalUserDataManager = get(),
                userPreferences = get(),
            )
        }
        factory { LoginViewModel(authRepository = get()) }
        factory { RegisterViewModel(authRepository = get()) }
        factory { WelcomeViewModel(authRepository = get(), onboardingRepository = get()) }
        factory { OnboardingViewModel(onboardingRepository = get()) }

        single<TasksRepository> {
            TasksRepositoryImpl(
                appDatabase = appDatabase,
                tasksDao = get(),
                syncDao = get(),
                syncTrigger = get(),
            )
        }

        single<RoutineRepository> {
            RoutineRepositoryImpl(
                appDatabase = appDatabase,
                routinesDao = get(),
                syncDao = get(),
                syncTrigger = get(),
            )
        }

        single<RoutineAlarmGateway> { AlarmManagerRoutineAlarmGateway(context) }
        single<ScheduleRecordDataSource> { PreferencesScheduleRecordDataSource(context) }
        single<RoutineNotificationGateway> { RoutineNotificationPresenter(context) }
        single<RoutineScheduleRepository> {
            RoutineScheduleRepositoryImpl(
                alarmGateway = get(),
                notificationGateway = get(),
                scheduleRecordDataSource = get(),
            )
        }

        factory { HomeViewModel() }

        factory {
            val logoutUseCase: LogoutUseCase = get()
            SettingsViewModel(
                logoutUseCase = { logoutUseCase() },
                authRepository = get(),
            )
        }

        factory {
            LandingViewModel(
                routineRepository = get(),
                routineScheduleRepository = get(),
            )
        }

        factory {
            RoutinesListViewModel(
                routineRepository = get(),
                routineScheduleRepository = get(),
            )
        }

        factory {
            TasksListViewModel(
                repository = get()
            )
        }

        factory { (id: String) ->
            RoutineViewModel(
                id = id,
                tasksRepository = get(),
                routineRepository = get(),
                routineScheduleRepository = get(),
            )
        }

        factory { (routineId: String?) ->
            CreateTaskViewModel(
                routineId = routineId,
                tasksRepository = get(),
                routineRepository = get(),
            )
        }

        factory { (arguments: EditTaskArguments) ->
            EditTaskViewModel(
                arguments = arguments,
                tasksRepository = get(),
                routineRepository = get(),
            )
        }

        factory {
            CreateRoutineViewModel(
                routineRepository = get(),
            )
        }

        factory { (routineId: String) ->
            EditRoutineViewModel(
                routineId = routineId,
                routineRepository = get(),
            )
        }
    }
}