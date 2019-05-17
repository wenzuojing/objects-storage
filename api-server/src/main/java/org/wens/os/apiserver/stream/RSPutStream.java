package org.wens.os.apiserver.stream;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.wens.os.apiserver.util.ReedSolomonUtils;
import org.wens.os.common.OSException;
import org.wens.os.common.io.CalDigestInputStream;
import org.wens.os.common.util.MessageDisgestUtils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.security.MessageDigest;
import java.util.List;
import java.util.stream.Collectors;

import static org.wens.os.apiserver.stream.RSConfig.*;

/**
 * @author wens
 */
public class RSPutStream {

    public static class WriteResult {

        public final String checksum;

        public final long size;


        public WriteResult(String checksum, long size) {
            this.checksum = checksum;
            this.size = size;
        }
    }


    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private List<PutStream> putStreams;

    private WriteResult writeResult;

    public RSPutStream(List<String> servers) {
        if (servers.size() != DATA_SHARDS + PARITY_SHARDS) {
            throw new OSException("expect " + (DATA_SHARDS + PARITY_SHARDS) + " server,but " + servers.size());
        }
        this.putStreams = servers.stream().map(s -> new PutStream(s)).collect(Collectors.toList());
    }

    public WriteResult write(InputStream srcInputStream) throws IOException {
        MessageDigest messageDigest = MessageDisgestUtils.sha256();
        srcInputStream = new CalDigestInputStream(srcInputStream, messageDigest);

        byte[] block = new byte[BLOCK_SIZE];

        long size = 0;

        while (true) {

            int n = IOUtils.read(srcInputStream,block,0,block.length);
            if (n == 0) {
                break;
            }
            size += n;
            byte[] bytes;
            if (n != block.length) {
                bytes = new byte[n];
                System.arraycopy(block, 0, bytes, 0, n);
            } else {
                bytes = block;
            }
            byte[][] shards = ReedSolomonUtils.encode(bytes, DATA_SHARDS, PARITY_SHARDS);
            for (int i = 0; i < DATA_SHARDS + PARITY_SHARDS; i++) {
                byte[] intBytes = new byte[4];
                ByteBuffer.wrap(intBytes).putInt(shards[i].length);
                System.err.println( i+ " " + shards[i].length);
                putStreams.get(i).write(intBytes, 0, intBytes.length);
                putStreams.get(i).write(shards[i], 0, shards[i].length);
            }
        }
        writeResult = new WriteResult(String.format("sha256-%s", Hex.encodeHexString(messageDigest.digest())), size);
        return writeResult;
    }


    public boolean commit() throws IOException {
        if (writeResult == null) {
            throw new IllegalStateException("write result is null");
        }
        String name = writeResult.checksum.split("-")[1];
        boolean allSuccess = true;
        for (int i = 0; i < putStreams.size(); i++) {
            boolean b = putStreams.get(i).commit(String.format("%s.%s", name, i));
            if (!b) {
                log.warn("commit fail. shard index is {}", i);
                allSuccess = false;
                break;
            }
        }
        return allSuccess;
    }

    public boolean rollback() throws IOException {
        if (writeResult == null) {
            throw new IllegalStateException("write result is null");
        }

        boolean allSuccess = true;
        for (int i = 0; i < putStreams.size(); i++) {
            boolean b = putStreams.get(i).rollback();
            if (!b) {
                log.warn("rollback fail. shard index is {}", i);
                allSuccess = false;
            }
        }
        return allSuccess;
    }

}
