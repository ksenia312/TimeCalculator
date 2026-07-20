package com.example.morningcalculator.di

import android.content.Context
import androidx.room.Room
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import com.example.morningcalculator.data.db.AppDatabase
import com.example.morningcalculator.data.repository.RoutineRepositoryImpl
import com.example.morningcalculator.data.repository.TasksRepositoryImpl
import com.example.morningcalculator.features.home.presentation.HomeViewModel
import com.example.morningcalculator.features.landing.presentation.LandingViewModel
import com.example.morningcalculator.features.routine.presentation.RoutineViewModel
import com.example.morningcalculator.features.routineeditor.presentation.CreateRoutineViewModel
import com.example.morningcalculator.features.routineeditor.presentation.EditRoutineViewModel
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListViewModel
import com.example.morningcalculator.features.taskeditor.presentation.CreateTaskViewModel
import com.example.morningcalculator.features.taskeditor.presentation.EditTaskViewModel
import com.example.morningcalculator.features.tasks.presentation.TasksListViewModel
import com.example.morningcalculator.shared.navigator.EditTaskArguments
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {
    fun registerModules(context: Context): Module = module {
        val db = Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "morning-db"
        ).build()

        single { db.tasksDao() }
        single { db.routinesDao() }

        single<TasksRepository> {
            TasksRepositoryImpl(get())
        }

        single<RoutineRepository> {
            RoutineRepositoryImpl(
                dao = get(),
            )
        }

        single {
            HomeViewModel()
        }

        single {
            LandingViewModel(
                routineRepository = get(),
            )
        }

        single {
            RoutinesListViewModel(
                routineRepository = get(),
            )
        }

        single {
            TasksListViewModel(
                repository = get()
            )
        }

        factory { (id: String) ->
            RoutineViewModel(
                id = id,
                tasksRepository = get(),
                routineRepository = get(),
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