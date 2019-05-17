package org.wens.os.apiserver.util;

import com.backblaze.erasure.ReedSolomon;

import java.nio.ByteBuffer;

/**
 * @author wens
 */
public class ReedSolomonUtils {

    public static byte[][] encode(byte[] bytes, int ds, int ps) {
        int shardSize = (bytes.length + 4 + ds - 1) / ds;
        byte[][] shards = new byte[ds + ps][shardSize];

        byte[] allBytes = new byte[shardSize * ds];
        ByteBuffer.wrap(allBytes).putInt(bytes.length);
        System.arraycopy(bytes, 0, allBytes, 4, bytes.length);


        for (int i = 0; i < ds; i++) {
            System.arraycopy(allBytes, i * shardSize, shards[i], 0, shardSize);
        }

        ReedSolomon reedSolomon = ReedSolomon.create(ds, ps);
        reedSolomon.encodeParity(shards, 0, shardSize);
        return shards;
    }

    public static byte[] decode(byte[][] shards, int ds, int ps) {
        int shardSize = -1;
        final boolean[] shardPresent = new boolean[shards.length];
        for (int i = 0; i < shards.length; i++) {
            if (shards[i] != null) {
                shardSize = shards[i].length;
                shardPresent[i] = true;
            }
        }

        for (int i = 0; i < shards.length; i++) {
            if (shards[i] == null) {
                shards[i] = new byte[shardSize];
            }
        }

        // Use Reed-Solomon to fill in the missing shards
        ReedSolomon reedSolomon = ReedSolomon.create(ds, ps);
        reedSolomon.decodeMissing(shards, shardPresent, 0, shardSize);

        byte[] allBytes = new byte[shardSize * ds];

        for (int i = 0; i < ds; i++) {
            System.arraycopy(shards[i], 0, allBytes, i * shardSize, shardSize);
        }
        int n = ByteBuffer.wrap(allBytes).getInt();
        byte[] bytes = new byte[n];
        System.arraycopy(allBytes, 4, bytes, 0, n);
        return bytes;
    }

    public static void main(String[] args) {

        int ds = 3;

        int ps = 2;

        for (int i = 10; i < 100000; i++) {

            StringBuilder sb = new StringBuilder();


            for (int j = 0; j < i; j++) {
                sb.append("W");
            }

            byte[] bytes = sb.toString().getBytes();
            byte[] bytes1 = decode(encode(bytes, ds, ps), ds, ps);
            String s = new String(bytes1);

            if (bytes1.length != bytes.length || !sb.toString().equals(s)) {
                System.out.println("My God" + i);
            }
        }


    }
}
