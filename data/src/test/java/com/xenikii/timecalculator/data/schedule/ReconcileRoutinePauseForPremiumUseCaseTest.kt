package com.xenikii.timecalculator.data.schedule

import com.xenikii.timecalculator.domain.model.PremiumEntitlementState
import com.xenikii.timecalculator.domain.model.Routine
import com.xenikii.timecalculator.domain.model.RoutineLink
import com.xenikii.timecalculator.domain.model.RoutinePauseState
import com.xenikii.timecalculator.domain.model.RoutineScheduleAnchor
import com.xenikii.timecalculator.domain.model.SubData
import com.xenikii.timecalculator.domain.model.Task
import org.junit.Assert.assertEquals
import org.junit.Test
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Instant

class ReconcileRoutinePauseForPremiumUseCaseTest {

    private val useCase = ReconcileRoutinePauseForPremiumUseCase()

    @Test
    fun `expired premium with 4 active routines pauses the least recently triggered one`() {
        val routines = (1..4).map { routine(id = "r$it", lastTriggeredAt = it.toLong(), state = RoutinePauseState.ACTIVE) }

        val result = useCase(routines, PremiumEntitlementState.EXPIRED)

        assertEquals(RoutinePauseState.PAUSED_AUTO, result.single { it.id == "r1" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r2" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r3" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r4" }.state)
    }

    @Test
    fun `routines that have never fired are paused before ones that have`() {
        val routines = listOf(
            routine(id = "r1", lastTriggeredAt = null, state = RoutinePauseState.ACTIVE),
            routine(id = "r2", lastTriggeredAt = 100L, state = RoutinePauseState.ACTIVE),
            routine(id = "r3", lastTriggeredAt = 50L, state = RoutinePauseState.ACTIVE),
            routine(id = "r4", lastTriggeredAt = 1L, state = RoutinePauseState.ACTIVE),
        )

        val result = useCase(routines, PremiumEntitlementState.EXPIRED)

        // r1 never fired at all - lowest priority, paused even though r4 fired longer ago.
        assertEquals(RoutinePauseState.PAUSED_AUTO, result.single { it.id == "r1" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r2" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r3" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r4" }.state)
    }

    @Test
    fun `tie-break between equal lastTriggeredAt values is deterministic by id`() {
        val routines = listOf(
            routine(id = "a", lastTriggeredAt = 100L, state = RoutinePauseState.ACTIVE),
            routine(id = "b", lastTriggeredAt = 100L, state = RoutinePauseState.ACTIVE),
            routine(id = "c", lastTriggeredAt = 100L, state = RoutinePauseState.ACTIVE),
            routine(id = "d", lastTriggeredAt = 100L, state = RoutinePauseState.ACTIVE),
        )

        val result = useCase(routines, PremiumEntitlementState.EXPIRED)

        // All four tie on lastTriggeredAt, so id ascending decides: a, b, c stay active, d pauses.
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "a" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "b" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "c" }.state)
        assertEquals(RoutinePauseState.PAUSED_AUTO, result.single { it.id == "d" }.state)
    }

    @Test
    fun `tie-break between multiple never-fired routines is deterministic by id`() {
        val routines = listOf(
            routine(id = "a", lastTriggeredAt = null, state = RoutinePauseState.ACTIVE),
            routine(id = "b", lastTriggeredAt = null, state = RoutinePauseState.ACTIVE),
            routine(id = "c", lastTriggeredAt = null, state = RoutinePauseState.ACTIVE),
            routine(id = "d", lastTriggeredAt = null, state = RoutinePauseState.ACTIVE),
        )

        val result = useCase(routines, PremiumEntitlementState.EXPIRED)

        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "a" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "b" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "c" }.state)
        assertEquals(RoutinePauseState.PAUSED_AUTO, result.single { it.id == "d" }.state)
    }

    @Test
    fun `expired premium never touches manually paused routines or counts them against the limit`() {
        val routines = listOf(
            routine(id = "r1", lastTriggeredAt = 1L, state = RoutinePauseState.PAUSED_MANUAL),
            routine(id = "r2", lastTriggeredAt = 2L, state = RoutinePauseState.ACTIVE),
            routine(id = "r3", lastTriggeredAt = 3L, state = RoutinePauseState.ACTIVE),
            routine(id = "r4", lastTriggeredAt = 4L, state = RoutinePauseState.ACTIVE),
        )

        val result = useCase(routines, PremiumEntitlementState.EXPIRED)

        assertEquals(RoutinePauseState.PAUSED_MANUAL, result.single { it.id == "r1" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r2" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r3" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r4" }.state)
    }

    @Test
    fun `expired premium with 3 or fewer active routines is a no-op`() {
        val routines = (1..3).map { routine(id = "r$it", lastTriggeredAt = it.toLong(), state = RoutinePauseState.ACTIVE) }

        val result = useCase(routines, PremiumEntitlementState.EXPIRED)

        assertEquals(routines, result)
    }

    @Test
    fun `active premium wakes only auto-paused routines`() {
        val routines = listOf(
            routine(id = "r1", lastTriggeredAt = 1L, state = RoutinePauseState.PAUSED_AUTO),
            routine(id = "r2", lastTriggeredAt = 2L, state = RoutinePauseState.PAUSED_MANUAL),
            routine(id = "r3", lastTriggeredAt = 3L, state = RoutinePauseState.ACTIVE),
        )

        val result = useCase(routines, PremiumEntitlementState.ACTIVE)

        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r1" }.state)
        assertEquals(RoutinePauseState.PAUSED_MANUAL, result.single { it.id == "r2" }.state)
        assertEquals(RoutinePauseState.ACTIVE, result.single { it.id == "r3" }.state)
    }

    @Test
    fun `unknown entitlement never changes anything`() {
        val routines = listOf(
            routine(id = "r1", lastTriggeredAt = 1L, state = RoutinePauseState.PAUSED_AUTO),
            routine(id = "r2", lastTriggeredAt = 2L, state = RoutinePauseState.ACTIVE),
        )

        val result = useCase(routines, PremiumEntitlementState.UNKNOWN)

        assertEquals(routines, result)
    }

    @Test
    fun `is idempotent once the target state is reached`() {
        val routines = (1..5).map { routine(id = "r$it", lastTriggeredAt = it.toLong(), state = RoutinePauseState.ACTIVE) }

        val once = useCase(routines, PremiumEntitlementState.EXPIRED)
        val twice = useCase(once, PremiumEntitlementState.EXPIRED)

        assertEquals(once, twice)
    }

    private fun routine(id: String, lastTriggeredAt: Long?, state: RoutinePauseState): Routine {
        val task = Task(
            id = "task-$id",
            title = "Task",
            description = "",
            data = listOf(SubData(id = "sub-$id", duration = 5.minutes)),
        )
        return Routine(
            id = id,
            title = "Routine $id",
            scheduledAt = Instant.fromEpochMilliseconds(0),
            scheduledAtAnchor = RoutineScheduleAnchor.START,
            modifiedAt = 0L,
            color = "#000000",
            data = listOf(RoutineLink(id = "link-$id", task = task, subData = task.data.first())),
            state = state,
            lastTriggeredAt = lastTriggeredAt,
        )
    }
}
