package com.wkq.base.widget

import org.junit.Assert.assertEquals
import org.junit.Test

class VerifyCodeInputViewTest {

    @Test
    fun testNormalizeInput_numericOnly() {
        assertEquals("1234", VerifyCodeInputView.Companion.normalizeInput("12a3b4", true))
        assertEquals("99", VerifyCodeInputView.Companion.normalizeInput("9 9", true))
    }

    @Test
    fun testNormalizeInput_alphanumeric() {
        assertEquals("12ab34", VerifyCodeInputView.Companion.normalizeInput("12ab34", false))
        assertEquals("9a9", VerifyCodeInputView.Companion.normalizeInput("9 a 9", false))
    }
}
