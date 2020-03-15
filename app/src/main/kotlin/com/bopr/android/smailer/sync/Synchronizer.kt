package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.GeoCoordinates.Companion.coordinatesOf
import com.bopr.android.smailer.GoogleDrive
import com.bopr.android.smailer.PhoneEvent
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 *Performs synchronization.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class Synchronizer(context: Context,
                            account: Account,
                            private val database: Database,
                            private val metaFile: String = "meta.json",
                            private val dataFile: String = "data.json") {

    private val log = LoggerFactory.getLogger("Synchronizer")
    private val drive = GoogleDrive(context)

    init {
        drive.login(account)
    }

    @Throws(IOException::class)
    fun sync() {
        val meta = drive.download(metaFile, MetaData::class)
        if (meta == null || meta.syncTime <= database.lastSyncTime) {
            upload()
        } else {
            download()
        }
    }

    @Throws(IOException::class)
    fun download() {
        val data = drive.download(dataFile, SyncData::class)
        if (data != null) {
            putLocalData(data)

            log.debug("Downloaded remote data")
        } else {
            log.debug("No remote data")
        }
    }

    @Throws(IOException::class)
    fun upload() {
        drive.upload(metaFile, MetaData(database.lastSyncTime))
        drive.upload(dataFile, getLocalData())

        log.debug("Uploaded local data")
    }

    @Throws(IOException::class)
    fun clear() {
        drive.delete(metaFile)
        drive.delete(dataFile)

        log.debug("Remote data deleted")
    }

    private fun getLocalData(): SyncData {
        return database.batchRead {
            SyncData(
                    phoneBlacklist = phoneBlacklist,
                    phoneWhitelist = phoneWhitelist,
                    textBlacklist = textBlacklist,
                    textWhitelist = textWhitelist,
                    events = events.map(::eventToData)
            )
        }
    }

    private fun putLocalData(data: SyncData) {
        database.batchWrite {
            data.events.map(::dataToEvent).let(::putEvents)
            phoneBlacklist = data.phoneBlacklist
            phoneWhitelist = data.phoneWhitelist
            textBlacklist = data.textBlacklist
            textWhitelist = data.textWhitelist
        }
    }

    private fun eventToData(event: PhoneEvent): SyncData.Event {
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

    private fun dataToEvent(data: SyncData.Event): PhoneEvent {
        return PhoneEvent(
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

}