package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.StringDataset
import org.junit.Test


class EventFilterPhoneBlacklistActivityTest : BaseActivityTest(EventFilterPhoneBlacklistActivity::class) {

    private lateinit var dataset:StringDataset

    override fun beforeActivityCreate() {
       dataset = database.phoneBlacklist
    }

    @Test
    fun testActivity() {
        testAddItem()
        testAddItemCancel()
        testEditItemClick()
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

        inputText("12345")
        clickOkButton()

        assertRecyclerItemDisplayed("12345")
    }

    private fun testAddItemCancel() {
        database.commit {
            dataset.clear()
        }

        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("12345")
        clickCancelButton()

        assertRecyclerItemNotDisplayed("12345")
    }

    private fun testEditItemClick() {
        database.commit {
            dataset.replaceAll(setOf("A"))
        }

        assertRecyclerItemDisplayed("A")

        clickRecyclerItem("A")

        assertAlertDialogDisplayed(R.string.edit)

        inputText("B")
        clickOkButton()

        assertRecyclerItemDisplayed("B")
        assertRecyclerItemNotDisplayed("A")
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
