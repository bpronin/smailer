package com.bopr.android.smailer.messenger.mail

import android.Manifest.permission.GET_ACCOUNTS
import android.Manifest.permission.READ_CONTACTS
import androidx.test.filters.SmallTest
import androidx.test.rule.GrantPermissionRule
import androidx.test.rule.GrantPermissionRule.*
import com.bopr.android.smailer.AccountsHelper.Companion.accounts
import com.bopr.android.smailer.BaseTest
import com.google.api.services.gmail.GmailScopes.GMAIL_SEND
import org.junit.Rule
import org.junit.Test

/**
 * [GoogleMailSession] tester.
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
@SmallTest
class GoogleMailSessionTest : BaseTest() {

    @get:Rule
    val permissionRule = grant(
        GET_ACCOUNTS,
        READ_CONTACTS /* https://stackoverflow.com/questions/35050548/android-accountmanager-getaccounts-returns-an-empty-array  */
    )

    @Test
    fun test_List() {
        val account = targetContext.accounts.requirePrimaryGoogleAccount()
        val mail = GoogleMailSession(targetContext, account, GMAIL_SEND)
        mail.list("label:inbox") {
            println(it)
        }

    }
}