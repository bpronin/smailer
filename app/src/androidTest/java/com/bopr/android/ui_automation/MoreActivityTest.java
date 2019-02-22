package com.bopr.android.ui_automation;

import android.annotation.SuppressLint;

import com.bopr.android.smailer.R;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isChecked;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.isNotChecked;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_NOTIFY_SEND_SUCCESS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 * {@link com.bopr.android.smailer.ui.MoreActivity} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@SuppressLint("CommitPrefEdits")
public class MoreActivityTest extends BaseActivityTest {

    @Test
    public void testContentSetting() {
        onView(withText(R.string.preferences)).perform(click());

        String[] titles = RULE.getActivity().getResources().getStringArray(R.array.email_content_names);

         /* check settings */
        assertThat(settings.getStringSet(KEY_PREF_EMAIL_CONTENT, null), equalTo(DEFAULT_CONTENT));

        /* check all and press cancel */
        onView(withText(R.string.email_content)).perform(click());
        onView(withText(R.string.email_message_content)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onView(withText(android.R.string.cancel)).perform(click());

        assertThat(settings.getStringSet(KEY_PREF_EMAIL_CONTENT, null), equalTo(DEFAULT_CONTENT));

        /* check all and press ok */
        onView(withText(R.string.email_content)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onView(withText(android.R.string.ok)).perform(click());

        assertThat(settings.getStringSet(KEY_PREF_EMAIL_CONTENT, null), Matchers.<String>empty());
    }

    @Test
    public void testLanguageSetting() {
        onView(withText(R.string.preferences)).perform(click());

        String[] titles = RULE.getActivity().getResources().getStringArray(R.array.language_names);
        String[] values = RULE.getActivity().getResources().getStringArray(R.array.email_locale_values);

         /* check settings */
        assertThat(settings.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(DEFAULT_LOCALE));

        onView(withSummary(R.string.email_message_language, titles[0])).check(matches(isDisplayed()));

        /* press cancel */
        onView(withText(R.string.email_message_language)).perform(click());
        onView(withText(R.string.email_language)).check(matches(isDisplayed()));
        onView(withText(android.R.string.cancel)).perform(click());

        assertThat(settings.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(DEFAULT_LOCALE));

        /* check item 1 */
        onView(withText(R.string.email_message_language)).perform(click());
        onView(withText(R.string.email_language)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());

        onView(withSummary(R.string.email_message_language, titles[1])).check(matches(isDisplayed()));
        assertThat(settings.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(values[1]));

        /* check item 2 */
        onView(withText(R.string.email_message_language)).perform(click());
        onView(withText(R.string.email_language)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());

        onView(withSummary(R.string.email_message_language, titles[2])).check(matches(isDisplayed()));
        assertThat(settings.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(values[2]));
    }

    @Test
    public void testNotifySuccessSetting() {
        onView(withText(R.string.preferences)).perform(click());

        /* check settings */
        assertFalse(settings.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false));

        onView(withPrefSwitcher(R.string.notify_on_send)).check(matches(isNotChecked()));
        onView(withSummary(R.string.notify_send_success, R.string.notify_on_send)).check(matches(isDisplayed()));

        onView(withText(R.string.notify_on_send)).perform(click());
        onView(withPrefSwitcher(R.string.notify_on_send)).check(matches(isChecked()));

        /* check settings */
        assertTrue(settings.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false));
    }

    @Test
    public void testResendSetting() {
        onView(withText(R.string.preferences)).perform(click());

        /* check settings */
        assertTrue(settings.getBoolean(KEY_PREF_RESEND_UNSENT, false));

        onView(withPrefSwitcher(R.string.resend)).check(matches(isChecked()));
        onView(withSummary(R.string.try_to_resend_unsent, R.string.resend)).check(matches(isDisplayed()));

        onView(withText(R.string.resend)).perform(click());
        onView(withPrefSwitcher(R.string.resend)).check(matches(isNotChecked()));

        /* check settings */
        assertFalse(settings.getBoolean(KEY_PREF_RESEND_UNSENT, false));
    }


}