package com.bopr.android.smailer.ui_automation;

import android.annotation.SuppressLint;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.bopr.android.smailer.R;

import org.hamcrest.Matchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
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
        onView(withText(R.string.pref_title_more)).perform(click());

        String[] titles = rule.getActivity().getResources().getStringArray(R.array.email_content_titles);

         /* check preferences */
        assertThat(preferences.getStringSet(KEY_PREF_EMAIL_CONTENT, null), equalTo(DEFAULT_CONTENT));

        /* check all and press cancel */
        onView(withText(R.string.pref_title_content)).perform(click());
        onView(withText(R.string.pref_dialog_title_email_content)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onView(withText(android.R.string.cancel)).perform(click());

        assertThat(preferences.getStringSet(KEY_PREF_EMAIL_CONTENT, null), equalTo(DEFAULT_CONTENT));

        /* check all and press ok */
        onView(withText(R.string.pref_title_content)).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onView(withText(android.R.string.ok)).perform(click());

        assertThat(preferences.getStringSet(KEY_PREF_EMAIL_CONTENT, null), Matchers.<String>empty());
    }

    @Test
    public void testLanguageSetting() {
        onView(withText(R.string.pref_title_more)).perform(click());

        String[] titles = rule.getActivity().getResources().getStringArray(R.array.email_locale_titles);
        String[] values = rule.getActivity().getResources().getStringArray(R.array.email_locale_values);

         /* check preferences */
        assertThat(preferences.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(DEFAULT_LOCALE));

        onView(withSummary(R.string.pref_title_email_locale, titles[0])).check(matches(isDisplayed()));

        /* press cancel */
        onView(withText(R.string.pref_title_email_locale)).perform(click());
        onView(withText(R.string.pref_dialog_title_email_locale)).check(matches(isDisplayed()));
        onView(withText(android.R.string.cancel)).perform(click());

        assertThat(preferences.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(DEFAULT_LOCALE));

        /* check item 1 */
        onView(withText(R.string.pref_title_email_locale)).perform(click());
        onView(withText(R.string.pref_dialog_title_email_locale)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());

        onView(withSummary(R.string.pref_title_email_locale, titles[1])).check(matches(isDisplayed()));
        assertThat(preferences.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(values[1]));

        /* check item 2 */
        onView(withText(R.string.pref_title_email_locale)).perform(click());
        onView(withText(R.string.pref_dialog_title_email_locale)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());

        onView(withSummary(R.string.pref_title_email_locale, titles[2])).check(matches(isDisplayed()));
        assertThat(preferences.getString(KEY_PREF_EMAIL_LOCALE, null), equalTo(values[2]));
    }

    @Test
    public void testNotifySuccessSetting() {
        onView(withText(R.string.pref_title_more)).perform(click());

        /* check preferences */
        assertFalse(preferences.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false));

        onView(withPrefSwitcher(R.string.pref_title_notify_send_success)).check(matches(isNotChecked()));
        onView(withSummary(R.string.pref_description_notify_send_success, R.string.pref_title_notify_send_success)).check(matches(isDisplayed()));

        onView(withText(R.string.pref_title_notify_send_success)).perform(click());
        onView(withPrefSwitcher(R.string.pref_title_notify_send_success)).check(matches(isChecked()));

        /* check preferences */
        assertTrue(preferences.getBoolean(KEY_PREF_NOTIFY_SEND_SUCCESS, false));
    }

    @Test
    public void testResendSetting() {
        onView(withText(R.string.pref_title_more)).perform(click());

        /* check preferences */
        assertTrue(preferences.getBoolean(KEY_PREF_RESEND_UNSENT, false));

        onView(withPrefSwitcher(R.string.pref_title_resend_unsent)).check(matches(isChecked()));
        onView(withSummary(R.string.pref_description_resend_unsent, R.string.pref_title_resend_unsent)).check(matches(isDisplayed()));

        onView(withText(R.string.pref_title_resend_unsent)).perform(click());
        onView(withPrefSwitcher(R.string.pref_title_resend_unsent)).check(matches(isNotChecked()));

        /* check preferences */
        assertFalse(preferences.getBoolean(KEY_PREF_RESEND_UNSENT, false));
    }


}