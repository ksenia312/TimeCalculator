package com.xenikii.timecalculator.shared.navigator

import androidx.navigation3.runtime.NavKey

interface DeepLinkKey : NavKey {
    val parent: NavKey
}

fun NavKey.syntheticBackStack(): List<NavKey> {
    val result = mutableListOf<NavKey>()
    var current: NavKey = this
    while (true) {
        result += current
        current = (current as? DeepLinkKey)?.parent ?: break
    }
    return result.asReversed()
}
