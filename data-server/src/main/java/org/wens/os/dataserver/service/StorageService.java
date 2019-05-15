package org.wens.os.dataserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * @author wens
 */
public interface StorageService {

    OutputStream write(String key) throws IOException;

    InputStream read(String key) throws IOException;

    boolean remove(String key) throws IOException;

    long size(String key) throws IOException;


}
