package com.wkq.base.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class MultiSpanTextViewTest {

    @Test
    fun testBuildSpanRanges_singleMatch() {
        val text = "Please read our Privacy Policy carefully."
        val items = listOf(
            MultiSpanTextView.SpanItem(keyword = "Privacy Policy")
        )
        
        val ranges = MultiSpanTextView.Companion.buildSpanRanges(text, items)
        assertEquals(1, ranges.size)
        assertEquals(16, ranges[0].start)
        assertEquals(30, ranges[0].end)
        assertEquals("Privacy Policy", ranges[0].item.keyword)
    }

    @Test
    fun testBuildSpanRanges_multipleMatches() {
        val text = "Please read our Privacy Policy and User Agreement carefully."
        val items = listOf(
            MultiSpanTextView.SpanItem(keyword = "Privacy Policy"),
            MultiSpanTextView.SpanItem(keyword = "User Agreement")
        )
        
        val ranges = MultiSpanTextView.Companion.buildSpanRanges(text, items)
        assertEquals(2, ranges.size)
        val sortedRanges = ranges.sortedBy { it.start }
        assertEquals(16, sortedRanges[0].start)
        assertEquals(30, sortedRanges[0].end)
        assertEquals("Privacy Policy", sortedRanges[0].item.keyword)
        
        assertEquals(35, sortedRanges[1].start)
        assertEquals(49, sortedRanges[1].end)
        assertEquals("User Agreement", sortedRanges[1].item.keyword)
    }

    @Test
    fun testBuildSpanRanges_overlappingRanges() {
        val text = "We have a SuperAgreement for you."
        val items = listOf(
            MultiSpanTextView.SpanItem(keyword = "SuperAgreement"),
            MultiSpanTextView.SpanItem(keyword = "Agreement")
        )
        
        val ranges = MultiSpanTextView.Companion.buildSpanRanges(text, items)
        assertEquals(1, ranges.size)
        assertEquals(10, ranges[0].start)
        assertEquals(24, ranges[0].end)
        assertEquals("SuperAgreement", ranges[0].item.keyword)
    }
}
