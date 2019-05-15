package org.wens.os.dataserver.service;


import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class LocalDiskStorageService implements StorageService{


    @Override
    public void write(InputStream inputStream, String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.getParentFile().exists()){
            file.getParentFile().mkdirs();
        }
        try( FileOutputStream fileOutputStream = new FileOutputStream(file)){
            IOUtils.copyLarge(inputStream,fileOutputStream);
        }

    }

    @Override
    public OutputStream read(String filePath) throws IOException {
        File file = new File(filePath);
        if(!file.exists()){
            return null;
        }
        return new FileOutputStream(file);
    }

    @Override
    public boolean remove(String filePath) throws IOException {

        File file = new File(filePath);
        if(!file.exists()){
            return false;
        }
        return file.delete() ;
    }

    @Override
    public boolean move(String srcFilePath, String toFilePath) throws IOException {
        Files.move(Paths.get(srcFilePath) , Paths.get(toFilePath) , StandardCopyOption.REPLACE_EXISTING );
        return true;
    }
}
