package com.bopr.android.smailer.util

import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.BaseTest
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

/**
 * AndroidUtil tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class AndroidUtilTest : BaseTest() {

    @get:Rule
    /* somehow it gives access to account list in API>=26 */
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(READ_CONTACTS)
//    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(GET_ACCOUNTS)

    @Test
    fun testDeviceName() {
        assertNotNull(DEVICE_NAME)
    }

    @Test
    fun testPrimaryAccount() {
        assertNotNull(AccountHelper(targetContext).getPrimaryGoogleAccount())
    }

    @Test
    fun testGetAccount() {
        assertNull(AccountHelper(targetContext).getGoogleAccount(""))
    }

}