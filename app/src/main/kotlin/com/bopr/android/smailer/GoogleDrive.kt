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
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.StringWriter
import java.io.Writer
import kotlin.reflect.KClass

/**
 * Helper class to access Google drive.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
class GoogleDrive(private val context: Context) {

    private val log = LoggerFactory.getLogger("GoogleDrive")
    private lateinit var service: Drive

    fun login(account: Account) {
        val credential = GoogleAccountCredential
                .usingOAuth2(context, setOf(DRIVE_APPDATA))
                .setSelectedAccount(account)
        service = Drive.Builder(NetHttpTransport(), jacksonFactory(), credential)
                .setApplicationName("smailer")
                .build()
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
    fun <T : Any> download(filename: String, dataClass: KClass<out T>): T? {
        return open(filename)?.let {
            jacksonFactory().createJsonParser(it).parseAndClose(dataClass.java)
        }
    }

    @Throws(IOException::class)
    fun upload(filename: String, data: Any) {
        val writer: Writer = StringWriter()
        jacksonFactory().createJsonGenerator(writer).use {
            it.serialize(data)
            it.flush()
            write(filename, writer.toString())
        }
    }

    @Throws(IOException::class)
    fun delete(filename: String) {
        find(filename)?.let {
            service.files()
                    .delete(it)
                    .setFields("id")
                    .execute()

            log.debug("Deleted: $filename")
        }
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

    private fun jacksonFactory() = JacksonFactory.getDefaultInstance()

    companion object {

        private const val APP_DATA_FOLDER = "appDataFolder"
        private const val MIME_JSON = "text/json"
    }

}