package org.wens.os.apiserver.stream;

public class RSConfig {

    public static final int DATA_SHARDS = 1;
    public static final int PARITY_SHARDS = 1;

    public static final int BLOCK_SIZE = 1024 * 1024 * 2 * DATA_SHARDS ;
}
