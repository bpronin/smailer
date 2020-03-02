package com.bopr.android.smailer.util

import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * AndroidUtil tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AndroidUtilTest : BaseTest() {

//    @Rule
//    @JvmField
//    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(GET_ACCOUNTS)

    @Test
    fun testDeviceName() {
        assertNotNull(deviceName())
    }

    @Test
    fun testPrimaryAccount() {
        assertNotNull(primaryAccount(targetContext))
    }

    @Test
    fun testGetAccount() {
        assertNull(getAccount(targetContext, ""))
    }

}