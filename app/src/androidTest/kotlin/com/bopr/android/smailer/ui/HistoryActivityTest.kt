package com.bopr.android.smailer.ui


import com.bopr.android.smailer.provider.telephony.PhoneEventInfo
import com.bopr.android.smailer.R
import com.bopr.android.smailer.data.StringDataset
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.System.currentTimeMillis


class HistoryActivityTest : BaseActivityTest(HistoryActivity::class) {

    @Test
    fun testActivity() {
        testClear()
        testClearCancel()
        testRemoveItem()
        testMarkAllAsRead()
        testAddPhoneToBlacklist()
        testAddPhoneToBlacklistCancel()
        testAddPhoneToWhitelist()
        testAddPhoneToWhitelistCancel()
        testAddTextToBlacklist()
        testAddTextToBlacklistCancel()
        testAddTextToWhitelist()
        testAddTextToWhitelistCancel()
    }

    private fun testClear() {
        database.commit {
            phoneEvents.clear()
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = currentTimeMillis(), acceptor = "device-1"))
            phoneEvents.add(PhoneEventInfo(phone = "2", startTime = currentTimeMillis(), acceptor = "device-2"))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = currentTimeMillis(), acceptor = "device-3"))
        }

        assertRecyclerItemDisplayed("1")
        assertRecyclerItemDisplayed("2")
        assertRecyclerItemDisplayed("3")

        clickOptionsMenuItem(R.string.clear)

        assertAlertDialogDisplayed(R.string.ask_clear_history)

        clickOkButton()

        assertRecyclerItemNotDisplayed("1")
        assertRecyclerItemNotDisplayed("2")
        assertRecyclerItemNotDisplayed("3")
    }

    private fun testClearCancel() {
        database.commit {
            phoneEvents.clear()
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = currentTimeMillis(), acceptor = "device-1"))
            phoneEvents.add(PhoneEventInfo(phone = "2", startTime = currentTimeMillis(), acceptor = "device-2"))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = currentTimeMillis(), acceptor = "device-3"))
        }

        assertRecyclerItemDisplayed("1")
        assertRecyclerItemDisplayed("2")
        assertRecyclerItemDisplayed("3")

        clickOptionsMenuItem(R.string.clear)

        assertAlertDialogDisplayed(R.string.ask_clear_history)

        clickCancelButton()

        assertRecyclerItemDisplayed("1")
        assertRecyclerItemDisplayed("2")
        assertRecyclerItemDisplayed("3")
    }

    private fun testRemoveItem() {
        database.commit {
            phoneEvents.clear()
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = currentTimeMillis(), acceptor = "device-1"))
            phoneEvents.add(PhoneEventInfo(phone = "2", startTime = currentTimeMillis(), acceptor = "device-2"))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = currentTimeMillis(), acceptor = "device-3"))
        }

        swipeRecyclerItem("2")

        assertRecyclerItemDisplayed("1")
        assertRecyclerItemNotDisplayed("2")
        assertRecyclerItemDisplayed("3")
    }

    private fun testMarkAllAsRead() {
        database.commit {
            phoneEvents.clear()
            phoneEvents.add(
                PhoneEventInfo(phone = "1", startTime = currentTimeMillis(), acceptor = "device-1",
                    isRead = false)
            )
            phoneEvents.add(
                PhoneEventInfo(phone = "2", startTime = currentTimeMillis(), acceptor = "device-2",
                    isRead = false)
            )
            phoneEvents.add(
                PhoneEventInfo(phone = "3", startTime = currentTimeMillis(), acceptor = "device-3",
                    isRead = false)
            )
        }

        assertEquals(3, database.phoneEvents.unreadCount)

        clickOptionsMenuItem(R.string.mark_all_as_read)

        assertEquals(0, database.phoneEvents.unreadCount)
    }

    private fun testAddToList(dataset: StringDataset, menuTitle: Int, dialogTitle: Int, isCheckingText: Boolean,
                              isCancel: Boolean) {
        database.commit {
            dataset.clear()
            phoneEvents.clear()
            phoneEvents.add(PhoneEventInfo(phone = "1", startTime = currentTimeMillis(), acceptor = "device-1"))
            phoneEvents.add(PhoneEventInfo(phone = "phone", startTime = currentTimeMillis(), acceptor = "device-2", text = "text"))
            phoneEvents.add(PhoneEventInfo(phone = "3", startTime = currentTimeMillis(), acceptor = "device-3"))
        }

        assertTrue(dataset.isEmpty())

        longClickRecyclerItem("phone")
        clickContextMenuItem(menuTitle)

        assertAlertDialogDisplayed(dialogTitle)
        if (isCheckingText) {
            assertTextInputDisplayed("text")
        } else {
            assertTextInputDisplayed("phone")
        }

        if (isCancel) {
            clickCancelButton()
        } else {
            clickOkButton()
        }

        assertRecyclerItemDisplayed("phone")

        if (isCancel) {
            assertTrue(dataset.isEmpty())
        } else {
            assertRecyclerItemDisplayed("phone")
            assertEquals(1, dataset.size)
            if (isCheckingText) {
                assertTrue(dataset.contains("text"))
            } else {
                assertTrue(dataset.contains("phone"))
            }
        }
    }

    private fun testAddPhoneToBlacklist() {
        testAddToList(
                database.phoneBlacklist,
                R.string.add_phone_to_blacklist_action,
                R.string.add_to_blacklist,
                isCheckingText = false,
                isCancel = false
        )
    }

    private fun testAddPhoneToBlacklistCancel() {
        testAddToList(
                database.phoneBlacklist,
                R.string.add_phone_to_blacklist_action,
                R.string.add_to_blacklist,
                isCheckingText = false,
                isCancel = true
        )
    }

    private fun testAddPhoneToWhitelist() {
        testAddToList(
                database.phoneWhitelist,
                R.string.add_phone_to_whitelist_action,
                R.string.add_to_whitelist,
                isCheckingText = false,
                isCancel = false
        )
    }

    private fun testAddPhoneToWhitelistCancel() {
        testAddToList(
                database.phoneWhitelist,
                R.string.add_phone_to_whitelist_action,
                R.string.add_to_whitelist,
                isCheckingText = false,
                isCancel = true
        )
    }

    private fun testAddTextToBlacklist() {
        testAddToList(
                database.smsTextBlacklist,
                R.string.add_text_to_blacklist_action,
                R.string.add_to_blacklist,
                isCheckingText = true,
                isCancel = false
        )
    }

    private fun testAddTextToBlacklistCancel() {
        testAddToList(
                database.smsTextBlacklist,
                R.string.add_text_to_blacklist_action,
                R.string.add_to_blacklist,
                isCheckingText = true,
                isCancel = true
        )
    }

    private fun testAddTextToWhitelist() {
        testAddToList(
                database.smsTextWhitelist,
                R.string.add_text_to_whitelist_action,
                R.string.add_to_whitelist,
                isCheckingText = true,
                isCancel = false
        )
    }

    private fun testAddTextToWhitelistCancel() {
        testAddToList(
                database.smsTextWhitelist,
                R.string.add_text_to_whitelist_action,
                R.string.add_to_whitelist,
                isCheckingText = true,
                isCancel = true
        )
    }

}
