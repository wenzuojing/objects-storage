package org.wens.os.dataserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.function.Predicate;

/**
 * @author wens
 */
public interface StorageService {

    class Key {
        public final String key;

        public final long size;

        public final long lastmodify;

        public Key(String key, long size, long lastmodify) {
            this.key = key;
            this.size = size;
            this.lastmodify = lastmodify;
        }
    }

    OutputStream write(String key) throws IOException;

    InputStream read(String key) throws IOException;

    boolean remove(String key) throws IOException;

    long size(String key) throws IOException;

    List<Key> list(Predicate<Key> predicate);

}
