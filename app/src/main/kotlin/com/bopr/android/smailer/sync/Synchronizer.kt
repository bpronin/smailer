package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.util.GeoCoordinates.Companion.coordinatesOf
import com.bopr.android.smailer.provider.telephony.PhoneEventData
import com.bopr.android.smailer.external.GoogleDrive
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Performs synchronization application data with google drive.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class Synchronizer(context: Context,
                            account: Account,
                            private val database: Database,
                            private val drive: GoogleDrive = GoogleDrive(context, account),
                            private val metaFile: String = "meta.json",
                            private val dataFile: String = "data.json") {

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
                    textBlacklist = smsTextBlacklist,
                    textWhitelist = smsTextWhitelist,
                    events = phoneEvents.map(::eventToData)
            )
        }
    }

    private fun putLocalData(data: SyncData) {
        database.commit(false) {
            batch {
                phoneEvents.replaceAll(data.events.map(::dataToEvent))
                phoneBlacklist.replaceAll(data.phoneBlacklist)
                phoneWhitelist.replaceAll(data.phoneWhitelist)
                smsTextBlacklist.replaceAll(data.textBlacklist)
                smsTextWhitelist.replaceAll(data.textWhitelist)
            }
        }
    }

    private fun eventToData(event: PhoneEventData): SyncData.Event {
        return SyncData.Event(
                incoming = event.isIncoming,
                missed = event.isMissed,
                phone = event.phone,
                recipient = event.acceptor,
                startTime = event.startTime,
                endTime = event.endTime,
                text = event.text,
                details = event.details,
                latitude = event.location?.latitude,
                longitude = event.location?.longitude,
                state = event.state,
                processStatus = event.processStatus,
                processTime = event.processTime,
                isRead = event.isRead
        )
    }

    private fun dataToEvent(data: SyncData.Event): PhoneEventData {
        return PhoneEventData(
                phone = data.phone,
                isIncoming = data.incoming,
                startTime = data.startTime,
                endTime = data.endTime,
                isMissed = data.missed,
                text = data.text,
                location = coordinatesOf(data.latitude, data.longitude),
                details = data.details,
                state = data.state,
                acceptor = data.recipient,
                isRead = data.isRead,
                processStatus = data.processStatus,
                processTime = data.processTime
        )
    }

    companion object {

        private val log = LoggerFactory.getLogger("Synchronizer")

        const val SYNC_NORMAL = 0
        const val SYNC_FORCE_DOWNLOAD = 1
        const val SYNC_FORCE_UPLOAD = 2
    }
}