package com.example.morningcalculator.shared.navigator

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * Holds a deep-link destination that must be opened only once the user is logged in.
 * [MainActivity] publishes here; the navigation gate consumes it after [AppRoute] auth passes.
 */
object PendingDeepLink {
    val route = MutableStateFlow<AppRoute?>(null)
}
