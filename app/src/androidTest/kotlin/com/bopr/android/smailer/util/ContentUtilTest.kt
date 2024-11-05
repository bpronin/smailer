package com.bopr.android.smailer.util

import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * ContentUtil.kt tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class ContentUtilTest : BaseTest() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    /**
    * Before testing add contact for "1 234-567-8901" on testing device.
    * Contact "dummy" should not be present.
     */
    @Test
    fun testContactName() {
        assertNull(targetContext.getContactName("dummy"))
        assertEquals("John Doe", targetContext.getContactName("+1 234-567-8901"))
    }

}