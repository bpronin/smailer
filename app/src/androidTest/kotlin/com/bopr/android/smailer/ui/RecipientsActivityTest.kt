package com.bopr.android.smailer.ui


import com.bopr.android.smailer.R
import com.bopr.android.smailer.Settings.Companion.PREF_REMOTE_CONTROL_FILTER_RECIPIENTS
import org.junit.Test


class RecipientsActivityTest : BaseActivityTest(RecipientsActivity::class) {

    override fun beforeActivityCreate() {
        settings.update {
            putStringSet(PREF_REMOTE_CONTROL_FILTER_RECIPIENTS, emptySet())
        }
    }

    @Test
    fun testActivity() {
        testAddItemButton()
        testEditItem()
        testRemoveItem()
    }

    private fun testAddItemButton() {
        clickFab()

        assertAlertDialogDisplayed(R.string.add)

        inputText("user@mail.ru")
        clickOkButton()

        assertRecyclerItemDisplayed("user@mail.ru")
    }

    private fun testEditItem() {
        clickRecyclerItem("user@mail.ru")

        assertAlertDialogDisplayed(R.string.edit)

        inputText("user2@mail.ru")
        clickOkButton()

        assertRecyclerItemDisplayed("user2@mail.ru")
        assertRecyclerItemNotDisplayed("user@mail.ru")
    }

    private fun testRemoveItem() {
        swipeRecyclerItem("user2@mail.ru")

        assertRecyclerItemNotDisplayed("user2@mail.ru")
    }

}
