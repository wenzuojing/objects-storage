package org.wens.os.dataserver.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.nio.file.Paths;

@Service
public class StorageInstanceService {

    @Value("${storage.root}")
    private String storageRoot;

    public StorageService tempStorageService;

    public StorageService garbageStorageService;

    public StorageService objectsStorageService;

    @PostConstruct
    public void init() {
        tempStorageService = new LocalDiskStorageService(Paths.get(storageRoot, "temp").toString());
        garbageStorageService = new LocalDiskStorageService(Paths.get(storageRoot, "garbage").toString());
        objectsStorageService = new LocalDiskStorageService(Paths.get(storageRoot, "objects").toString());
    }

    public StorageService getTempStorageService() {
        return tempStorageService;
    }

    public StorageService getGarbageStorageService() {
        return garbageStorageService;
    }

    public StorageService getObjectsStorageService() {
        return objectsStorageService;
    }
}
