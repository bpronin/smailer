package com.bopr.android.smailer.messenger.mail

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_CONTACTS
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import com.bopr.android.smailer.AccountHelper
import com.bopr.android.smailer.BaseTest
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.junit.Rule
import org.junit.Test

/**
 * [GoogleMailSession] tester.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
@SmallTest
class GoogleMailSessionTest : BaseTest() {

    @get:Rule
    val permissionRule: GrantPermissionRule = GrantPermissionRule.grant(
        GET_ACCOUNTS,
        READ_CONTACTS /* https://stackoverflow.com/questions/35050548/android-accountmanager-getaccounts-returns-an-empty-array  */
    )

    @Test
    fun test_List() {
        val account = AccountHelper(targetContext).requirePrimaryGoogleAccount()
        val mail = GoogleMailSession(targetContext, account, GMAIL_SEND)
        mail.list("label:inbox"){
            println(it)
        }

    }
}