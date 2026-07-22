package com.example.morningcalculator.data.auth

import com.example.morningcalculator.domain.model.AuthSessionState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class AuthSessionStateMemoryDataSource {
    private val flow = MutableStateFlow<AuthSessionState>(AuthSessionState.Loading)
    fun observe(): Flow<AuthSessionState> = flow
    fun get(): AuthSessionState = flow.value
    fun set(state: AuthSessionState) {
        flow.value = state
    }
}
