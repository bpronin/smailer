package com.bopr.android.smailer

import org.junit.Test
import org.junit.Assert.*

class BuildInfoTest : BaseTest() {
    
    @Test
    fun test_get() {
        val info = BuildInfo.get(targetContext)
        assertNotNull(info)
        assertTrue(info.number.isNotBlank())
        assertTrue(info.time.isNotBlank())
    }

}