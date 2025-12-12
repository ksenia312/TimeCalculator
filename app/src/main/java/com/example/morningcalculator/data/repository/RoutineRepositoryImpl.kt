package com.example.morningcalculator.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import com.example.morningcalculator.core.model.Routine
import com.example.morningcalculator.core.model.RoutineRequest
import com.example.morningcalculator.core.repository.RoutineRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.json.Json
import java.util.UUID

class RoutineRepositoryImpl(
    context: Context, private val prefs: SharedPreferences = context.getSharedPreferences(
        "tasks", Context.MODE_PRIVATE
    )
) : RoutineRepository {

    companion object {
        private const val KEY_ROUTINE = "routine_json"
    }

    var selectedRoutineId: String? = null

    private val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == KEY_ROUTINE) {
            refresh()
        }
    }

    private val _routineFlow = MutableStateFlow(loadRoutineFromPrefs())
    private val _routinesFlow = MutableStateFlow(loadRoutinesFromPrefs())

    init {
        prefs.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun initializeId(id: String) {
        this.selectedRoutineId = id
        refresh()
    }

    override fun clearId() {
        selectedRoutineId = null
        refresh()
    }

    override val routineFlow: StateFlow<Routine.Links?> = _routineFlow.asStateFlow()

    override val routinesFlow: StateFlow<List<Routine.Links>> = _routinesFlow.asStateFlow()

    override fun addRoutine(request: RoutineRequest) {
        val routine = Routine.Links(
            id = UUID.randomUUID().toString(),
            title = request.title,
            links = emptyList(),
            time = request.time,
            color = request.color,
            modifiedAt = System.currentTimeMillis(),
        )
        addOrChangeRoutine(routine)
    }

    override fun updateRoutine(routine: Routine.Links) {
        addOrChangeRoutine(routine)
    }

    private fun addOrChangeRoutine(initialRoutine: Routine.Links) {
        val newRoutine = initialRoutine.copy(modifiedAt = System.currentTimeMillis())
        val routines = _routinesFlow.value
        var updatedRoutines =
            routines.map { r -> if (r.id == newRoutine.id) newRoutine else r }.toList()

        val ids = updatedRoutines.map { it.id }
        if (!ids.contains(newRoutine.id)) {
            updatedRoutines = updatedRoutines + (newRoutine)
        }
        prefs.edit() {
            putString(KEY_ROUTINE, Json.encodeToString(updatedRoutines))
        }
//        _routinesFlow.value = updatedRoutines
//        _routineFlow.value = routine

        refresh()
    }

    private fun loadRoutinesFromPrefs(): List<Routine.Links> {
        val json = prefs.getString(KEY_ROUTINE, "[]") ?: "[]"
        return runCatching { Json.decodeFromString<List<Routine.Links>>(json) }.getOrDefault(
            emptyList()
        )
    }

    private fun loadRoutineFromPrefs(): Routine.Links? {
        return loadRoutinesFromPrefs().firstOrNull { it.id == selectedRoutineId }
    }

    private fun refresh() {
        _routineFlow.value = loadRoutineFromPrefs()
        _routinesFlow.value = loadRoutinesFromPrefs()
    }
}