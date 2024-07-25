package com.bopr.android.smailer.ui

import android.os.Bundle
import android.text.TextUtils
import androidx.preference.ExtMultiSelectListPreference
import androidx.preference.Preference
import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_BOT_TOKEN
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_CHAT_ID
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSAGE_CONTENT
import com.bopr.android.smailer.Settings.Companion.PREF_TELEGRAM_MESSENGER_ENABLED
import com.bopr.android.smailer.processor.telegram.BaseTelegramEventFormatter
import com.bopr.android.smailer.processor.telegram.TelegramSession
import com.bopr.android.smailer.util.GeoLocation
import com.bopr.android.smailer.util.GeoLocation.Companion.requestGeoLocation
import com.bopr.android.smailer.util.PreferenceProgress
import com.bopr.android.smailer.util.SummaryStyle.SUMMARY_STYLE_ACCENTED
import com.bopr.android.smailer.util.onOffText
import com.bopr.android.smailer.util.requirePreference
import com.bopr.android.smailer.util.requirePreferenceAs
import com.bopr.android.smailer.util.setOnChangeListener
import com.bopr.android.smailer.util.setOnClickListener
import com.bopr.android.smailer.util.showToast
import com.bopr.android.smailer.util.telegramErrorText
import com.bopr.android.smailer.util.titles
import com.bopr.android.smailer.util.updateSummary

/**
 * Event consumers settings fragment.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class TelegramSettingsFragment : BasePreferenceFragment(R.xml.pref_telegram_settings) {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requirePreference(PREF_TELEGRAM_MESSENGER_ENABLED).setOnChangeListener {
            it.apply {
                setTitle(onOffText(settings.getBoolean(it.key)))
            }
        }

        requirePreferenceAs<ExtMultiSelectListPreference>(PREF_TELEGRAM_MESSAGE_CONTENT).apply {
            setOnChangeListener {
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                it.apply {
                    updateSummary(titles().joinToString(", "))
                }
            }
        }

        requirePreference(PREF_TELEGRAM_BOT_TOKEN).setOnChangeListener {
            it.apply {
                val token = settings.getString(it.key)
                if (token.isNullOrEmpty()) {
                    updateSummary(R.string.unspecified, SUMMARY_STYLE_ACCENTED)
                } else {
                    updateSummary(R.string.specified)
                }
            }
        }

        requirePreference(SEND_TEST_TELEGRAM_MESSAGE).setOnClickListener {
            onSendTestTelegramMessage(it)
        }
    }

    private fun onSendTestTelegramMessage(preference: Preference) {
        requireContext().requestGeoLocation { location ->
            val formater = TestTelegramEventFormatter(System.currentTimeMillis(), location)
            val progress = PreferenceProgress(preference).apply { start() }
            TelegramSession(
                context = requireContext(),
                token = settings.getString(PREF_TELEGRAM_BOT_TOKEN)
            ).sendMessage(
                oldChatId = settings.getString(PREF_TELEGRAM_CHAT_ID),
                message = formater.formatMessage(),
                onSuccess = { chatId ->
                    progress.stop()
                    settings.update { putString(PREF_TELEGRAM_CHAT_ID, chatId) }
                    showToast(R.string.test_message_sent)
                },
                onError = { error ->
                    progress.stop()
                    showInfoDialog(R.string.test_message_failed, telegramErrorText(error))
                }
            )
        }
    }

    private inner class TestTelegramEventFormatter(time: Long, location: GeoLocation?) :
        BaseTelegramEventFormatter(requireContext(), time, time, location) {

        override fun getTitle(): String {
            return getString(R.string.test_message)
        }

        override fun getMessage(): String {
            return getString(R.string.this_is_test_message)
        }

        override fun getSenderName(): String {
            return getString(R.string.sender_of, getString(R.string.app_name))
        }

    }

    companion object {

        private const val SEND_TEST_TELEGRAM_MESSAGE = "send_test_telegram_message"
    }
}