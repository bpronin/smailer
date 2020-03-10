package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.GeoCoordinates.Companion.coordinatesOf
import com.bopr.android.smailer.GoogleDrive
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.Settings
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_PHONE_WHITELIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_BLACKLIST
import com.bopr.android.smailer.Settings.Companion.PREF_FILTER_TEXT_WHITELIST
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
                            private val database: Database,
                            private val settings: Settings = Settings(context),
                            private val metaFile: String = "meta.json",
                            private val dataFile: String = "data.json") {

    private val log = LoggerFactory.getLogger("Synchronizer")
    private val drive = GoogleDrive(context)

    init {
        drive.login(account)
    }

    @Throws(IOException::class)
    fun sync() {
        val localMeta = localMetaData()
        val remoteMeta = drive.download(metaFile, MetaData::class.java)
        if (remoteMeta == null || localMeta.syncTime >= remoteMeta.syncTime) {
            upload()
        } else {
            download()
        }
    }

    @Throws(IOException::class)
    fun download() {
        val data = drive.download(dataFile, SyncData::class.java)
        if (data != null) {
            putLocalData(data)

            log.debug("Downloaded remote data")
        } else {
            log.debug("No remote data")
        }
    }

    @Throws(IOException::class)
    fun upload() {
        drive.upload(metaFile, localMetaData())
        drive.upload(dataFile, getLocalData())

        log.debug("Uploaded local data")
    }

    @Throws(IOException::class)
    fun clear() {
        drive.delete(metaFile)
        drive.delete(dataFile)

        log.debug("Remote data deleted")
    }

    private fun localMetaData(): MetaData {
        return MetaData(settings.getLong(PREF_SYNC_TIME, 0))
    }

    private fun getLocalData(): SyncData {
        return SyncData(
                phoneBlacklist = settings.getStringList(PREF_FILTER_PHONE_BLACKLIST),
                phoneWhitelist = settings.getStringList(PREF_FILTER_PHONE_WHITELIST),
                textBlacklist = settings.getStringList(PREF_FILTER_TEXT_BLACKLIST),
                textWhitelist = settings.getStringList(PREF_FILTER_TEXT_WHITELIST),
                events = database.events.map(::eventToData)
        )
    }

    private fun putLocalData(data: SyncData) {
        data.events?.map(::dataToEvent)?.let(database::putEvents)

        settings.update {
            putStringList(PREF_FILTER_PHONE_BLACKLIST, data.phoneBlacklist)
            putStringList(PREF_FILTER_PHONE_WHITELIST, data.phoneWhitelist)
            putStringList(PREF_FILTER_TEXT_BLACKLIST, data.textBlacklist)
            putStringList(PREF_FILTER_TEXT_WHITELIST, data.textWhitelist)
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