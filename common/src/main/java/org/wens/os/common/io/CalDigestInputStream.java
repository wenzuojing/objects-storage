package org.wens.os.common.io;

import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author wens
 */
public class CalDigestInputStream extends InputStream {

    private InputStream innerInputStream ;

    private  MessageDigest messageDigest ;

    public CalDigestInputStream(InputStream inputStream , MessageDigest messageDigest ){
        this.innerInputStream = inputStream ;
        this.messageDigest = messageDigest ;
    }


    @Override
    public int read() throws IOException {
        int r = innerInputStream.read();
        if( r != -1 ){
            messageDigest.update( (byte) r );
        }
        return r ;
    }

    @Override
    public int read(byte[] b) throws IOException {
        int r  = innerInputStream.read(b);
        if( r != -1 ){
            messageDigest.update( b , 0 , r );
        }
        return r ;
    }

    @Override
    public int read(byte[] b, int off, int len) throws IOException {
        int r  = innerInputStream.read(b , off ,len);
        if( r != -1 ){
            messageDigest.update( b , off , r );
        }
        return r ;
    }


    @Override
    public long skip(long n) throws IOException {
        return innerInputStream.skip(n);
    }

    @Override
    public int available() throws IOException {
        return innerInputStream.available();
    }

    @Override
    public void close() throws IOException {
        innerInputStream.close();
    }

    @Override
    public synchronized void mark(int readlimit) {
        innerInputStream.mark(readlimit);
    }

    @Override
    public synchronized void reset() throws IOException {
        innerInputStream.reset();
    }

    @Override
    public boolean markSupported() {
        return innerInputStream.markSupported();
    }
}
