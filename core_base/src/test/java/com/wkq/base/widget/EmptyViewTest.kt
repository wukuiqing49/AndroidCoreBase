package com.wkq.base.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class EmptyViewTest {

    @Test
    fun testCalculateInSampleSize() {
        assertEquals(2, EmptyView.Companion.calculateInSampleSize(200, 200, 100, 100))
        assertEquals(4, EmptyView.Companion.calculateInSampleSize(400, 400, 100, 100))
        assertEquals(1, EmptyView.Companion.calculateInSampleSize(80, 80, 100, 100))
    }

    @Test
    fun testResolveTargetSize() {
        assertEquals(100, EmptyView.Companion.resolveTargetSize(100, 160))
        assertEquals(160, EmptyView.Companion.resolveTargetSize(null, 160))
        assertEquals(160, EmptyView.Companion.resolveTargetSize(0, 160))
        assertEquals(160, EmptyView.Companion.resolveTargetSize(-10, 160))
    }
}
