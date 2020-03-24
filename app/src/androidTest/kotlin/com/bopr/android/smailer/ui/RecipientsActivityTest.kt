package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_RECIPIENTS_ADDRESS
import org.junit.Test


class RecipientsActivityTest : BaseActivityTest(RecipientsActivity::class) {

    @Test
    fun testActivity() {
        testAddItem()
        testAddItemCancel()
        testEditItem()
        testEditItemCancel()
        testRemoveItem()
    }

    private fun testAddItem() {
        settings.update {
            putStringList(PREF_RECIPIENTS_ADDRESS, emptyList())
        }

        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("user@mail.ru")
        clickOkButton()

        assertRecyclerItemDisplayed("user@mail.ru")
    }

    private fun testAddItemCancel() {
        settings.update {
            putStringList(PREF_RECIPIENTS_ADDRESS, emptyList())
        }

        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("user@mail.ru")
        clickCancelButton()

        assertRecyclerItemNotDisplayed("user@mail.ru")
    }

    private fun testEditItem() {
        settings.update {
            putStringList(PREF_RECIPIENTS_ADDRESS, listOf("user@mail.ru"))
        }

        clickRecyclerItem("user@mail.ru")

        assertAlertDialogDisplayed(R.string.edit)

        inputText("user2@mail.ru")
        clickOkButton()

        assertRecyclerItemDisplayed("user2@mail.ru")
        assertRecyclerItemNotDisplayed("user@mail.ru")
    }

    private fun testEditItemCancel() {
        settings.update {
            putStringList(PREF_RECIPIENTS_ADDRESS, listOf("user@mail.ru"))
        }

        clickRecyclerItem("user@mail.ru")

        assertAlertDialogDisplayed(R.string.edit)

        inputText("user2@mail.ru")
        clickCancelButton()

        assertRecyclerItemDisplayed("user@mail.ru")
        assertRecyclerItemNotDisplayed("user2@mail.ru")
    }

    private fun testRemoveItem() {
        settings.update {
            putStringList(PREF_RECIPIENTS_ADDRESS, listOf("user@mail.ru"))
        }

        swipeRecyclerItem("user@mail.ru")

        assertRecyclerItemNotDisplayed("user@mail.ru")
    }

}
