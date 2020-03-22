package com.bopr.android.smailer.ui


import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AlertDialogLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.ViewInteraction
import androidx.test.espresso.action.ViewActions.click
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

fun clickPreference(title: Int) {
    onView(allOf(
            withText(title),
            isDescendantOfA(instanceOf(RecyclerView::class.java)),
            isDisplayed())
    ).perform(click())
}

fun clickPreferenceAtPosition(position: Int) {
    onView(allOf(
            childAtPosition(allOf(withId(R.id.recycler_view)), position),
            isDisplayed())
    ).perform(click())
}

fun clickBackButton() {
    onView(allOf(
            withContentDescription("Navigate up"),
            withParent(withId(R.id.action_bar)),
            isDisplayed())
    ).perform(click())
}

fun clickCancelButton() {
    onView(allOf(
            withText(android.R.string.cancel))
    ).perform(click())
}

fun assertPageDisplayed(title: Int) {
    onView(allOf(
            withText(title),
            withParent(withId(R.id.action_bar)))
    ).check(matches(isDisplayed()))
}

fun assertAlertDialogDisplayed(title: Int) {
    onView(allOf(
            withText(title),
            isDescendantOfA(instanceOf(AlertDialogLayout::class.java)))
    ).check(matches(isDisplayed()))
}

fun hideBatteryOptimizationDialog() {
    if (onView(withText(R.string.battery_optimization)).isExists()) {
        clickCancelButton()
    }
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
