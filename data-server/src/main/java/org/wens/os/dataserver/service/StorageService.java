package org.wens.os.dataserver.service;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface StorageService {

    void write(InputStream inputStream , String filePath) throws IOException ;

    OutputStream read(String filePath) throws IOException ;

    boolean remove(String filePath) throws IOException ;

    boolean move(String srcFilePath , String toFilePath ) throws IOException;



}
