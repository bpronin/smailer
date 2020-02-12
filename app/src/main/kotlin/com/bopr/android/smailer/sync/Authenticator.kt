package com.bopr.android.smailer.sync

import android.accounts.AbstractAccountAuthenticator
import android.accounts.Account
import android.accounts.AccountAuthenticatorResponse
import android.accounts.NetworkErrorException
import android.content.Context
import android.os.Bundle

/**
 * Authenticator stub.
 *
 *
 * Required by synchronization framework.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class Authenticator(context: Context) : AbstractAccountAuthenticator(context) {

    override fun editProperties(response: AccountAuthenticatorResponse, accountType: String): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun addAccount(response: AccountAuthenticatorResponse, accountType: String,
                            authTokenType: String, requiredFeatures: Array<String>,
                            options: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun confirmCredentials(response: AccountAuthenticatorResponse, account: Account,
                                    options: Bundle): Bundle? {
        return null
    }

    @Throws(NetworkErrorException::class)
    override fun getAuthToken(response: AccountAuthenticatorResponse, account: Account,
                              authTokenType: String, options: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    override fun getAuthTokenLabel(authTokenType: String): String {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun updateCredentials(response: AccountAuthenticatorResponse, account: Account,
                                   authTokenType: String, options: Bundle): Bundle {
        throw UnsupportedOperationException()
    }

    @Throws(NetworkErrorException::class)
    override fun hasFeatures(response: AccountAuthenticatorResponse, account: Account,
                             features: Array<String>): Bundle {
        throw UnsupportedOperationException()
    }
}