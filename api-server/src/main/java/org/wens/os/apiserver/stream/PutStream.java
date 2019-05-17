package org.wens.os.apiserver.stream;

import com.alibaba.fastjson.JSONObject;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.Response;
import okio.BufferedSink;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wens.os.common.OSException;
import org.wens.os.common.http.OKHttps;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

/**
 * @author wens
 */
public class PutStream {

    final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private String server;

    private OutputStream outputStream;

    private Future<WriteResult> writeResultFuture;


    public PutStream(String server) {
        this.server = server;
        PipedOutputStream output;
        PipedInputStream input;
        try {
            output = new PipedOutputStream();
            input = new PipedInputStream(output);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        this.outputStream = output;
        this.writeResultFuture = executorService.submit(() -> {
            try (Response response = OKHttps.post(String.format("http://%s/temp", server), new RequestBody() {
                @Override
                public MediaType contentType() {
                    return MediaType.get("application/octet-stream");
                }

                @Override
                public void writeTo(BufferedSink bufferedSink) throws IOException {
                    IOUtils.copy(input, bufferedSink.outputStream());
                }
            })) {
                if (response.code() != 200) {
                    return null;
                }
                return JSONObject.parseObject(response.body().string(), WriteResult.class);
            }
        });

    }

    public void write(byte[] b, int off, int len) throws IOException {
        outputStream.write(b, off, len);
    }

    public void write(InputStream inputStream) throws IOException {
        byte[] b = new byte[1024 * 4];
        while (true) {
            int n = inputStream.read(b, 0, b.length);
            if (n == -1) {
                break;
            }
            write(b, 0, n);
        }
    }


    public boolean commit(String name) throws IOException {

        WriteResult writeResult = getWriteResult();
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

    public boolean rollback() throws IOException {
        WriteResult writeResult = getWriteResult();
        if (writeResult == null) {
            throw new IllegalStateException("write result is null");
        }

        try (Response response = OKHttps.delete(String.format("http://%s/temp/%s", server, writeResult.uuid))) {
            if (response.code() != 200) {
                return true;
            } else {
                return false;
            }
        }
    }

    private WriteResult getWriteResult() throws IOException {
        outputStream.close();
        WriteResult writeResult;
        try {
            writeResult = writeResultFuture.get();
        } catch (Throwable e) {
            throw new OSException(e);
        }
        return writeResult;
    }

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
}
