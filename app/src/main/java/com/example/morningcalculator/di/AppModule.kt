package com.example.morningcalculator.di

import android.content.Context
import com.example.morningcalculator.core.repository.RoutineRepository
import com.example.morningcalculator.core.repository.TasksRepository
import com.example.morningcalculator.data.repository.RoutineRepositoryImpl
import com.example.morningcalculator.data.repository.TasksRepositoryImpl
import com.example.morningcalculator.features.home.presentation.HomeViewModel
import com.example.morningcalculator.features.routineslist.presentation.RoutinesListViewModel
import com.example.morningcalculator.features.tasks.presentation.TasksListViewModel
import org.koin.core.module.Module
import org.koin.dsl.module

object AppModule {
    fun registerModules(context: Context): Module = module {
        // Repositories
        single<TasksRepository> {
            TasksRepositoryImpl(context)
        }
        single<RoutineRepository> {
            RoutineRepositoryImpl(context)
        }

        // ViewModels
        single {
            HomeViewModel(
                routineRepository = get(),
                tasksRepository = get()
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
    }

}