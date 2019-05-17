package org.wens.os.apiserver.stream;

import okhttp3.Response;
import org.apache.commons.io.IOUtils;
import org.wens.os.common.OSNotFoundException;
import org.wens.os.common.http.OKHttps;

import java.io.Closeable;
import java.io.IOException;

/**
 * @author wens
 */
public class GetStream implements Closeable {

    private Response response;

    public GetStream(String server, String name) throws IOException {
        response = OKHttps.get(String.format("http://%s/objects/%s", server, name));
        if (response.code() == 404) {
            throw new OSNotFoundException("can not find stream");
        }
        if (response.code() != 200) {
            throw new IOException("read fail");
        }
    }

    public int read(byte[] b, int off, int len) throws IOException {
        return IOUtils.read(response.body().byteStream(),b, off, len);
    }

    @Override
    public void close() throws IOException {
        if (response != null) {
            response.close();
        }
    }
}
