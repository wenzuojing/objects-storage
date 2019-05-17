package org.wens.os.dataserver.service;


import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class LocalDiskStorageService implements StorageService {

    private String storageDir;

    public LocalDiskStorageService(String storageDir) {
        this.storageDir = storageDir;
    }

    @Override
    public OutputStream write(String key) throws IOException {
        File file = new File(storageDir, key);
        if (!file.getParentFile().exists()) {
            file.getParentFile().mkdirs();
        }
        return new FileOutputStream(file);
    }

    @Override
    public InputStream read(String key) throws IOException {
        File file = new File(storageDir, key);
        if (!file.exists()) {
            return null;
        }
        return new FileInputStream(file);
    }


    @Override
    public boolean remove(String key) throws IOException {

        File file = new File(storageDir, key);
        if (!file.exists()) {
            return false;
        }
        return file.delete();
    }


    @Override
    public long size(String key) throws IOException {
        File file = new File(storageDir, key);
        if (!file.exists()) {
            return -1l;
        }
        return file.length();
    }

    @Override
    public List<Key> list(Predicate<Key> predicate) {
        List<Key> keys = new ArrayList<>();
        File file = new File(storageDir);

        if(!file.exists()){
            file.mkdirs();
        }

        for(File f : file.listFiles()){
            keys.add(new Key(f.getName(),f.length(),f.lastModified()));
        }

        if( predicate == null ){
            return keys ;
        }
        return keys.stream().filter(predicate).collect(Collectors.toList());
    }

}
