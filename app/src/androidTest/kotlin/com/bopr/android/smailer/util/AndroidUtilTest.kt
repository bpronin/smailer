package com.bopr.android.smailer.util

import android.Manifest.permission.READ_CONTACTS
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
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
    val permissionRule = GrantPermissionRule.grant(READ_CONTACTS)

    @Test
    fun testDeviceName() {
        assertNotNull(DEVICE_NAME)
    }

    @Test
    fun testPrimaryAccount() {
        assertNotNull(targetContext.accounts.getPrimaryGoogleAccount())
    }

    @Test
    fun testGetAccount() {
        assertNull(targetContext.accounts.getGoogleAccount(""))
    }

}