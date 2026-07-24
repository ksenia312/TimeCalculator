package com.xenikii.timecalculator.shared.animation

import androidx.compose.animation.AnimatedContentScope
import androidx.compose.animation.ExperimentalSharedTransitionApi
import androidx.compose.animation.SharedTransitionScope
import androidx.compose.runtime.compositionLocalOf

/**
 * Provides the [SharedTransitionScope] created around the whole navigation display so that shared
 * elements can be matched across destinations. Defaults to `null` (e.g. in previews) so callers
 * can safely skip the shared-element behaviour when it is not available.
 */
@OptIn(ExperimentalSharedTransitionApi::class)
val LocalSharedTransitionScope = compositionLocalOf<SharedTransitionScope?> { null }

/**
 * Bridges the per-destination [AnimatedContentScope] exposed by navigation3 into a nullable local
 * so shared elements work inside a destination while previews (with no scope) keep rendering.
 */
val LocalCardAnimatedContentScope = compositionLocalOf<AnimatedContentScope?> { null }

/** Builds the shared-element key used to morph a routine card between screens. */
fun routineCardSharedKey(routineId: String): String = "routine-card-$routineId"
