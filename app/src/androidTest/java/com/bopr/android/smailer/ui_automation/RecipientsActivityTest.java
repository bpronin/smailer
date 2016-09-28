package com.bopr.android.smailer.ui_automation;

import android.annotation.SuppressLint;
import android.support.test.filters.LargeTest;
import android.support.test.runner.AndroidJUnit4;

import com.bopr.android.smailer.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.clearText;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.action.ViewActions.typeText;
import static android.support.test.espresso.assertion.ViewAssertions.doesNotExist;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.assertThat;
import static android.support.test.espresso.matcher.ViewMatchers.isDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.KEY_PREF_RECIPIENTS_ADDRESS;
import static junit.framework.Assert.assertEquals;
import static org.hamcrest.Matchers.isEmptyString;

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
         /* check preferences */
        assertThat(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());

        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.pref_title_recipients)).perform(click());
        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));

        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item and click cancel */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.cancel)).perform(click());

        assertItemNotExists("john@mail.com");

        /* add an item and click ok */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");

        /* edit an item and click cancel */
        onRecyclerItem(withText("john@mail.com")).perform(click());
        onView(withText(R.string.pref_dialog_title_edit_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.cancel)).perform(click());

        assertItemDisplayed("john@mail.com");
        assertItemNotExists("mary@mail.com");

        /* edit an item and click ok */
        onRecyclerItem(withText("john@mail.com")).perform(click());
        onView(withText(R.string.pref_dialog_title_edit_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("mary@mail.com");
        assertItemNotExists("john@mail.com");

         /* check preferences */
        assertEquals("mary@mail.com", preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, null));
    }

    @Test
    public void testAddExistent() {
         /* check preferences */
        assertThat(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());

        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.pref_title_recipients)).perform(click());

        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));
        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");

        /* add an the item another time */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        onToast("Recipient john@mail.com already exists").check(matches(isDisplayed()));

         /* check preferences */
        assertEquals("john@mail.com", preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, null));
    }

    @Test
    public void testEditExistent() {
         /* check preferences */
        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.pref_title_recipients)).perform(click());

        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));
        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        /* add another item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");
        assertItemDisplayed("mary@mail.com");

        /* try to change to existent address */
        onRecyclerItem(withText("john@mail.com")).perform(click());
        onView(withText(R.string.pref_dialog_title_edit_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("mary@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        onToast("Recipient mary@mail.com already exists").check(matches(isDisplayed()));

         /* check preferences */
        assertEquals("mary@mail.com, john@mail.com", preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, null));
    }

    @Test
    public void testRemove() {
         /* check preferences */
        assertThat(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());

        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));

        /* click on label */
        onView(withText(R.string.pref_title_recipients)).perform(click());
        onView(withText(R.string.pref_title_recipients)).check(matches(isDisplayed()));

        onView(withId(android.R.id.list)).check(matches(emptyList()));

        /* add an item and click ok */
        onView(withId(R.id.button_add)).perform(click());
        onView(withText(R.string.pref_dialog_title_add_recipient)).check(matches(isDisplayed()));
        onView(withText(R.string.pref_dialog_message_recipient)).check(matches(isDisplayed()));
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        /* swipe out item */
        onRecyclerItem(withText("john@mail.com")).perform(swipeRight());

        assertItemNotExists("john@mail.com");
        onView(withText(R.string.message_item_removed)).check(matches(isDisplayed()));

        /* undo */
        onView(withText(R.string.action_undo)).perform(click());
        assertItemDisplayed("john@mail.com");

        /* swipe out item again */
        onRecyclerItem(withText("john@mail.com")).perform(swipeRight());

        /* swipe out snackbar */
        onView(withText(R.string.message_item_removed)).perform(swipeRight());

        assertItemNotExists("john@mail.com");

         /* check preferences */
        assertThat(preferences.getString(KEY_PREF_RECIPIENTS_ADDRESS, ""), isEmptyString());
    }

    @Test
    public void testSummaryText() {
        onView(withSummary(R.string.pref_title_recipients, R.string.pref_description_not_set)).check(matches(isDisplayed()));

        onView(withText(R.string.pref_title_recipients)).perform(click());

        /* add an item */
        onView(withId(R.id.button_add)).perform(click());
        onView(withEditText()).perform(clearText());
        onView(withEditText()).perform(typeText("john@mail.com"));
        onView(withText(android.R.string.ok)).perform(click());

        assertItemDisplayed("john@mail.com");

        onHomeButton().perform(click());

        onView(withText(R.string.app_name)).check(matches(isDisplayed()));
        onView(withSummary(R.string.pref_title_recipients, "john@mail.com")).check(matches(isDisplayed()));
    }

    private void assertItemDisplayed(String address) {
        onRecyclerItem(withText(address)).check(matches(isDisplayed()));
    }

    private void assertItemNotExists(String address) {
        onRecyclerItem(withText(address)).check(doesNotExist());
    }

}