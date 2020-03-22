package com.bopr.android.smailer.ui


import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.widget.AlertDialogLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import com.bopr.android.smailer.R
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.instanceOf
import org.hamcrest.TypeSafeMatcher

fun ViewInteraction.isExists(): Boolean {
    var exists = true
    withFailureHandler { _, _ ->
        exists = false
    }.check(matches(isDisplayed()))
    return exists
}

fun childAtPosition(parentMatcher: Matcher<View>, position: Int): Matcher<View> {

    return object : TypeSafeMatcher<View>() {

        override fun describeTo(description: Description) {
            description.appendText("Child at position $position in parent ")
            parentMatcher.describeTo(description)
        }

        public override fun matchesSafely(view: View): Boolean {
            val parent = view.parent
            return parent is ViewGroup && parentMatcher.matches(parent)
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

fun clickPreference(title: Int) {
    onView(preferenceTitle(title)).perform(click())
}

fun clickPreferenceAtPosition(position: Int) {
    onView(allOf(
            childAtPosition(allOf(withId(R.id.recycler_view)), position))
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

fun assertPageDisplayed(title: Int) {
    onView(allOf(
            withText(title),
            withParent(withId(R.id.action_bar)))
    ).check(matches(isDisplayed()))
}

fun assertAlertDialogDisplayed(title: Int) {
    onView(alertDialog(title)).check(matches(isDisplayed()))
}

fun assertPreferenceSummaryEquals(title: Int, summary: String) {
    onView(allOf(
            withId(android.R.id.summary),
            hasSibling(preferenceTitle(title)))
    ).check(matches(withText(summary)))
}

