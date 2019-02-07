package com.bopr.android.ui_automation;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;
import android.support.test.espresso.ViewInteraction;
import android.support.test.rule.ActivityTestRule;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Switch;

import com.bopr.android.smailer.ui.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;

import java.util.Locale;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.matcher.RootMatchers.withDecorView;
import static android.support.test.espresso.matcher.ViewMatchers.hasSibling;
import static android.support.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static android.support.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static android.support.test.espresso.matcher.ViewMatchers.withChild;
import static android.support.test.espresso.matcher.ViewMatchers.withContentDescription;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_HOST;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.DEFAULT_PORT;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_HOST;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_PORT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static com.bopr.android.smailer.Settings.getPreferences;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.not;

/**
 * Base UI tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BaseActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> rule = new ActivityTestRule<>(MainActivity.class);

    protected SharedPreferences preferences;

    @BeforeClass
    public static void setUpClass() {
        Locale.setDefault(Locale.US);
    }

    @Before
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public void setUp() {
        preferences = getPreferences(rule.getContext());
        preferences
                .edit()
                .clear()
                .putString(KEY_PREF_EMAIL_HOST, DEFAULT_HOST)
                .putString(KEY_PREF_EMAIL_PORT, DEFAULT_PORT)
                .putStringSet(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
                .putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                .putString(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                .putBoolean(KEY_PREF_RESEND_UNSENT, true)
                .apply();
    }

    @NonNull
    protected ViewInteraction onDialog(Matcher<View> matcher) {
        return onView(matcher).inRoot(withDecorView(not(is(rule.getContext().getWindow().getDecorView()))));
    }

    @NonNull
    protected ViewInteraction onToast(String text) {
        return onDialog(withText(text));
    }

//    @NonNull
//    protected ViewInteraction onSnackBar(Matcher<View> matcher) {
//        //return onView(allOf(withId(android.support.design.R.id.snackbar_text), matcher));
//        return onView(withId(android.support.design.R.id.snackbar_text))
//                .inRoot(withDecorView(not(is(rule.getContext().getWindow().getDecorView()))));
//    }

    @NonNull
    protected ViewInteraction onToolbar(Matcher<View> matcher) {
        return onView(allOf(isDescendantOfA(Matchers.<View>instanceOf(Toolbar.class)), matcher));
    }

    @NonNull
    protected ViewInteraction onHomeButton() {
        return onToolbar(withContentDescription("Navigate up"));
    }

    @NonNull
    protected ViewInteraction onMenuButton() {
        return onToolbar(withContentDescription("More options"));
    }

    @SuppressWarnings("unchecked")
    public static ViewInteraction onRecyclerItem(Matcher<View> itemMatcher) {
        Matcher<View> anyItem = isDescendantOfA(isAssignableFrom(RecyclerView.class));
        return onView(allOf(anyItem, itemMatcher));
    }

/*
    protected <T> void assertListItemDisplayed(Class<?> itemClass, Matcher<T> matcher) {
        onData(allOf(is(instanceOf(itemClass)), matcher)).check(matches(isDisplayed()));
    }
*/

/*
    protected <T> void assertListItemNotExists(int listId, Matcher<T> matcher) {
        onView(withId(listId)).check(matches(not(withList(matcher))));
    }
*/

/*
    @NonNull
    protected <T> Matcher<View> withList(final Matcher<T> dataMatcher) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (!(view instanceof AdapterView)) {
                    return false;
                }

                Adapter adapter = ((AdapterView) view).getAdapter();
                for (int i = 0; i < adapter.getCount(); i++) {
                    if (dataMatcher.matches(adapter.getItem(i))) {
                        return true;
                    }
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("with class name: ");
                dataMatcher.describeTo(description);
            }
        };
    }
*/

    @NonNull
    protected Matcher<View> emptyList() {
        return hasListItemsCount(0);
    }

    @NonNull
    protected Matcher<View> hasListItemsCount(final int count) {
        return new TypeSafeMatcher<View>() {

            @Override
            public boolean matchesSafely(View view) {
                if (view instanceof AdapterView) {
                    return (((AdapterView) view).getAdapter().getCount() == count);
                } else if (view instanceof RecyclerView) {
                    return (((RecyclerView) view).getAdapter().getItemCount() == count);
                }
                return false;
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("count = " + count);
            }
        };
    }

    @NonNull
    protected Matcher<View> withSummary(int title, String summary) {
        return allOf(withText(summary), hasSibling(withText(title)));
    }

    @NonNull
    protected Matcher<View> withSummary(int title, int summary) {
        return allOf(withText(summary), hasSibling(withText(title)));
    }

    @NonNull
    protected Matcher<View> withPrefSwitcher(int title) {
        return allOf(instanceOf(Switch.class), isDescendantOfA(withChild(withChild(withText(title)))));
    }

    @NonNull
    protected Matcher<View> withEditText() {
        return instanceOf(EditText.class);
    }

}