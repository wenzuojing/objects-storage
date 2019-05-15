package org.wens.os.dataserver.service;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import org.wens.os.common.util.UUIDS;

import javax.annotation.Resource;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;

@RunWith(SpringRunner.class)
@SpringBootTest
public class LocalDiskStorageServiceTest {

    @Resource
    private LocalDiskStorageService localDiskStorageService ;

    private String dir  = UUIDS.uuid() ;

    @Before
    public void before(){
        File file = Paths.get(System.getProperty("java.io.tmpdir"), dir).toFile();
        if(!file.exists()){
            file.mkdir();
        }
    }

    @After
    public void after() throws IOException {
        File file = Paths.get(System.getProperty("java.io.tmpdir"), dir).toFile();
        FileUtils.deleteDirectory(file);
    }




    @Test
    public void test_1() throws IOException {

        Path root  = Paths.get(System.getProperty("java.io.tmpdir"), dir) ;
        String content = UUIDS.uuid() ;
        ByteArrayInputStream inputStream = new ByteArrayInputStream(content.getBytes() );
        String filePath =  Paths.get( root.toString() , UUIDS.uuid() ).toString() ;

        localDiskStorageService.write(inputStream , filePath );
        InputStream inputStream1 = localDiskStorageService.read(filePath);
        String content1 = IOUtils.toString(inputStream1);
        Assert.assertEquals(content,content1 );
        Assert.assertEquals(content.getBytes().length,localDiskStorageService.size( filePath ) );

        String mvPath = filePath+".bak";
        localDiskStorageService.move( filePath , mvPath );

        Assert.assertEquals(-1l,localDiskStorageService.size( filePath ) );
        Assert.assertEquals(content.getBytes().length,localDiskStorageService.size( mvPath ) );

        localDiskStorageService.remove(mvPath);
        Assert.assertEquals(-1l ,localDiskStorageService.size( mvPath ) );


    }


}
