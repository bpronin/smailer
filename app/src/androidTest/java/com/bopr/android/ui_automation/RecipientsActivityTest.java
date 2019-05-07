package com.bopr.android.ui_automation;

import android.annotation.SuppressLint;

import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.bopr.android.smailer.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.clearText;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeRight;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.assertThat;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static org.hamcrest.Matchers.isEmptyString;
import static org.junit.Assert.assertEquals;

/**
 * {@link com.bopr.android.smailer.ui.RecipientsActivity} tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
@SuppressLint("CommitPrefEdits")
public class RecipientsActivityTest extends BaseActivityTest {

    @Test
    public void testAddEdit() {
         /* check settings */
        assertThat(settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());

        onView(withText(R.string.recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.recipients)).perform(click());
        onView(withText(R.string.recipients)).check(matches(isDisplayed()));

        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item and click cancel */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.cancel)).perform(click());

        assertItemNotExists("john@mail.com");

        /* add an item and click ok */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");

        /* edit an item and click cancel */
        onRecyclerItem(withText("john@mail.com")).perform(click());
        onView(withText(R.string.edit)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.cancel)).perform(click());

        assertItemDisplayed("john@mail.com");
        assertItemNotExists("mary@mail.com");

        /* edit an item and click ok */
        onRecyclerItem(withText("john@mail.com")).perform(click());
        onView(withText(R.string.edit)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("mary@mail.com");
        assertItemNotExists("john@mail.com");

         /* check settings */
        assertEquals("mary@mail.com", settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, null));
    }

    @Test
    public void testAddExistent() {
         /* check settings */
        assertThat(settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());

        onView(withText(R.string.recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.recipients)).perform(click());

        onView(withText(R.string.recipients)).check(matches(isDisplayed()));
        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");

        /* add an the item another time */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        onToast("Recipient john@mail.com already exists").check(matches(isDisplayed()));

         /* check settings */
        assertEquals("john@mail.com", settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, null));
    }

    @Test
    public void testEditExistent() {
         /* check settings */
        onView(withText(R.string.recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.recipients)).perform(click());

        onView(withText(R.string.recipients)).check(matches(isDisplayed()));
        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        /* add another item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");
        assertItemDisplayed("mary@mail.com");

        /* try to change to existent address */
        onRecyclerItem(withText("john@mail.com")).perform(click());
        onView(withText(R.string.edit)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        onToast("Recipient mary@mail.com already exists").check(matches(isDisplayed()));

         /* check settings */
        assertEquals("mary@mail.com,john@mail.com", settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, null));
    }

    @Test
    public void testRemove() {
         /* check settings */
        assertThat(settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());

        onView(withText(R.string.recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.recipients)).perform(click());
        onView(withText(R.string.recipients)).check(matches(isDisplayed()));

        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item and click ok */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.add)).check(matches(isDisplayed()));
        onView(withText(R.string.email_address)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        /* swipe out item */
        onRecyclerItem(withText("john@mail.com")).perform(swipeRight());

        assertItemNotExists("john@mail.com");
        onView(withText(R.string.item_removed)).check(matches(isDisplayed()));

        /* undo */
        onView(withText(R.string.undo)).perform(click());
        assertItemDisplayed("john@mail.com");

        /* swipe out item again */
        onRecyclerItem(withText("john@mail.com")).perform(swipeRight());

        /* swipe out snackbar */
        onView(withText(R.string.item_removed)).perform(swipeRight());

        assertItemNotExists("john@mail.com");

         /* check settings */
        assertThat(settings.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());
    }

    @Test
    public void testSummaryText() {
        onView(withSummary(R.string.recipients, R.string.not_specified)).check(matches(isDisplayed()));

        onView(withText(R.string.recipients)).perform(click());

        /* add an item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");

        onHomeButton().perform(click());

        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
        onView(withSummary(R.string.recipients, "john@mail.com")).check(matches(isDisplayed()));
    }

    private void assertItemDisplayed(String address) {
        onRecyclerItem(withText(address)).check(matches(isDisplayed()));
    }

    private void assertItemNotExists(String address) {
        onRecyclerItem(withText(address)).check(doesNotExist());
    }

}