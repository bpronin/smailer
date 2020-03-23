package com.bopr.android.smailer.ui


import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.EditText
import androidx.appcompat.widget.AlertDialogLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.bopr.android.smailer.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.TypeSafeMatcher

fun string(id: Int): String {
    return InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}

fun stringArray(id: Int): Array<String> {
    return InstrumentationRegistry.getInstrumentation().targetContext.resources.getStringArray(id)
}

fun ViewInteraction.isExists(): Boolean {
    var exists = true
    withFailureHandler { _, _ ->
        exists = false
    }.check(matches(isDisplayed()))
    return exists
}

fun ViewInteraction.isCheckedCheckbox(): Boolean {
    var checked = true
    withFailureHandler { _, _ ->
        checked = false
    }.check(matches(isChecked()))
    return checked
}

/* matchers */

fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

    return object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return parent is ViewGroup
                    && parentMatcher.matches(parent)
                    && view == parent.getChildAt(position)
        }
    }
}

fun preferenceTitle(title: Int): Matcher<View> {
    return allOf(
            withId(android.R.id.title),
            withText(title),
            isDescendantOfA(instanceOf(RecyclerView::class.java)))
}

fun alertDialog(title: Int): Matcher<View>? {
    return allOf(
            withText(title),
            isDescendantOfA(instanceOf(AlertDialogLayout::class.java)))
}

/* actions */

fun clickPreference(title: Int) {
    onView(preferenceTitle(title)).perform(click())
}

fun clickPreferenceAt(title: Int, position: Int) {
    onView(allOf(
            preferenceTitle(title),
            isDescendantOfA(childAtPosition(allOf(withId(R.id.recycler_view)), position)))
    ).perform(click())
}

fun clickBackButton() {
    onView(allOf(
            withContentDescription("Navigate up"),
            withParent(withId(R.id.action_bar)))
    ).perform(click())
}

fun clickOkButton() {
    onView(withText(android.R.string.ok)).perform(click())
}

fun clickCancelButton() {
    onView(withText(android.R.string.cancel)).perform(click())
}

fun inputText(text: String) {
    onView(instanceOf(EditText::class.java)).perform(replaceText(text))
}

fun clearInputText() {
    onView(instanceOf(EditText::class.java)).perform(clearText())
}

fun setCheckboxChecked(title: String) {
    val onCheckbox = onView(allOf(
            instanceOf(Checkable::class.java),
            withText(title)
    ))
    if (!onCheckbox.isCheckedCheckbox()) {
        onCheckbox.perform(click())
    }
}

fun setCheckboxUnchecked(title: String) {
    val onCheckbox = onView(allOf(
            instanceOf(Checkable::class.java),
            withText(title)
    ))
    if (onCheckbox.isCheckedCheckbox()) {
        onCheckbox.perform(click())
    }
}

/* assertions */

fun assertPageDisplayed(title: Int) {
    onView(allOf(
            withText(title),
            withParent(withId(R.id.action_bar)))
    ).check(matches(isDisplayed()))
}

fun assertAlertDialogDisplayed(title: Int) {
    onView(alertDialog(title)).check(matches(isDisplayed()))
}

fun assertPreferenceSummaryEquals(title: Int, summary: Int) {
    assertPreferenceSummaryEquals(title, string(summary))
}

fun assertPreferenceSummaryEquals(title: Int, summary: String) {
    onView(allOf(
            withId(android.R.id.summary),
            hasSibling(preferenceTitle(title)))
    ).check(matches(withText(summary)))
}

