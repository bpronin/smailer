package com.bopr.android.smailer.ui


import android.app.Activity
import com.bopr.android.smailer.R
import com.bopr.android.smailer.data.StringDataset
import org.junit.Test
import kotlin.reflect.KClass


abstract class BaseEventFilterListActivityTest(activityClass: KClass<out Activity>,
                                               private val listName: String) : BaseActivityTest(activityClass) {

    protected lateinit var dataset: StringDataset

    override fun beforeActivityCreate() {
        dataset = database.phoneEventsFilters.getValue(listName)
    }

    @Test
    open fun testActivity() {
        testAddItem()
        testAddItemCancel()
        testEditItemClick()
        testEditItemCancel()
        testRemoveItemSwipe()
        testEditItemMenu()
        testRemoveItemMenu()
        testClear()
        testClearCancel()
        testSort()
    }

    private fun testAddItem() {
        database.commit {
            dataset.clear()
        }

        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("A")
        clickOkButton()

        assertRecyclerItemDisplayed("A")
    }

    private fun testAddItemCancel() {
        database.commit {
            dataset.clear()
        }

        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("A")
        clickCancelButton()

        assertRecyclerItemNotDisplayed("A")
    }

    private fun testEditItemClick() {
        database.commit {
            dataset.replaceAll(setOf("A"))
        }

        assertRecyclerItemDisplayed("A")

        clickRecyclerItem("A")

        assertAlertDialogDisplayed(R.string.edit)
        assertTextInputDisplayed("A")

        inputText("B")
        clickOkButton()

        assertRecyclerItemDisplayed("B")
        assertRecyclerItemNotDisplayed("A")
    }

    private fun testEditItemCancel() {
        database.commit {
            dataset.replaceAll(setOf("A"))
        }

        assertRecyclerItemDisplayed("A")

        clickRecyclerItem("A")

        assertAlertDialogDisplayed(R.string.edit)
        assertTextInputDisplayed("A")

        inputText("B")
        clickCancelButton()

        assertRecyclerItemDisplayed("A")
        assertRecyclerItemNotDisplayed("B")
    }

    private fun testEditItemMenu() {
        database.commit {
            dataset.replaceAll(setOf("A"))
        }

        assertRecyclerItemDisplayed("A")

        longClickRecyclerItem("A")
        clickContextMenuItem(R.string.edit)

        assertAlertDialogDisplayed(R.string.edit)

        inputText("B")
        clickOkButton()

        assertRecyclerItemDisplayed("B")
        assertRecyclerItemNotDisplayed("A")
    }

    private fun testRemoveItemSwipe() {
        database.commit {
            dataset.replaceAll(setOf("A"))
        }

        assertRecyclerItemDisplayed("A")

        swipeRecyclerItem("A")

        assertRecyclerItemNotDisplayed("A")
    }

    private fun testRemoveItemMenu() {
        database.commit {
            dataset.replaceAll(setOf("A"))
        }

        assertRecyclerItemDisplayed("A")

        longClickRecyclerItem("A")
        clickContextMenuItem(R.string.remove)

        assertRecyclerItemNotDisplayed("A")
    }

    private fun testClear() {
        database.commit {
            dataset.replaceAll(setOf("A", "B", "C"))
        }

        assertRecyclerItemDisplayed("A")
        assertRecyclerItemDisplayed("B")
        assertRecyclerItemDisplayed("C")

        clickOptionsMenuItem(R.string.clear)
        clickOkButton()

        assertRecyclerItemNotDisplayed("A")
        assertRecyclerItemNotDisplayed("B")
        assertRecyclerItemNotDisplayed("C")
    }

    private fun testClearCancel() {
        database.commit {
            dataset.replaceAll(setOf("A", "B", "C"))
        }

        assertRecyclerItemDisplayed("A")
        assertRecyclerItemDisplayed("B")
        assertRecyclerItemDisplayed("C")

        clickOptionsMenuItem(R.string.clear)
        clickCancelButton()

        assertRecyclerItemDisplayed("A")
        assertRecyclerItemDisplayed("B")
        assertRecyclerItemDisplayed("C")
    }

    private fun testSort() {
        database.commit {
            dataset.replaceAll(setOf("C", "B", "A"))
        }

        assertRecyclerItemDisplayedAt("C", 0)
        assertRecyclerItemDisplayedAt("B", 1)
        assertRecyclerItemDisplayedAt("A", 2)

        clickOptionsMenuItem(R.string.sort)

        assertRecyclerItemDisplayedAt("A", 0)
        assertRecyclerItemDisplayedAt("B", 1)
        assertRecyclerItemDisplayedAt("C", 2)
    }

}
