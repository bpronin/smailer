package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.external.GoogleDrive
import com.bopr.android.smailer.messenger.Event
import com.bopr.android.smailer.messenger.EventPayload
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.util.Bits
import com.bopr.android.smailer.util.GeoLocation.Companion.fromCoordinates
import com.bopr.android.smailer.util.Logger
import kotlinx.parcelize.Parcelize
import java.io.IOException

/**
 * Performs synchronization application data with google drive.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class Synchronizer(
    context: Context,
    account: Account,
    private val database: Database,
    private val drive: GoogleDrive = GoogleDrive(context, account),
    private val metaFile: String = "meta.json",
    private val dataFile: String = "data.json"
) {

    /**
     * Synchronizes database with google drive.
     *
     * @param mode one of [SYNC_FORCE_DOWNLOAD], [SYNC_FORCE_UPLOAD], [SYNC_NORMAL]
     * @return true if local database have been changed
     */
    @Throws(IOException::class)
    fun sync(mode: Int = SYNC_NORMAL): Boolean {
        when (mode) {
            SYNC_FORCE_DOWNLOAD -> {
                download()
            }

            SYNC_FORCE_UPLOAD -> {
                upload()
                return true
            }

            else -> {
                val databaseTime = database.updateTime
                val meta = drive.download(metaFile, SyncMetaData::class)
                when {
                    meta == null || meta.time < databaseTime -> {
                        upload()
                        return true
                    }

                    meta.time != databaseTime -> {
                        download()
                    }

                    else -> {
                        log.debug("Data is up to date")
                    }
                }
            }
        }
        return false
    }

    @Throws(IOException::class)
    fun clear() {
        drive.delete(metaFile)
        drive.delete(dataFile)

        log.debug("Remote data deleted")
    }

    @Throws(IOException::class)
    private fun download() {
        val data = drive.download(dataFile, SyncData::class)
        if (data != null) {
            putLocalData(data)

            log.debug("Downloaded")
        } else {
            log.debug("No remote data")
        }
    }

    @Throws(IOException::class)
    private fun upload() {
        drive.upload(metaFile, SyncMetaData(database.updateTime))
        drive.upload(dataFile, getLocalData())

        log.debug("Uploaded")
    }

    private fun getLocalData(): SyncData {
        return database.run {
            SyncData(
                phoneBlacklist = phoneBlacklist,
                phoneWhitelist = phoneWhitelist,
                textBlacklist = textBlacklist,
                textWhitelist = textWhitelist,
                events = events.map(::eventToData),

//                    phoneCalls = phoneCalls.map(::phoneCallToData)
            )
        }
    }

    private fun putLocalData(data: SyncData) {
        database.commit(false) {
            batch {
                events.replaceAll(data.events.map(::dataToEvent))
//                phoneCalls.replaceAll(data.phoneCalls.map(::dataToPhoneCall))
                phoneBlacklist.replaceAll(data.phoneBlacklist)
                phoneWhitelist.replaceAll(data.phoneWhitelist)
                textBlacklist.replaceAll(data.textBlacklist)
                textWhitelist.replaceAll(data.textWhitelist)
            }
        }
    }

    // TODO: read and write raw records directly from database
    //  do not convert into Payload objects

    private fun eventToData(event: Event): SyncData.Event {
        return SyncData.Event(
            timestamp = event.timestamp,
            target = event.target,
            latitude = event.location?.latitude,
            longitude = event.location?.longitude,
            processState = event.processState,
            bypassFlags = event.bypassFlags.toInt(),
            processFlags = event.processFlags.toInt(),
            processTime = event.processTime,
            isRead = event.isRead
        )
    }

    @Suppress("PARCELABLE_PRIMARY_CONSTRUCTOR_IS_EMPTY")
    @Parcelize
    class DummyPayload() : EventPayload

    private fun dataToEvent(data: SyncData.Event): Event {
        return Event(
            timestamp = data.timestamp,
            target = data.target,
            location = fromCoordinates(data.latitude, data.longitude),
            processState = data.processState,
            bypassFlags = Bits(data.bypassFlags),
            processFlags = Bits(data.processFlags),
            processTime = data.processTime,
            isRead = data.isRead,
            payload = DummyPayload()
        )
    }

    private fun phoneCallToData(info: PhoneCallInfo): SyncData.PhoneCall {
        return SyncData.PhoneCall(
            incoming = info.isIncoming,
            missed = info.isMissed,
            phone = info.phone,
            startTime = info.startTime,
            endTime = info.endTime,
            text = info.text
        )
    }

    private fun dataToPhoneCall(data: SyncData.PhoneCall): PhoneCallInfo {
        return PhoneCallInfo(
            phone = data.phone,
            isIncoming = data.incoming,
            startTime = data.startTime,
            endTime = data.endTime,
            isMissed = data.missed,
            text = data.text
        )
    }

    companion object {

        private val log = Logger("Synchronizer")

        const val SYNC_NORMAL = 0
        const val SYNC_FORCE_DOWNLOAD = 1
        const val SYNC_FORCE_UPLOAD = 2
    }
}