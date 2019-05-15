package org.wens.os.dataserver.controller;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wens.os.common.io.CalDigestInputStream;
import org.wens.os.common.util.MessageDisgestUtils;
import org.wens.os.dataserver.service.StorageService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.MessageDigest;

@Controller
@RequestMapping("/objects")
public class ObjectsController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${storage.root}")
    private String storageRoot ;

    @Resource
    private StorageService storageService ;

    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name , HttpServletResponse response ) throws IOException {

        try(InputStream inputStream = storageService.read(Paths.get(storageRoot,"objects" , name ).toString() , true )){
            if(inputStream == null ){
                log.warn("Can not read inputStream. [ hash = {} ]" , name );
                return ResponseEntity.notFound().build();
            }
            MessageDigest messageDigest = MessageDisgestUtils.sha256();
            try( InputStream inputStream2 = new CalDigestInputStream(inputStream , messageDigest )){
                IOUtils.copyLarge(inputStream2 ,response.getOutputStream());
                try( InputStream inputStream3 = storageService.read(Paths.get(storageRoot, "objects", name + ".checksum").toString())){
                    String sha256 = IOUtils.toString(inputStream3, Charset.forName("utf-8"));
                    String actualSha256 = Base64.encodeBase64String(messageDigest.digest()) ;
                    if(sha256.indexOf( actualSha256 ) == -1 ){
                        log.error("checksum mismatch.[ expired = {} , actual = {} ]" , sha256 ,actualSha256 );
                        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build() ;
                    }
                }

            }

        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{hash}")
    public ResponseEntity delete(@PathVariable("name") String name ) throws IOException {
        storageService.move( Paths.get(storageRoot,"objects" , name ).toString() ,  Paths.get(storageRoot,"garbage" , name ).toString() );
        return ResponseEntity.ok().build();
    }



}
