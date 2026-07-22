package com.example.morningcalculator.di

import android.content.Context
import androidx.room.Room
import com.example.morningcalculator.domain.repository.RoutineRepository
import com.example.morningcalculator.domain.repository.TasksRepository
import com.example.morningcalculator.domain.repository.AuthRepository
import com.example.morningcalculator.domain.repository.RoutineAlarmGateway
import com.example.morningcalculator.domain.repository.RoutineNotificationGateway
import com.example.morningcalculator.domain.repository.RoutineScheduleRepository
import com.example.morningcalculator.domain.repository.ScheduleRecordDataSource
import com.example.morningcalculator.data.auth.ClearLocalUserDataManager
import com.example.morningcalculator.data.db.AppDatabase
import com.example.morningcalculator.data.auth.AuthRepositoryImpl
import com.example.morningcalculator.data.auth.AuthSessionStateMemoryDataSource
import com.example.morningcalculator.data.auth.AuthUserPreferences
import com.example.morningcalculator.data.auth.SupabaseClientProvider
import com.example.morningcalculator.data.repository.RoutineRepositoryImpl
import com.example.morningcalculator.data.repository.TasksRepositoryImpl
import com.example.morningcalculator.data.schedule.alarm.AlarmManagerRoutineAlarmGateway
import com.example.morningcalculator.data.schedule.notification.RoutineNotificationPresenter
import com.example.morningcalculator.data.schedule.persistence.PreferencesScheduleRecordDataSource
import com.example.morningcalculator.data.schedule.repository.RoutineScheduleRepositoryImpl
import com.example.morningcalculator.features.home.presentation.HomeViewModel
import com.example.morningcalculator.features.landing.presentation.LandingViewModel
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.features.routineeditor.presentation.CreateRoutineViewModel
import com.example.morningcalculator.features.routineeditor.presentation.EditRoutineViewModel
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListViewModel
import com.example.morningcalculator.features.taskeditor.presentation.CreateTaskViewModel
import com.example.morningcalculator.features.taskeditor.presentation.EditTaskViewModel
import com.example.morningcalculator.features.tasks.presentation.TasksListViewModel
import com.example.morningcalculator.features.auth.presentation.LoginViewModel
import com.example.morningcalculator.features.auth.presentation.RegisterViewModel
import com.example.morningcalculator.features.auth.presentation.WelcomeViewModel
import com.example.morningcalculator.shared.navigator.EditTaskArguments
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {
    fun registerModules(
        context: Context,
        supabaseUrl: String,
        supabaseKey: String,
    ): Module = module {
        val db = Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "morning-db"
        ).build()

        single { db.tasksDao() }
        single { db.routinesDao() }

        single { SupabaseClientProvider.create(supabaseUrl, supabaseKey) }
        single { AuthSessionStateMemoryDataSource() }
        single { AuthUserPreferences(context) }
        single {
            ClearLocalUserDataManager(
                tasksRepository = get(),
                routineRepository = get(),
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
        factory { WelcomeViewModel(authRepository = get()) }

        single<TasksRepository> {
            TasksRepositoryImpl(get())
        }

        single<RoutineRepository> {
            RoutineRepositoryImpl(
                dao = get(),
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

        factory {
            HomeViewModel(authRepository = get())
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