package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.GeoCoordinates.Companion.coordinatesOf
import com.bopr.android.smailer.GoogleDrive
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_SYNC_TIME
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 *Performs synchronization.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class Synchronizer(context: Context,
                   account: Account,
                   private val database: Database = Database(context),
                   private val settings: Settings = Settings(context),
                   private val metaFile: String = "meta.json",
                   private val dataFile: String = "data.json") {

    private val log = LoggerFactory.getLogger("Synchronizer")
    private val drive = GoogleDrive(context, account)

    @Throws(IOException::class)
    fun sync(): Synchronizer {
        val localMeta = localMetaData()
        val remoteMeta = drive.download(metaFile, MetaData::class.java)
        if (remoteMeta == null || localMeta.syncTime >= remoteMeta.syncTime) {
            upload()
        } else {
            download()
        }
        return this
    }

    @Throws(IOException::class)
    fun download(): Synchronizer {
        val data = drive.download(dataFile, SyncData::class.java)
        if (data != null) {
            putLocalData(data)

            log.debug("Downloaded remote data")
        } else {
            log.debug("No remote data")
        }
        return this
    }

    @Throws(IOException::class)
    fun upload(): Synchronizer {
        drive.upload(metaFile, localMetaData())
        drive.upload(dataFile, getLocalData())

        log.debug("Uploaded local data")
        return this
    }

    @Throws(IOException::class)
    fun clear(): Synchronizer {
        drive.delete(metaFile)
        drive.delete(dataFile)

        log.debug("Remote data deleted")
        return this
    }

    fun dispose() {
        database.close()
    }

    private fun localMetaData(): MetaData {
        return MetaData(settings.getLong(PREF_SYNC_TIME, 0))
    }

    private fun getLocalData(): SyncData {
        val events = mutableListOf<SyncData.Event>()
        database.events.forEach { event ->
            events.add(eventToData(event))
        }

        with(settings.callFilter) {
            return SyncData(
                    phoneBlacklist,
                    textBlacklist,
                    phoneWhitelist,
                    textWhitelist,
                    events)
        }
    }

    private fun putLocalData(data: SyncData) {
        data.events?.map { e -> dataToEvent(e) }?.apply { database.putEvents(this) }

        with(settings.callFilter) {
            phoneBlacklist = data.phoneBlacklist ?: mutableSetOf()
            textBlacklist = data.textBlacklist ?: mutableSetOf()
            phoneWhitelist = data.phoneWhitelist ?: mutableSetOf()
            textWhitelist = data.textWhitelist ?: mutableSetOf()

            settings.edit().putFilter(this).apply()
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
                state = event.state)
    }

    private fun dataToEvent(data: SyncData.Event): PhoneEvent {
        return PhoneEvent(
                state = data.state,
                phone = data.phone,
                text = data.text,
                isIncoming = data.incoming,
                isMissed = data.missed,
                startTime = data.startTime,
                endTime = data.endTime,
                details = data.details,
                acceptor = data.recipient,
                location = coordinatesOf(data.latitude, data.longitude)
        )
    }

}