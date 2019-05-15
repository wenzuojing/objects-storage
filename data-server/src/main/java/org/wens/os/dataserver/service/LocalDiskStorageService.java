package org.wens.os.dataserver.service;


import java.io.*;

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

}
