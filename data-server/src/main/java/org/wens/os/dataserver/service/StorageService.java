package org.wens.os.dataserver.service;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author wens
 */
public interface StorageService {

    long write(InputStream inputStream , String key ) throws IOException ;

    long write(InputStream inputStream , String key , boolean gzipCompress ) throws IOException ;

    InputStream read(String key) throws IOException ;

    InputStream read(String key,boolean gzipUncompress) throws IOException ;

    boolean remove(String key) throws IOException ;

    boolean move(String srcKey , String toKey ) throws IOException;

    long size(String key)  throws IOException ;




}
