package com.bopr.android.smailer.util

import org.junit.Test

class EmailValidatorTest {

    @Test
    fun testRunInBackground() {
        val onPerform: () -> String = {
            println("Performing action")
            "ok"
        }

        onPerform()
    }

}