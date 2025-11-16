package com.example.morningcalculator.core.repository

import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import kotlinx.coroutines.flow.StateFlow

interface RoutineRepository {
    fun initializeId(id: String)

    fun clearId()

    fun routineFlow(): StateFlow<Routine.Links?>

    fun routinesFlow(): StateFlow<List<Routine.Links>>

    fun addRoutine(request: RoutineRequest)

    fun updateRoutine(routine: Routine.Links)
}

class PreviewRoutineRepository : RoutineRepository {
    override fun initializeId(id: String) {
        throw NotImplementedError("PreviewRoutineRepository does not implement initializeId")
    }

    override fun clearId() {
        throw NotImplementedError("PreviewRoutineRepository does not implement clearId")
    }

    override fun routineFlow(): StateFlow<Routine.Links?> {
        throw NotImplementedError("PreviewRoutineRepository does not implement routineFlow")
    }

    override fun routinesFlow(): StateFlow<List<Routine.Links>> {
        throw NotImplementedError("PreviewRoutineRepository does not implement routinesFlow")
    }

    override fun addRoutine(request: RoutineRequest) {
        throw NotImplementedError("PreviewRoutineRepository does not implement addRoutine")
    }

    override fun updateRoutine(routine: Routine.Links) {
        throw NotImplementedError("PreviewRoutineRepository does not implement updateRoutine")
    }
}