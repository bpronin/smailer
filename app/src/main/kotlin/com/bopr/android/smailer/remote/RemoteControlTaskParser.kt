package com.bopr.android.smailer.remote

import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.remote.RemoteControlTask.Companion.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.util.PHONE_REGEX
import com.bopr.android.smailer.util.QUOTATION_PATTERN
import com.bopr.android.smailer.util.QUOTATION_REGEX
import java.util.Locale
import java.util.Scanner
import java.util.regex.Pattern

/**
 * Parses text into remote control task.
 *
 * @author Boris Pronin ([boprsoft.dev@gmail.com](mailto:boprsoft.dev@gmail.com))
 */
internal class RemoteControlTaskParser {

    fun parse(text: String): RemoteControlTask? {
        val scanner = Scanner(text).useDelimiter("\\W+")
        if (hasNextToken(scanner, "(?i:DEVICE)")) {
            val task = RemoteControlTask(nextQuoted(scanner))
            when (nextToken(scanner, "(?i:ADD|PUT|REMOVE|DELETE|SEND)")) {
                "SEND" ->
                    if (hasNextToken(scanner, "(?i:SMS)")) {
                        task.action = SEND_SMS_TO_CALLER
                        task.arguments["text"] = nextQuoted(scanner)
                        task.arguments["phone"] = nextPhone(scanner)
                    }

                "ADD", "PUT" -> {
                    when (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                        "PHONE" -> {
                            task.argument = nextPhone(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> task.action = ADD_PHONE_TO_BLACKLIST
                                "WHITELIST" -> task.action = ADD_PHONE_TO_WHITELIST
                            }
                        }

                        "TEXT" -> {
                            task.argument = nextQuoted(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> task.action = ADD_TEXT_TO_BLACKLIST
                                "WHITELIST" -> task.action = ADD_TEXT_TO_WHITELIST
                            }
                        }
                    }
                    when (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                        "PHONE" -> {
                            task.argument = nextPhone(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> task.action = REMOVE_PHONE_FROM_BLACKLIST
                                "WHITELIST" -> task.action = REMOVE_PHONE_FROM_WHITELIST
                            }
                        }

                        "TEXT" -> {
                            task.argument = nextQuoted(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> task.action = REMOVE_TEXT_FROM_BLACKLIST
                                "WHITELIST" -> task.action = REMOVE_TEXT_FROM_WHITELIST
                            }
                        }
                    }
                }

                "REMOVE", "DELETE" -> when (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                    "PHONE" -> {
                        task.argument = nextPhone(scanner)
                        when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                            "BLACKLIST" -> task.action = REMOVE_PHONE_FROM_BLACKLIST
                            "WHITELIST" -> task.action = REMOVE_PHONE_FROM_WHITELIST
                        }
                    }

                    "TEXT" -> {
                        task.argument = nextQuoted(scanner)
                        when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                            "BLACKLIST" -> task.action = REMOVE_TEXT_FROM_BLACKLIST
                            "WHITELIST" -> task.action = REMOVE_TEXT_FROM_WHITELIST
                        }
                    }
                }
            }
            return task
        }
        return null
    }

    private fun hasNextToken(scanner: Scanner, pattern: String): Boolean {
        return scanner.findWithinHorizon(pattern, 0) != null
    }

    private fun nextToken(scanner: Scanner, pattern: String): String {
        return scanner.findWithinHorizon(pattern, 0).uppercase(Locale.ROOT)
    }

    private fun nextQuoted(scanner: Scanner): String? {
        return scanner.findWithinHorizon(QUOTATION_PATTERN, 0)?.let {
            scanner.match().group(1)
        }
    }

    private fun nextPhone(scanner: Scanner): String? {
        return scanner.findWithinHorizon(PHONE_OR_QUOTATION_PATTERN, 0)?.let { text ->
            scanner.match().group(2)    /* found quotation */
                ?: text               /* found pure number */
        }
    }

    companion object {

        private val PHONE_OR_QUOTATION_PATTERN: Pattern =
            Pattern.compile("($QUOTATION_REGEX|$PHONE_REGEX)")
    }
}