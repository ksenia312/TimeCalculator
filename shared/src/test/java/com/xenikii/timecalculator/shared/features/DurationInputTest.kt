package com.xenikii.timecalculator.shared.features

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class DurationInputTest {

    @Test
    fun `returns null when both fields are blank`() {
        assertNull(DurationInput().totalMinutesOrNull())
    }

    @Test
    fun `converts hours and minutes to total minutes`() {
        assertEquals(135L, DurationInput(hours = "2", minutes = "15").totalMinutesOrNull())
    }

    @Test
    fun `creates split fields from total minutes`() {
        assertEquals(
            DurationInput(hours = "1", minutes = "30"),
            DurationInput.fromTotalMinutes(90),
        )
    }

    @Test
    fun `keeps zero minutes visible for full hours`() {
        assertEquals(
            DurationInput(hours = "2", minutes = "0"),
            DurationInput.fromTotalMinutes(120),
        )
    }
}
