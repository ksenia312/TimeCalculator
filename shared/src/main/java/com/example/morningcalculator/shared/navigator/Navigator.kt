package com.example.morningcalculator.shared.navigator

import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.NavKey

interface Navigator {
    val currentAppRoute: AppRoute?
    val backstack: List<AppRoute>
    val canNavigateBack: Boolean
    fun navigateTo(appRoute: AppRoute, backstackBehavior: BackstackBehavior = BackstackBehavior.Default)
    fun navigateBack()
}

class NavigatorImpl(
    private val navigationBackStack: NavBackStack<NavKey>,
) : Navigator {

    override val currentAppRoute: AppRoute?
        get() = navigationBackStack.lastOrNull() as? AppRoute

    override val backstack: List<AppRoute>
        get() = navigationBackStack.mapNotNull { it as? AppRoute }

    override val canNavigateBack: Boolean
        get() = navigationBackStack.size > 1

    override fun navigateTo(appRoute: AppRoute, backstackBehavior: BackstackBehavior) {
        when (backstackBehavior) {
            BackstackBehavior.Default -> navigationBackStack.add(appRoute)
            BackstackBehavior.Clear -> {
                navigationBackStack.clear()
                navigationBackStack.add(appRoute)
            }

            BackstackBehavior.RemoveCurrent -> {
                navigationBackStack.removeLastOrNull()
                navigationBackStack.add(appRoute)
            }
        }
    }

    override fun navigateBack() {
        if (navigationBackStack.size > 1) {
            navigationBackStack.removeLastOrNull()
        }
    }
}

sealed class BackstackBehavior {
    data object Default : BackstackBehavior()
    data object RemoveCurrent : BackstackBehavior()
    data object Clear : BackstackBehavior()
}
