package com.bopr.android.smailer.ui


import android.view.View
import android.view.ViewGroup
import android.widget.Checkable
import android.widget.EditText
import androidx.appcompat.widget.AlertDialogLayout
import androidx.recyclerview.widget.RecyclerView
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import com.bopr.android.smailer.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.Matchers.*
import org.hamcrest.TypeSafeMatcher

fun string(id: Int): String {
    return InstrumentationRegistry.getInstrumentation().targetContext.getString(id)
}

fun stringArray(id: Int): Array<String> {
    return InstrumentationRegistry.getInstrumentation().targetContext.resources.getStringArray(id)
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

private fun preferenceTitle(title: Int): Matcher<View> {
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

private fun checkBox(title: String): Matcher<View>? {
    return allOf(
            instanceOf(Checkable::class.java),
            withText(title)
    )
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

fun clickFab() {
    onView(allOf(
            withId(R.id.button_add),
            instanceOf(FloatingActionButton::class.java)
    )).perform(click())
}

fun clickCancelButton() {
    onView(withText(android.R.string.cancel)).perform(click())
}

fun clickCheckbox(title: String) {
    onView(checkBox(title)).perform(click())
}

fun clickRecyclerItem(text: String) {
    onView(allOf(
            isDescendantOfA(instanceOf(RecyclerView::class.java)),
            withText(text)
    )).perform(click())
}

fun swipeRecyclerItem(text: String) {
    onView(allOf(
            isDescendantOfA(instanceOf(RecyclerView::class.java)),
            withText(text)
    )).perform(swipeRight())
}

fun clearInputText() {
    onView(instanceOf(EditText::class.java)).perform(clearText())
}

fun inputText(text: String) {
    onView(instanceOf(EditText::class.java)).perform(replaceText(text))
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

fun assertPreferenceSummaryIs(title: Int, summary: Int) {
    assertPreferenceSummaryIs(title, string(summary))
}

fun assertPreferenceSummaryIs(title: Int, summary: String) {
    onView(allOf(
            withId(android.R.id.summary),
            hasSibling(preferenceTitle(title)))
    ).check(matches(withText(summary)))
}

fun assertPreferenceEnabled(title: Int) {
    onView(preferenceTitle(title)).check(matches(isEnabled()))
}

fun assertPreferenceDisabled(title: Int) {
    onView(preferenceTitle(title)).check(matches(not(isEnabled())))
}

fun assertCheckboxChecked(title: String) {
    onView(checkBox(title)).check(matches(isChecked()))
}

fun assertCheckboxUnchecked(title: String) {
    onView(checkBox(title)).check(matches(isNotChecked()))
}

fun assertRecyclerItemDisplayed(text: String) {
    onView(allOf(
            isDescendantOfA(instanceOf(RecyclerView::class.java)),
            withText(text)
    )).check(matches(isDisplayed()))
}

fun assertRecyclerItemNotDisplayed(text: String) {
    onView(allOf(
            isDescendantOfA(instanceOf(RecyclerView::class.java)),
            withText(text)
    )).check(doesNotExist())
}
