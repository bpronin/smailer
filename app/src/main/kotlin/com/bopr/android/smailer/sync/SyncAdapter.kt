package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.AbstractThreadedSyncAdapter
import android.content.ContentProviderClient
import android.content.Context
import android.content.SyncResult
import android.os.Bundle
import com.bopr.android.smailer.Database
import com.bopr.android.smailer.GeoCoordinates.Companion.geoCoordinatesOf
import com.bopr.android.smailer.GoogleDrive
import com.bopr.android.smailer.PhoneEvent
import com.bopr.android.smailer.Settings
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Handle the transfer of data between a server and an app, using the Android sync adapter framework.
 *
 *
 * Required by synchronization framework.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
/* To debug it (to put breakpoints) remove android:process=":sync" from AndroidManifest */
class SyncAdapter(context: Context, autoInitialize: Boolean) : AbstractThreadedSyncAdapter(context, autoInitialize) {

    private val database: Database = Database(context)
    private val settings: Settings = Settings(context)

    override fun onPerformSync(account: Account, extras: Bundle, authority: String,
                               provider: ContentProviderClient, syncResult: SyncResult) {
        try {
            sync(context, account)
        } catch (x: Exception) {
            log.warn("Synchronization failed ", x)
        }
    }

    @Throws(IOException::class)
    fun sync(context: Context, account: Account) {
        val drive = GoogleDrive(context, account)
        val meta = getMetaData()
        val remoteMeta: MetaData? = drive.download(META_FILE, MetaData::class.java)
        if (remoteMeta == null || meta.syncTime >= remoteMeta.syncTime) {
            upload(drive)
        } else {
            download(drive)
        }
    }

    @Throws(IOException::class)
    fun download(drive: GoogleDrive) {
        val data = drive.download(DATA_FILE, SyncData::class.java)
        data?.let {
            putData(it)

            log.debug("Downloaded remote data")
        } ?: log.debug("No remote data")
    }

    @Throws(IOException::class)
    fun upload(drive: GoogleDrive) {
        drive.upload(META_FILE, getMetaData())
        drive.upload(DATA_FILE, getData())

        log.debug("Uploaded local data")
    }

    private fun getMetaData(): MetaData = MetaData(settings.getLong(Settings.PREF_SYNC_TIME, 0))

    private fun getData(): SyncData {
        val events = mutableListOf<SyncData.Event>()
        val rowSet = database.events
        rowSet.forEach { event ->
            events.add(eventToData(event))
        }

        with(settings.filter) {
            return SyncData(
                    phoneBlacklist,
                    textBlacklist,
                    phoneWhitelist,
                    textWhitelist,
                    events)
        }
    }

    private fun putData(data: SyncData) {
        for (event in data.events) {
            database.putEvent(dataToEvent(event))
        }

        with(settings.filter) {
            phoneBlacklist = data.phoneBlacklist
            textBlacklist = data.textBlacklist
            phoneWhitelist = data.phoneWhitelist
            textWhitelist = data.textWhitelist
        }

        settings.putFilter(settings.filter)
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
                location = geoCoordinatesOf(data.latitude, data.longitude)
        )
    }


    companion object {
        private val log = LoggerFactory.getLogger("SyncAdapter")
        private const val META_FILE = "meta.json"
        private const val DATA_FILE = "data.json"
    }

}