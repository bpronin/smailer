package com.bopr.android.smailer.sync

import android.accounts.Account
import android.content.Context
import com.bopr.android.smailer.data.Database
import com.bopr.android.smailer.util.GeoLocation.Companion.fromCoordinates
import com.bopr.android.smailer.provider.telephony.PhoneCallInfo
import com.bopr.android.smailer.external.GoogleDrive
import com.bopr.android.smailer.util.Logger
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
                    textBlacklist = textBlacklist,
                    textWhitelist = textWhitelist,
                    phoneCalls = phoneCalls.map(::phoneCallToData)
            )
        }
    }

    private fun putLocalData(data: SyncData) {
        database.commit(false) {
            batch {
                phoneCalls.replaceAll(data.phoneCalls.map(::dataToPhoneCall))
                phoneBlacklist.replaceAll(data.phoneBlacklist)
                phoneWhitelist.replaceAll(data.phoneWhitelist)
                textBlacklist.replaceAll(data.textBlacklist)
                textWhitelist.replaceAll(data.textWhitelist)
            }
        }
    }

    private fun phoneCallToData(info: PhoneCallInfo): SyncData.PhoneCall {
        return SyncData.PhoneCall(
                incoming = info.isIncoming,
                missed = info.isMissed,
                phone = info.phone,
                recipient = info.acceptor,
                startTime = info.startTime,
                endTime = info.endTime,
                text = info.text,
                details = info.details,
                latitude = info.location?.latitude,
                longitude = info.location?.longitude,
                state = info.processState,
                processStatus = info.bypassFlags,
                processTime = info.processTime,
                isRead = info.isRead
        )
    }

    private fun dataToPhoneCall(data: SyncData.PhoneCall): PhoneCallInfo {
        return PhoneCallInfo(
                phone = data.phone,
                isIncoming = data.incoming,
                startTime = data.startTime,
                endTime = data.endTime,
                isMissed = data.missed,
                text = data.text,
                location = fromCoordinates(data.latitude, data.longitude),
                details = data.details,
                processState = data.state,
                acceptor = data.recipient,
                isRead = data.isRead,
                bypassFlags = data.processStatus,
                processTime = data.processTime
        )
    }

    companion object {

        private val log = Logger("Synchronizer")

        const val SYNC_NORMAL = 0
        const val SYNC_FORCE_DOWNLOAD = 1
        const val SYNC_FORCE_UPLOAD = 2
    }
}