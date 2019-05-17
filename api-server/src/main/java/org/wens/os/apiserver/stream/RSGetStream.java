package org.wens.os.apiserver.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wens.os.apiserver.util.ReedSolomonUtils;
import org.wens.os.common.OSNotFoundException;
import org.wens.os.locate.LocateService;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.wens.os.apiserver.stream.RSConfig.DATA_SHARDS;
import static org.wens.os.apiserver.stream.RSConfig.PARITY_SHARDS;

/**
 * @author wens
 */
public class RSGetStream implements Closeable {

    final static ExecutorService executorService = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 10);

    private Logger log = LoggerFactory.getLogger(this.getClass());

    private GetStream[] getStreams;

    private InputStream inputStream;

    private String name;

    private LocateService locateService;

    public RSGetStream(String name, LocateService locateService) throws IOException {

        this.name = name;
        this.locateService = locateService;
        this.getStreams = new GetStream[DATA_SHARDS + PARITY_SHARDS];
        this.init();
    }

    private void init() throws IOException {
        List<String> names = new ArrayList<>(DATA_SHARDS + PARITY_SHARDS);
        for (int i = 0; i < (DATA_SHARDS + PARITY_SHARDS); i++) {
            names.add(String.format("%s.%s", name, i));
        }

        Map<String, String> locateResults = locateService.locate(names, DATA_SHARDS);

        if (locateResults.size() < DATA_SHARDS) {
            throw new OSNotFoundException("can not find object stream.");
        }

        for (int i = 0; i < (DATA_SHARDS + PARITY_SHARDS); i++) {
            String shardName = String.format("%s.%s", name, i);
            String server = locateResults.get(shardName);

            if (server != null) {
                getStreams[i] = new GetStream(server, shardName);
            }
        }

        PipedOutputStream output = new PipedOutputStream();
        inputStream = new PipedInputStream(output);

        executorService.submit(() -> {
            try {
                while (true) {
                    byte[][] shards = new byte[DATA_SHARDS + PARITY_SHARDS][];
                    boolean finish = false;
                    for (int i = 0; i < getStreams.length; i++) {
                        if (getStreams[i] != null) {
                            //read size
                            byte[] intBytes = new byte[4];
                            int r = getStreams[i].read(intBytes, 0, intBytes.length);
                            if (r == 0) {
                                finish = true;
                                break;
                            }
                            int len = ByteBuffer.wrap(intBytes).getInt() ;
                            System.err.println(i +" "+ len);
                            shards[i] = new byte[len];
                            getStreams[i].read(shards[i], 0, shards[i].length);
                        }
                    }

                    if (finish) {
                        break;
                    }

                    byte[] bytes = ReedSolomonUtils.decode(shards, DATA_SHARDS, PARITY_SHARDS);
                    output.write(bytes);
                }
            } catch (Throwable t) {
                log.error("read shard stream fail.", t);
            } finally {
                try {
                    output.close();
                } catch (IOException e) {
                    //
                }
            }
        });
    }

    public InputStream read() throws IOException {
        return inputStream;
    }

    @Override
    public void close() throws IOException {
        for (int i = 0; i < getStreams.length; i++) {
            if (getStreams[i] == null) {
                continue;
            }
            try {
                getStreams[i].close();
            } catch (Throwable t) {

            }
        }

    }
}
