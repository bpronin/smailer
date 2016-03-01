package com.bopr.android.smailer.util.mail;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.activation.DataSource;

/**
 * Class ByteArrayDataSource.
 *
 * @author Boris Pronin (<a href="mailto:boprsoft.dev@gmail.com">boprsoft.dev@gmail.com</a>)
 */
class ByteArrayDataSource implements DataSource {

    private byte[] data;

    public ByteArrayDataSource(byte[] data) {
        this.data = data;
    }

    @Override
    public String getContentType() {
        return "text/plain";
    }

    @Override
    public InputStream getInputStream() throws IOException {
        return new ByteArrayInputStream(data);
    }

    @Override
    public String getName() {
        return "ByteArrayDataSource";
    }

    @Override
    public OutputStream getOutputStream() throws IOException {
        throw new IOException("Not Supported");
    }
}
