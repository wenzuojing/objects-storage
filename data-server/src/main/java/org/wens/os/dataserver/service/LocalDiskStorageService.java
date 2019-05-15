package org.wens.os.dataserver.service;


import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

@Service
public class LocalDiskStorageService implements StorageService{


    @Override
    public long write(InputStream inputStream, String key) throws IOException {
        return write(inputStream,key,false );

    }

    @Override
    public long write(InputStream inputStream, String key, boolean gzipCompress) throws IOException {
        File file = new File(key);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try(OutputStream outputStream = gzipCompress ? new GZIPOutputStream(new FileOutputStream(file) ) : new FileOutputStream(file)){
            return IOUtils.copyLarge(inputStream,outputStream);
        }
    }

    @Override
    public InputStream read(String key) throws IOException {
        return read(key,false);
    }

    @Override
    public InputStream read(String key, boolean gzipUncompress) throws IOException {
        File file = new File(key);
        if(!file.exists()){
            return null;
        }
        return gzipUncompress ? new GZIPInputStream(new FileInputStream(file)):new FileInputStream(file);
    }

    @Override
    public boolean remove(String key) throws IOException {

        File file = new File(key);
        if(!file.exists()){
            return false;
        }
        return file.delete() ;
    }

    @Override
    public boolean move(String srcKey, String toKey) throws IOException {
        Files.move(Paths.get(srcKey) , Paths.get(toKey) , StandardCopyOption.REPLACE_EXISTING );
        return true;
    }

    @Override
    public long size(String key) throws IOException {
        File file = new File(key);
        if(!file.exists()){
            return -1l;
        }
        return file.length() ;
    }

}
