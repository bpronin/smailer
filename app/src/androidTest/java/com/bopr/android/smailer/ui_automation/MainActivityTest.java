package com.bopr.android.smailer.ui_automation;

import android.annotation.SuppressLint;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.MailMessage;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onData;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isChecked;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.isNotChecked;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_SERVICE_ENABLED;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * {@link MainActivity} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@SuppressLint("CommitPrefEdits")
public class MainActivityTest extends BaseActivityTest {

    @Test
    public void testTitle() {
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
        onView(withSummary(R.string.pref_title_recipients, R.string.pref_description_not_set)).check(matches(isDisplayed()));
    }

    @Test
    public void testEnabledSetting() {
        /* check preferences */
        assertTrue(preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, false));

        onView(withPrefSwitcher(R.string.pref_title_enable)).check(matches(isChecked()));
        onView(withSummary(R.string.pref_title_enable, R.string.pref_description_service_on)).check(matches(isDisplayed()));

        onView(withText(R.string.pref_title_enable)).perform(click());
        onView(withPrefSwitcher(R.string.pref_title_enable)).check(matches(isNotChecked()));
        onView(withSummary(R.string.pref_title_enable, R.string.pref_description_service_off)).check(matches(isDisplayed()));

        /* check preferences */
        assertFalse(preferences.getBoolean(KEY_PREF_SERVICE_ENABLED, true));
    }

    @Test
    public void testTriggersSetting() {
        String[] titles = rule.getActivity().getResources().getStringArray(R.array.email_trigger_titles);

         /* check preferences */
        assertThat(preferences.getStringSet(KEY_PREF_EMAIL_TRIGGERS, null), equalTo(DEFAULT_TRIGGERS));

        /* check all and press cancel */
        onView(withText(R.string.pref_title_trigger)).perform(click());
        onView(withText(R.string.pref_dialog_title_trigger)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[4]))).perform(click());
        onView(withText(android.R.string.cancel)).perform(click());

        assertThat(preferences.getStringSet(KEY_PREF_EMAIL_TRIGGERS, null), allOf(
                containsInAnyOrder(VAL_PREF_TRIGGER_IN_SMS, VAL_PREF_TRIGGER_MISSED_CALLS),
                not(containsInAnyOrder(VAL_PREF_TRIGGER_OUT_SMS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_OUT_CALLS))
        ));


        /* check all and press ok */
        onView(withText(R.string.pref_title_trigger)).perform(click());
        onView(withText(R.string.pref_dialog_title_trigger)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[4]))).perform(click());
        onView(withText(android.R.string.ok)).perform(click());

        assertThat(preferences.getStringSet(KEY_PREF_EMAIL_TRIGGERS, null), allOf(
                containsInAnyOrder(VAL_PREF_TRIGGER_OUT_CALLS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_OUT_SMS),
                not(containsInAnyOrder(VAL_PREF_TRIGGER_IN_SMS, VAL_PREF_TRIGGER_MISSED_CALLS))
        ));
    }

    @Test
    public void testAboutBoxView() {
        String[] openSources = rule.getActivity().getResources().getStringArray(R.array.open_source);

        onMenuButton().perform(click());
        onView(withText(R.string.action_about)).perform(click());
        onDialog(withText(R.string.app_title)).check(matches(isDisplayed()));
        onDialog(withText(R.string.about_dialog_copyright)).check(matches(isDisplayed()));
        onDialog(withText(R.string.about_dialog_title_open_source)).perform(click());
        onView(withText(R.string.open_source_title)).check(matches(isDisplayed()));
        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(openSources.length)));
        onHomeButton().perform(click());
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

    @Test
    public void testLogView() {
        Database database = new Database(rule.getActivity());
        database.clearMessages();
        database.updateMessage(new MailMessage("10", true, 10000L, 20000L, false, true, "SMS text", new GeoCoordinates(10.5, 20.5), true, "Test 10"));

        onMenuButton().perform(click());
        onView(withText(R.string.action_activity_log)).perform(click());
        onView(withText(R.string.activity_log_title)).check(matches(isDisplayed()));

        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(1)));

        /* press clear */
        onToolbar(withId(R.id.action_log_clear)).perform(click());

        /* in dialog click cancel */
        onDialog(withText(R.string.activity_log_ask_clear)).check(matches(isDisplayed()));
        onDialog(withText(android.R.string.cancel)).perform(click());

        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(1)));

        /* press clear again */
        onToolbar(withId(R.id.action_log_clear)).perform(click());

        /* in dialog click clear */
        onDialog(withText(R.string.action_clear)).perform(click());

        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(0)));

        onHomeButton().perform(click());
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

}