@file:Suppress("DEPRECATION")

package com.bopr.android.smailer.messenger.mail

///**
// * [PhoneCallEventProcessor] tester.
// *
// * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
// */
//@SmallTest
//class PhoneCallProcessorTest : BaseTest() {
//
//    @get:Rule
//    var permissionRule = grant(READ_CONTACTS)
//
//    private lateinit var database: Database
//    private lateinit var context: Context
//    private lateinit var messenger: MessageDispatcher
//    private lateinit var notifications: NotificationsHelper
//    private lateinit var preferences: SharedPreferences
//    private lateinit var processor: PhoneCallEventProcessor
//    private lateinit var accountManager: AccountManager
//
//    private fun defaultPayload(): PhoneCallData {
//        val time = System.currentTimeMillis()
//        return PhoneCallData(
//            startTime = time,
//            phone = "+123",
//            isIncoming = true,
//            endTime = time + 1000
//        )
//    }
//
//    @Suppress("DEPRECATION")
//    @Before
//    fun setUp() {
//        preferences = mock {
//            on {
//                getString(
//                    eq(PREF_MAIL_SENDER_ACCOUNT),
//                    anyOrNull()
//                )
//            }.doReturn("sender@mail.com")
//            on {
//                getString(
//                    eq(PREF_MAIL_MESSENGER_RECIPIENTS),
//                    anyOrNull()
//                )
//            }.doReturn("recipient@mail.com")
//            on {
//                getString(
//                    eq(PREF_MESSAGE_LOCALE),
//                    anyOrNull()
//                )
//            }.doReturn(VAL_PREF_DEFAULT)
//            on {
//                getStringSet(
//                    eq(PREF_PHONE_PROCESS_TRIGGERS),
//                    anyOrNull()
//                )
//            }.doReturn(
//                setOf(
//                    VAL_PREF_TRIGGER_IN_SMS,
//                    VAL_PREF_TRIGGER_MISSED_CALLS
//                )
//            )
//            on {
//                getStringSet(
//                    eq(PREF_MAIL_MESSAGE_CONTENT),
//                    anyOrNull()
//                )
//            }.doReturn(
//                setOf(
//                    VAL_PREF_MESSAGE_CONTENT_BODY,
//                    VAL_PREF_MESSAGE_CONTENT_CALLER,
//                    VAL_PREF_MESSAGE_CONTENT_DEVICE_NAME,
//                    VAL_PREF_MESSAGE_CONTENT_HEADER,
//                    VAL_PREF_MESSAGE_CONTENT_LOCATION,
//                    VAL_PREF_MESSAGE_CONTENT_CREATION_TIME,
//                    VAL_PREF_MESSAGE_CONTENT_DISPATCH_TIME,
//                    VAL_PREF_MESSAGE_CONTENT_CONTROL_LINKS
//                )
//            )
//        }
//
//        accountManager = mock {
//            on { getAccountsByType(eq("com.google")) }.doReturn(
//                arrayOf(
//                    Account(
//                        "sender@mail.com",
//                        "com.google"
//                    )
//                )
//            )
//        }
//
//        val connectivityManager = mock<ConnectivityManager> {
//            val networkInfo = mock<NetworkInfo> { /* do not inline. do not optimize imports */
//                on { isConnectedOrConnecting }.doReturn(true)
//            }
//            on { activeNetworkInfo }.doReturn(networkInfo)
//        }
//
//        context = mock {
//            on { contentResolver }.doReturn(targetContext.contentResolver)
//            on { resources }.doReturn(targetContext.resources)
//            on {
//                getSharedPreferences(
//                    anyString(),
//                    anyInt()
//                )
//            }.doReturn(preferences)
//            on { getSystemService(eq(Context.ACCOUNT_SERVICE)) }.doReturn(accountManager)
//            on { getSystemService(eq(Context.CONNECTIVITY_SERVICE)) }.doReturn(connectivityManager)
//        }
//
//        messenger = mock()
//        notifications = mock()
//
//        database = targetContext.database
////            .apply {
////            name = "test.sqlite"
////            destroy()
////        } /* not a mock context here! */
//
//        processor = PhoneCallEventProcessor(context)
//    }
//
//    @After
//    fun tearDown() {
//        database.close()
//    }
//
//    /**
//     * Tests successful processing - mail sent.
//     */
//    @Test
//    fun testProcessMailSent() {
//        val call = defaultPayload()
//
//        processor.addRecord(call)
//        processor.processPending()
//
////        verify(messenger).sendMessagesFor(argThat {
////            id == null
////                    && subject == "[SMailer] Incoming SMS from \"+123\""
////                    && !body.isNullOrBlank()
////                    && attachment == null
////                    && recipients == "recipient@mail.com"
////                    && replyTo == null
////                    && from == "sender@mail.com"
////        })
//        verifyNotifyAccountError()
//        verifyNotifyRecipientsError()
//        verifyNotifyGoogleAccessError()
//
//        val savedEvent = database.events.first()
//        val info = savedEvent.payload as PhoneCallData
//
//        assertEquals(call.phone, info.phone)
//        assertEquals(STATE_PROCESSED, savedEvent.processState)
//        assertTrue(savedEvent.bypassFlags.isEmpty())
//        assertEquals(GeoLocation(60.0, 30.0), savedEvent.location)
//    }
//
//    /**
//     * Tests successful processing - call ignored.
//     */
//    @Test
//    fun testProcessIgnored() {
//        /* make the call not an SMS then default filter will deny it */
//        val call = defaultPayload()
//
//        processor.addRecord(call)
//        processor.processPending()
//
//        verify(messenger, never()).dispatch(any(), any(), any())
//        verifyNotifyAccountError()
//        verifyNotifyRecipientsError()
//        verifyNotifyGoogleAccessError()
//
//        val savedEvent = database.events.first()
//
//        assertEquals(STATE_IGNORED, savedEvent.processState)
//        assertEquals(FLAG_BYPASS_TRIGGER_OFF, savedEvent.bypassFlags)
//    }
//
//    /**
//     * Test processing when sender account setting is unspecified.
//     */
//    @Test
//    fun testProcessNoSender() {
//        whenever(preferences.getString(eq(PREF_MAIL_SENDER_ACCOUNT), anyOrNull())).thenReturn(null)
//
//        val call = defaultPayload()
//        processor.addRecord(call)
//        processor.processPending()
//
////        verify(messenger, never()).login(any(), any())
//        verify(messenger, never()).dispatch(any(), any(), any())
//        verify(notifications).notifyError(
//            eq(NTF_GOOGLE_ACCOUNT),
//            eq(getString(R.string.sender_account_not_found)),
//            eq(MailSettingsActivity::class)
//        )
//
//        val savedEvent = database.events.first()
//
//        assertEquals(STATE_PENDING, savedEvent.processState)
//        assertTrue(savedEvent.bypassFlags.isEmpty())
//    }
//
//    /**
//     * Test processing when recipients setting is unspecified.
//     */
//    @Test
//    fun testProcessNoRecipients() {
//        whenever(
//            preferences.getString(
//                eq(PREF_MAIL_MESSENGER_RECIPIENTS),
//                anyOrNull()
//            )
//        ).thenReturn(null)
//
//        val call = defaultPayload()
//        processor.addRecord(call)
//        processor.processPending()
//
//        verify(messenger, never()).dispatch(any(), any(), any())
//        verify(notifications).notifyError(
//            NTF_MAIL_RECIPIENTS,
//            eq(getString(R.string.no_recipients_specified)),
//            MailRecipientsActivity::class
//        )
//
//        val savedEvent = database.events.first()
//
//        assertEquals(STATE_PENDING, savedEvent.processState)
//        assertTrue(savedEvent.bypassFlags.isEmpty())
//    }
//
//    /**
//     * Transport exception during send does not produce any notifications.
//     */
//    @Test
//    fun testProcessTransportSendFailed() {
//        doThrow(IOException("Test error")).whenever(messenger).dispatch(any(), any(), any())
//
//        val call = defaultPayload()
//        processor.addRecord(call)
//        processor.processPending()
//
////        verify(messenger).login(any(), any())
//        verify(messenger).dispatch(any(), any(), any())
//        verifyNotifyAccountError()
//        verifyNotifyRecipientsError()
//        verifyNotifyGoogleAccessError()
//
//        val savedEvent = database.events.first()
//
//        assertEquals(STATE_PENDING, savedEvent.processState)
//        assertTrue(savedEvent.bypassFlags.isEmpty())
//    }
//
//    /**
//     * When [PREF_NOTIFY_SEND_SUCCESS] setting is set to true then success notification should be shown.
//     */
//    @Test
//    fun testSuccessNotification() {
//        /* the setting is OFF */
//        whenever(
//            preferences.getBoolean(
//                eq(PREF_NOTIFY_SEND_SUCCESS),
//                anyOrNull()
//            )
//        ).thenReturn(false)
//
//        processor.addRecord(defaultPayload())
//        processor.processPending()
//
//        verifyNotifyMailSuccess()
//
//        /* the setting is ON */
//        whenever(
//            preferences.getBoolean(
//                eq(PREF_NOTIFY_SEND_SUCCESS),
//                anyOrNull()
//            )
//        ).thenReturn(true)
//
//        processor.addRecord(defaultPayload())
//        processor.processPending()
//
//        verifyNotifyMailSuccess()
//    }
//
//    /**
//     * Test resending pending messages.
//     */
//    @Test
//    fun testProcessRecords() {
//        /* disable transport */
//        doThrow(IOException("Test error")).whenever(messenger).dispatch(any(), any(), any())
//
//        processor.addRecord(defaultPayload())
//        processor.addRecord(defaultPayload())
//        processor.addRecord(defaultPayload())
//        processor.processPending()
//
//        assertEquals(3, database.events.size)
//        assertEquals(3, database.events.pending.size)
//        verifyNotifyAccountError()
//        verifyNotifyRecipientsError()
//        verifyNotifyGoogleAccessError()
//
//        /* try resend with disabled transport */
//        processor.processPending()
//
//        assertEquals(3, database.events.size)
//        assertEquals(3, database.events.pending.size)
//        verifyNotifyAccountError()
//        verifyNotifyRecipientsError()
//        verifyNotifyGoogleAccessError()
//
//        /* enable transport an try again */
//        doNothing().whenever(messenger).dispatch(any(), any(), any())
//
//        processor.processPending()
//
//        assertEquals(3, database.events.size)
//        assertEquals(0, database.events.pending.size)
//        verifyNotifyAccountError()
//        verifyNotifyRecipientsError()
//        verifyNotifyGoogleAccessError()
//    }
//
//    private fun verifyNotifyMailSuccess() {
//        verify(notifications, never()).notifyInfo(
//            eq(getString(R.string.email_successfully_send)),
//            eq(null),
//            eq(MainActivity::class)
//        )
//    }
//
//    private fun verifyNotifyAccountError() {
//        verify(notifications, never()).notifyError(
//            eq(NTF_GOOGLE_ACCOUNT),
//            eq(getString(R.string.sender_account_not_found)),
//            eq(MailSettingsActivity::class)
//        )
//    }
//
//    private fun verifyNotifyGoogleAccessError() {
//        verify(notifications, never()).notifyError(
//            eq(NTF_GOOGLE_ACCESS),
//            eq(getString(R.string.no_access_to_google_account)),
//            eq(MailSettingsActivity::class)
//        )
//    }
//
//    private fun verifyNotifyRecipientsError() {
//        verify(notifications, never()).notifyError(
//            eq(NTF_GOOGLE_ACCESS),
//            anyString(),
//            eq(MailRecipientsActivity::class)
//        )
//    }
//}