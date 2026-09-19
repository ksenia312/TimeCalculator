package com.xenikii.timecalculator.data.sync

import org.junit.Assert.assertEquals
import org.junit.Test

class MergedLastTriggeredAtTest {

    @Test
    fun `local null, remote present returns remote`() {
        assertEquals(100L, mergedLastTriggeredAt(local = null, remote = 100L))
    }

    @Test
    fun `local present, remote null returns local`() {
        assertEquals(100L, mergedLastTriggeredAt(local = 100L, remote = null))
    }

    @Test
    fun `both null returns null`() {
        assertEquals(null, mergedLastTriggeredAt(local = null, remote = null))
    }

    @Test
    fun `both present returns the max`() {
        assertEquals(200L, mergedLastTriggeredAt(local = 100L, remote = 200L))
        assertEquals(200L, mergedLastTriggeredAt(local = 200L, remote = 100L))
    }

    @Test
    fun `remote older than local never regresses the fact`() {
        assertEquals(200L, mergedLastTriggeredAt(local = 200L, remote = 50L))
    }
}
