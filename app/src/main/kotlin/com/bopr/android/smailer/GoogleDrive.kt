package com.bopr.android.smailer

import android.accounts.Account
import android.content.Context
import com.google.api.client.googleapis.extensions.android.gms.auth.GoogleAccountCredential
import com.google.api.client.http.ByteArrayContent
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.jackson2.JacksonFactory
import com.google.api.services.drive.Drive
import com.google.api.services.drive.DriveScopes.DRIVE_APPDATA
import com.google.api.services.drive.model.File
import com.google.common.collect.ImmutableList
import com.google.common.collect.ImmutableSet
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.io.Writer

/**
 * Helper class to access Google drive.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GoogleDrive(context: Context, account: Account) {

    private val service: Drive

    init {
        val credential = GoogleAccountCredential.usingOAuth2(context, ImmutableSet.of(DRIVE_APPDATA))
                .setSelectedAccount(account)
        service = Drive.Builder(NetHttpTransport(),
                JacksonFactory.getDefaultInstance(), credential)
                .setApplicationName("smailer")
                .build()
    }

    @Throws(IOException::class)
    private fun open(filename: String): InputStream? {
        return find(filename)?.let {
            service.files()[it].executeMediaAsInputStream()
        }
    }

    @Throws(IOException::class)
    private fun write(filename: String, json: String) {
        find(filename)?.let {
            update(it, filename, json)
        } ?: create(filename, json)
    }

    @Throws(IOException::class)
    private fun create(filename: String, json: String) {
        val metadata = File()
                .setParents(ImmutableList.of(APP_DATA_FOLDER))
                .setMimeType(MIME_JSON)
                .setName(filename)
        val content = ByteArrayContent.fromString(MIME_JSON, json)
        service.files()
                .create(metadata, content)
                .setFields("id")
                .execute()
    }

    @Throws(IOException::class)
    private fun update(fileId: String, filename: String, json: String) {
        val metadata = File().setName(filename)
        val content = ByteArrayContent.fromString(MIME_JSON, json)
        service.files()
                .update(fileId, metadata, content)
                .execute()
    }

    @Throws(IOException::class)
    private fun find(filename: String): String? {
        val files = service.files()
                .list()
                .setSpaces(APP_DATA_FOLDER)
                .setQ("name='$filename'")
                .setFields("files(id)")
                .execute()
                .files

        if (files.isNotEmpty()) {
            if (files.size > 1) {
                log.warn("Multiple entries found")
            }
            return files[0].id
        }
        return null
    }

    @Throws(IOException::class)
    fun delete(filename: String) {
        find(filename)?.let {
            service.files()
                    .delete(it)
                    .setFields("id")
                    .execute()
        }

        log.debug("Deleted: $filename")
    }

    @Throws(IOException::class)
    fun clear() {
        for (file in list()) {
            service.files()
                    .delete(file.id)
                    .execute()
        }

        log.debug("All data removed")
    }

    @Throws(IOException::class)
    fun list(): List<File> {
        return service.files().list()
                .setSpaces(APP_DATA_FOLDER)
                .setFields("files(id, name)")
                .execute()
                .files
    }

    @Throws(IOException::class)
    fun upload(filename: String, data: Any) {
        val writer: Writer = StringWriter()
        val generator = JacksonFactory.getDefaultInstance().createJsonGenerator(writer)
        generator.serialize(data)
        generator.flush()
        write(filename, writer.toString())
        generator.close()
    }

    @Throws(IOException::class)
    fun <T> download(filename: String, dataClass: Class<out T?>): T? {
        return open(filename)?.let {
            JacksonFactory.getDefaultInstance().createJsonParser(it).parseAndClose(dataClass)
        }
    }

    companion object {
        private val log = LoggerFactory.getLogger("GoogleDrive")

        private const val APP_DATA_FOLDER = "appDataFolder"
        private const val MIME_JSON = "text/json"
    }

}