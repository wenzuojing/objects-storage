package org.wens.os.common.io;

import java.io.IOException;
import java.io.OutputStream;
import java.security.MessageDigest;

/**
 * @author wens
 */
public class CalDigestOutputStream extends OutputStream {

    private OutputStream innerOutputStream;

    private MessageDigest messageDigest;

    public CalDigestOutputStream(OutputStream outputStream, MessageDigest messageDigest) {
        this.innerOutputStream = outputStream;
        this.messageDigest = messageDigest;
    }

    @Override
    public void write(byte[] b) throws IOException {
        innerOutputStream.write(b);
        messageDigest.update(b);
    }

    @Override
    public void write(byte[] b, int off, int len) throws IOException {
        innerOutputStream.write(b, off, len);
        messageDigest.update(b, off, len);
    }

    @Override
    public void flush() throws IOException {
        innerOutputStream.flush();
    }

    @Override
    public void close() throws IOException {
        innerOutputStream.close();
    }

    @Override
    public void write(int b) throws IOException {
        innerOutputStream.write(b);
        messageDigest.update((byte) b);
    }
}
