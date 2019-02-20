package com.bopr.android.smailer.mail;

import java.io.File;
import java.util.Collection;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public interface MailTransport {

    /**
     * Starts new delivery session.
     */
    void startSession(@NonNull String sender, @NonNull String password, @NonNull String host, @NonNull String port);

    /**
     * Sends email with attachment.
     */
    void send(String subject, String body, @Nullable Collection<File> attachment, @NonNull String recipients) throws Exception;
}
