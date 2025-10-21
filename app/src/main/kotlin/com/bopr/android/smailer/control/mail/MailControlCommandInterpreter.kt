package com.bopr.android.smailer.control.mail

import com.bopr.android.smailer.control.ControlCommand
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_PHONE_TO_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_PHONE_TO_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_TEXT_TO_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.ADD_TEXT_TO_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_PHONE_FROM_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_PHONE_FROM_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_TEXT_FROM_BLACKLIST
import com.bopr.android.smailer.control.ControlCommand.Action.REMOVE_TEXT_FROM_WHITELIST
import com.bopr.android.smailer.control.ControlCommand.Action.SEND_SMS_TO_CALLER
import com.bopr.android.smailer.util.PHONE_REGEX
import com.bopr.android.smailer.util.QUOTATION_PATTERN
import com.bopr.android.smailer.util.QUOTATION_REGEX
import java.util.Locale
import java.util.Scanner
import java.util.regex.Pattern

/**
 * Parses email body into control command.
 * For example: "To device My Device. Add phone +1234567901 to blacklist."
 *
 * @author Boris Pronin ([boris280471@gmail.com](mailto:boris280471@gmail.com))
 */
internal class MailControlCommandInterpreter {

    fun interpret(text: String): ControlCommand? {
        val scanner = Scanner(text).useDelimiter("\\W+")
        if (hasNextToken(scanner, "(?i:DEVICE)")) {
            val command = ControlCommand(nextQuoted(scanner))
            when (nextToken(scanner, "(?i:ADD|PUT|REMOVE|DELETE|SEND)")) {
                "SEND" ->
                    if (hasNextToken(scanner, "(?i:SMS)")) {
                        command.action = SEND_SMS_TO_CALLER
                        command.arguments["text"] = nextQuoted(scanner)
                        command.arguments["phone"] = nextPhone(scanner)
                    }

                "ADD", "PUT" -> {
                    when (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                        "PHONE" -> {
                            command.argument = nextPhone(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> command.action = ADD_PHONE_TO_BLACKLIST
                                "WHITELIST" -> command.action = ADD_PHONE_TO_WHITELIST
                            }
                        }

                        "TEXT" -> {
                            command.argument = nextQuoted(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> command.action = ADD_TEXT_TO_BLACKLIST
                                "WHITELIST" -> command.action = ADD_TEXT_TO_WHITELIST
                            }
                        }
                    }
                    when (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                        "PHONE" -> {
                            command.argument = nextPhone(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> command.action = REMOVE_PHONE_FROM_BLACKLIST
                                "WHITELIST" -> command.action = REMOVE_PHONE_FROM_WHITELIST
                            }
                        }

                        "TEXT" -> {
                            command.argument = nextQuoted(scanner)
                            when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                                "BLACKLIST" -> command.action = REMOVE_TEXT_FROM_BLACKLIST
                                "WHITELIST" -> command.action = REMOVE_TEXT_FROM_WHITELIST
                            }
                        }
                    }
                }

                "REMOVE", "DELETE" -> when (nextToken(scanner, "(?i:PHONE|TEXT)")) {
                    "PHONE" -> {
                        command.argument = nextPhone(scanner)
                        when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                            "BLACKLIST" -> command.action = REMOVE_PHONE_FROM_BLACKLIST
                            "WHITELIST" -> command.action = REMOVE_PHONE_FROM_WHITELIST
                        }
                    }

                    "TEXT" -> {
                        command.argument = nextQuoted(scanner)
                        when (nextToken(scanner, "(?i:BLACKLIST|WHITELIST)")) {
                            "BLACKLIST" -> command.action = REMOVE_TEXT_FROM_BLACKLIST
                            "WHITELIST" -> command.action = REMOVE_TEXT_FROM_WHITELIST
                        }
                    }
                }
            }
            return command
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
                ?: text                 /* found pure number */
        }
    }

    companion object {

        private val PHONE_OR_QUOTATION_PATTERN: Pattern =
            Pattern.compile("($QUOTATION_REGEX|$PHONE_REGEX)")
    }
}