package com.bopr.android.ui_automation;

import android.annotation.SuppressLint;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.bopr.android.smailer.Database;
import com.bopr.android.smailer.GeoCoordinates;
import com.bopr.android.smailer.PhoneEvent;
import com.bopr.android.smailer.R;
import com.bopr.android.smailer.ui.MainActivity;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onData;
import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_IN_SMS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_MISSED_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_CALLS;
import static com.bopr.android.smailer.Settings.VAL_PREF_TRIGGER_OUT_SMS;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * {@link MainActivity} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@SuppressLint("CommitPrefEdits")
public class MainActivityTest extends BaseActivityTest {

    @Override
    public void setUp() {
        super.setUp();
    }

    @Test
    public void testTitle() {
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
        onView(withSummary(R.string.recipients, R.string.not_specified)).check(matches(isDisplayed()));
    }

    @Test
    public void testTriggersSetting() {
        String[] titles = RULE.getActivity().getResources().getStringArray(R.array.trigger_names);

         /* check settings */
        assertThat(settings.getStringSet(PREF_EMAIL_TRIGGERS, null), equalTo(DEFAULT_TRIGGERS));

        /* check all and press cancel */
        onView(withText(R.string.triggers)).perform(click());
        onView(withText(R.string.triggers)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[4]))).perform(click());
        onView(withText(android.R.string.cancel)).perform(click());

        assertThat(settings.getStringSet(PREF_EMAIL_TRIGGERS, null), allOf(
                containsInAnyOrder(VAL_PREF_TRIGGER_IN_SMS, VAL_PREF_TRIGGER_MISSED_CALLS),
                not(containsInAnyOrder(VAL_PREF_TRIGGER_OUT_SMS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_OUT_CALLS))
        ));


        /* check all and press ok */
        onView(withText(R.string.triggers)).perform(click());
        onView(withText(R.string.triggers)).check(matches(isDisplayed()));
        onData(allOf(is(instanceOf(String.class)), is(titles[0]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[1]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[2]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[3]))).perform(click());
        onData(allOf(is(instanceOf(String.class)), is(titles[4]))).perform(click());
        onView(withText(android.R.string.ok)).perform(click());

        assertThat(settings.getStringSet(PREF_EMAIL_TRIGGERS, null), allOf(
                containsInAnyOrder(VAL_PREF_TRIGGER_OUT_CALLS, VAL_PREF_TRIGGER_IN_CALLS, VAL_PREF_TRIGGER_OUT_SMS),
                not(containsInAnyOrder(VAL_PREF_TRIGGER_IN_SMS, VAL_PREF_TRIGGER_MISSED_CALLS))
        ));
    }

    @Test
    public void testAboutBoxView() {
        String[] openSources = RULE.getActivity().getResources().getStringArray(R.array.open_source);

        onMenuButton().perform(click());
        onView(withText(R.string.about)).perform(click());
        onDialog(withText(R.string.app_title)).check(matches(isDisplayed()));
        onDialog(withText(R.string.copyright)).check(matches(isDisplayed()));
        onDialog(withText(R.string.open_source_and_third_party)).perform(click());
        onView(withText(R.string.open_source)).check(matches(isDisplayed()));
        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(openSources.length)));
        onHomeButton().perform(click());
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

    @Test
    public void testLogView() {
        Database database = new Database(RULE.getActivity());
        database.clearEvents();
        database.putEvent(new PhoneEvent("10", true, 10000L, 20000L, false, "SMS text",
                new GeoCoordinates(10.5, 20.5), "Test 10", PhoneEvent.STATE_PENDING, null));

        onMenuButton().perform(click());
        onView(withText(R.string.call_history)).perform(click());
        onView(withText(R.string.call_history)).check(matches(isDisplayed()));

        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(1)));

        /* press clear */
        onToolbar(withId(R.id.action_log_clear)).perform(click());

        /* in dialog click cancel */
        onDialog(withText(R.string.ask_clear_history)).check(matches(isDisplayed()));
        onDialog(withText(android.R.string.cancel)).perform(click());

        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(1)));

        /* press clear again */
        onToolbar(withId(R.id.action_log_clear)).perform(click());

        /* in dialog click clear */
        onDialog(withText(R.string.clear)).perform(click());

        onView(withId(android.R.id.list)).check(matches(hasListItemsCount(0)));

        onHomeButton().perform(click());
        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
    }

}