package com.bopr.android.ui_automation;

import android.view.View;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.Switch;

import com.bopr.android.smailer.Settings;
import com.bopr.android.smailer.ui.MainActivity;

import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.hamcrest.Matchers;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;

import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.espresso.ViewInteraction;
import androidx.test.rule.ActivityTestRule;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.matcher.RootMatchers.withDecorView;
import static androidx.test.espresso.matcher.ViewMatchers.hasSibling;
import static androidx.test.espresso.matcher.ViewMatchers.isAssignableFrom;
import static androidx.test.espresso.matcher.ViewMatchers.isDescendantOfA;
import static androidx.test.espresso.matcher.ViewMatchers.withChild;
import static androidx.test.espresso.matcher.ViewMatchers.withContentDescription;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static com.bopr.android.smailer.Settings.DEFAULT_CONTENT;
import static com.bopr.android.smailer.Settings.DEFAULT_LOCALE;
import static com.bopr.android.smailer.Settings.DEFAULT_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_CONTENT;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_LOCALE;
import static com.bopr.android.smailer.Settings.KEY_PREF_EMAIL_TRIGGERS;
import static com.bopr.android.smailer.Settings.KEY_PREF_RESEND_UNSENT;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.instanceOf;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;

/**
 * Base UI tester.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
public class BaseActivityTest {

    @ClassRule
    public static final ActivityTestRule<MainActivity> RULE = new ActivityTestRule<>(MainActivity.class);

    Settings settings;

    @BeforeClass
    public static void setUpClass() {
        Locale.setDefault(Locale.US);
    }

    @Before
    public void setUp() {
        settings = new Settings(RULE.getActivity());
        settings
                .edit()
                .clear()
                .putStringSet(KEY_PREF_EMAIL_TRIGGERS, DEFAULT_TRIGGERS)
                .putStringSet(KEY_PREF_EMAIL_CONTENT, DEFAULT_CONTENT)
                .putString(KEY_PREF_EMAIL_LOCALE, DEFAULT_LOCALE)
                .putBoolean(KEY_PREF_RESEND_UNSENT, true)
                .apply();
    }

    @NonNull
    static ViewInteraction onDialog(Matcher<View> matcher) {
        return onView(matcher).inRoot(withDecorView(not(is(RULE.getActivity().getWindow().getDecorView()))));
    }

    @NonNull
    static ViewInteraction onToast(String text) {
        return onDialog(withText(text));
    }

    @NonNull
    static ViewInteraction onToolbar(Matcher<View> matcher) {
        return onView(allOf(isDescendantOfA(Matchers.<View>instanceOf(Toolbar.class)), matcher));
    }

    @NonNull
    static ViewInteraction onHomeButton() {
        return onToolbar(withContentDescription("Navigate up"));
    }

    @NonNull
    static ViewInteraction onMenuButton() {
        return onToolbar(withContentDescription("More options"));
    }

    @SuppressWarnings("unchecked")
    static ViewInteraction onRecyclerItem(Matcher<View> itemMatcher) {
        Matcher<View> anyItem = isDescendantOfA(isAssignableFrom(RecyclerView.class));
        return onView(allOf(anyItem, itemMatcher));
    }

    @NonNull
    static Matcher<View> emptyList() {
        return hasListItemsCount(0);
    }

    @NonNull
    static Matcher<View> hasListItemsCount(final int count) {
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
    static Matcher<View> withSummary(int title, String summary) {
        return allOf(withText(summary), hasSibling(withText(title)));
    }

    @NonNull
    static Matcher<View> withSummary(int title, int summary) {
        return allOf(withText(summary), hasSibling(withText(title)));
    }

    @NonNull
    static Matcher<View> withPrefSwitcher(int title) {
        return allOf(instanceOf(Switch.class), isDescendantOfA(withChild(withChild(withText(title)))));
    }

    @NonNull
    static Matcher<View> withEditText() {
        return instanceOf(EditText.class);
    }

}