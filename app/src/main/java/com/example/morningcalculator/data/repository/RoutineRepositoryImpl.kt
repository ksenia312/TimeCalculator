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

class RoutineRepositoryImpl(
    context: Context, private val prefs: SharedPreferences = context.getSharedPreferences(
        "tasks", Context.MODE_PRIVATE
    )
) : RoutineRepository {

    companion object {
        private const val KEY_ROUTINE = "routine_json"
    }

    var id: String? = null

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
        this.id = id
        refresh()
    }

    override fun clearId() {
        id = null
        refresh()
    }

    override fun routineFlow(): StateFlow<Routine?> = _routineFlow.asStateFlow()

    override fun routinesFlow(): StateFlow<List<Routine>> = _routinesFlow.asStateFlow()

    override fun addRoutine(request: RoutineRequest) {
        val routine = Routine(title = request.title, entries = emptyList(), time = request.time)
        addOrChangeRoutine(routine)
    }

    override fun updateRoutine(routine: Routine) {
        addOrChangeRoutine(routine)
    }

    private fun addOrChangeRoutine(routine: Routine) {
        val routines = _routinesFlow.value
        var updatedRoutines = routines.map { if (it.id == routine.id) routine else it }.toList()
        if (!updatedRoutines.map { it.id }.contains(id)) {
            updatedRoutines = updatedRoutines + (routine)
        }
        prefs.edit() {
            putString(KEY_ROUTINE, Json.encodeToString(updatedRoutines))
        }
        _routinesFlow.value = updatedRoutines
        _routineFlow.value = routine

        refresh()
    }

    private fun loadRoutinesFromPrefs(): List<Routine> {
        val json = prefs.getString(KEY_ROUTINE, "[]") ?: "[]"
        return runCatching { Json.decodeFromString<List<Routine>>(json) }.getOrDefault(
            emptyList()
        )
    }

    private fun loadRoutineFromPrefs(): Routine? {
        return loadRoutinesFromPrefs().firstOrNull { it.id == id }
    }

    private fun refresh() {
        _routineFlow.value = loadRoutineFromPrefs()
        _routinesFlow.value = loadRoutinesFromPrefs()
    }
}