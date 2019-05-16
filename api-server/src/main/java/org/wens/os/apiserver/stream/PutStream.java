package org.wens.os.apiserver.stream;

import com.alibaba.fastjson.JSONObject;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.wens.os.common.OSException;
import org.wens.os.common.http.OKHttps;
import org.wens.os.common.io.CalDigestInputStream;
import org.wens.os.common.util.MessageDisgestUtils;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;

/**
 * @author wens
 */
public class PutStream implements Closeable  {

    public static class WriteResult {

        public final String checksum;

        public final long size;

        public final String uuid;

        public WriteResult(String checksum, long size, String uuid) {
            this.checksum = checksum;
            this.size = size;
            this.uuid = uuid;
        }
    }

    private String server;

    private WriteResult writeResult;

    public PutStream(String server) {
        this.server = server;
    }

    public WriteResult write(InputStream srcInputStream) throws IOException {
        MessageDigest messageDigest = MessageDisgestUtils.sha256();
        InputStream inputStream = new CalDigestInputStream(srcInputStream,messageDigest);
        try (Response response = OKHttps.post(String.format("http://%s/temp", server), new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.get("application/octet-stream");
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                IOUtils.copy(inputStream, bufferedSink.outputStream());
            }
        })) {
            if (response.code() != 200) {
                throw new IOException("write stream fail.");
            }
            WriteResult result = JSONObject.parseObject(response.body().string(), WriteResult.class);
            if(result.checksum.indexOf(Hex.encodeHexString(messageDigest.digest())) == -1 ){
                throw  new OSException("checksum mismatch");
            }
            this.writeResult = result ;
            return result ;
        }
    }


    public boolean commit(String name) throws IOException {
        if (writeResult == null) {
            throw new IllegalStateException("write result is null");
        }
        try (Response response = OKHttps.put(String.format("http://%s/temp/%s", server, writeResult.uuid), new FormBody.Builder().add("name", name).build())) {
            if (response.code() != 200) {
                return false;
            } else {
                return true;
            }
        }
    }

    public boolean delete() throws IOException {
        if (writeResult == null) {
            throw new IllegalStateException("write result is null");
        }
        try (Response response = OKHttps.delete(String.format("http://%s/temp/%s", server, writeResult.uuid))) {
            if (response.code() != 200) {
                return false;
            } else {
                return true;
            }
        }
    }

    @Override
    public void close() throws IOException {

    }
}
