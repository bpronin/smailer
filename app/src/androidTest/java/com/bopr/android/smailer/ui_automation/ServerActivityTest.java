package com.bopr.android.smailer.ui_automation;

import android.annotation.SuppressLint;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.bopr.android.smailer.R;
import com.bopr.android.smailer.Settings;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_ACCOUNT;
import static com.bopr.android.smailer.Settings.KEY_PREF_SENDER_PASSWORD;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertNull;

/**
 * {@link com.bopr.android.smailer.ui.ServerActivity} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@SuppressLint("CommitPrefEdits")
public class ServerActivityTest extends BaseActivityTest {

    @Test
    public void testAccountSetting() {
        onView(withText(R.string.title_email_sender)).perform(click());

        /* check preferences */
        assertNull(preferences.getString(KEY_PREF_SENDER_ACCOUNT, null));

        onView(withSummary(R.string.title_sender_account, R.string.title_not_set)).check(matches(isDisplayed()));

        /* enter address and press cancel */
        onView(withText(R.string.title_sender_account)).perform(click());
        onView(withText(R.string.title_outgoing_email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(typeText("test@mail.com"));
        onView(withText(android.R.string.cancel)).perform(click());

        onView(withSummary(R.string.title_sender_account, "test@mail.com")).check(doesNotExist());

        /* enter address and press ok */
        onView(withText(R.string.title_sender_account)).perform(click());
        onView(withText(R.string.title_outgoing_email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(typeText("test@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        onView(withSummary(R.string.title_sender_account, "test@mail.com")).check(matches(isDisplayed()));

        /* check preferences */
        assertEquals("test@mail.com", preferences.getString(KEY_PREF_SENDER_ACCOUNT, null));
    }

    @Test
    public void testPasswordSetting() {
        onView(withText(R.string.title_email_sender)).perform(click());

        /* check preferences */
        assertNull(preferences.getString(KEY_PREF_SENDER_PASSWORD, null));

        onView(withSummary(R.string.title_password, R.string.title_not_set)).check(matches(isDisplayed()));

        /* enter password and press cancel */
        onView(withText(R.string.title_password)).perform(click());
        onView(withText(R.string.title_password_description)).check(matches(isDisplayed()));
        onView(withEditText()).perform(typeText("password"));
        onView(withText(android.R.string.cancel)).perform(click());

        onView(withSummary(R.string.title_password, R.string.title_password_asterisks)).check(doesNotExist());

        /* enter password and press ok */
        onView(withText(R.string.title_password)).perform(click());
        onView(withText(R.string.title_password_description)).check(matches(isDisplayed()));
        onView(withEditText()).perform(typeText("password"));
        onView(withText(android.R.string.ok)).perform(click());

        onView(withSummary(R.string.title_password, R.string.title_password_asterisks)).check(matches(isDisplayed()));

        /* check preferences */
        assertNotNull(preferences.getString(KEY_PREF_SENDER_PASSWORD, null));
    }

    @Test
    public void testHost() {
        onView(withText(R.string.title_email_sender)).perform(click());

        /* check preferences */
        assertEquals(DEFAULT_HOST, preferences.getString(KEY_PREF_EMAIL_HOST, null));

        onView(withSummary(R.string.title_host, Settings.DEFAULT_HOST)).check(matches(isDisplayed()));

        /* enter host and press cancel */
        onView(withText(R.string.title_host)).perform(click());
        onView(withText(R.string.title_host)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("smtp.mail.com"));
        onView(withText(android.R.string.cancel)).perform(click());

        onView(withSummary(R.string.title_host, "smtp.mail.com")).check(doesNotExist());

        /* enter host and press ok */
        onView(withText(R.string.title_host)).perform(click());
        onView(withText(R.string.title_host)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("smtp.mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        onView(withSummary(R.string.title_host, "smtp.mail.com")).check(matches(isDisplayed()));

        /* check preferences */
        assertEquals("smtp.mail.com", preferences.getString(KEY_PREF_EMAIL_HOST, null));
    }

    @Test
    public void testPort() {
        onView(withText(R.string.title_email_sender)).perform(click());

        /* check preferences */
        assertEquals(DEFAULT_PORT, preferences.getString(KEY_PREF_EMAIL_PORT, null));

        onView(withSummary(R.string.title_port, Settings.DEFAULT_PORT)).check(matches(isDisplayed()));

        /* enter port and press cancel */
        onView(withText(R.string.title_port)).perform(click());
        onView(withText(R.string.title_port)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("123"));
        onView(withText(android.R.string.cancel)).perform(click());

        onView(withSummary(R.string.title_port, "123")).check(doesNotExist());

        /* enter port and press ok */
        onView(withText(R.string.title_port)).perform(click());
        onView(withText(R.string.title_port)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("123"));
        onView(withText(android.R.string.ok)).perform(click());

        onView(withSummary(R.string.title_port, "123")).check(matches(isDisplayed()));

        /* check preferences */
        assertEquals("123", preferences.getString(KEY_PREF_EMAIL_PORT, null));
    }

    @Test
    public void testSummaryText() {
        onView(withSummary(R.string.title_email_sender, R.string.title_not_set)).check(matches(isDisplayed()));

        /* enter page */
        onView(withText(R.string.title_email_sender)).perform(click());

        onView(withText(R.string.title_email_sender)).check(matches(isDisplayed()));

        /* add address */
        onView(withText(R.string.title_sender_account)).perform(click());
        onView(withEditText()).perform(typeText("test@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        /* leave the page */
        onHomeButton().perform(click());
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
        onView(withSummary(R.string.title_email_sender, "test@mail.com")).check(matches(isDisplayed()));
    }

}