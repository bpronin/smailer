package com.bopr.android.smailer.util

import org.junit.Test

class JavaMailTest {

    @Test
    fun send() {
        val transport = JavaMail
        transport.startSession("bo.garbage.box@gmail.com", "xxx", "smtp.gmail.com", 465)
        transport.send("boprsoft.dev@gmail.com", "test", "test body", null)
    }

}