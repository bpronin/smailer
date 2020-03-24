package com.bopr.android.smailer.ui


import android.app.Activity
import com.bopr.android.smailer.R
import org.junit.Test
import kotlin.reflect.KClass


abstract class BaseEventFilterTextListActivityTest(activityClass: KClass<out Activity>,
                                                   listName: String) : BaseEventFilterListActivityTest(
        activityClass, listName) {

    @Test
    override fun testActivity() {
        super.testActivity()
        testAddRegex()
        testEditRegex()
    }

    private fun testAddRegex() {
        database.commit {
            dataset.clear()
        }

        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("A")
        clickCheckbox(string(R.string.regular_expression))
        clickOkButton()

        assertRecyclerItemDisplayed("A")
    }

    private fun testEditRegex() {
        database.commit {
            dataset.replaceAll(setOf("REGEX:A"))
        }

        clickRecyclerItem("A")

        assertAlertDialogDisplayed(R.string.edit)
        assertTextInputDisplayed("A")
        assertCheckboxChecked(string(R.string.regular_expression))

        inputText("B")
        clickOkButton()

        assertRecyclerItemDisplayed("B")
    }

}
