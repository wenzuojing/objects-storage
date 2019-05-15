package org.wens.os.dataserver.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wens.os.common.io.CalDigestInputStream;
import org.wens.os.common.util.IO;
import org.wens.os.common.util.MessageDisgestUtils;
import org.wens.os.common.util.UUIDS;
import org.wens.os.dataserver.service.StorageService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

@Controller
@RequestMapping("/temp")
public class TempController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${storage.root}")
    private String storageRoot ;

    @Resource
    private StorageService storageService ;


    @PutMapping("/{uuid}")
    public ResponseEntity put(@PathVariable("uuid") String uuid) throws IOException {
        try( InputStream inputStream = storageService.read(Paths.get(storageRoot, "temp", uuid).toString())){
            if( inputStream == null ){
                log.warn("Can not read inputStream. [ uuid = {} ]" , uuid );
                return ResponseEntity.notFound().build();
            }

            JSONObject jsonObject = JSONObject.parseObject(IOUtils.toString(inputStream, Charset.forName("utf-8")));
            long actualSize  = storageService.size(Paths.get(storageRoot, "temp", uuid + ".bat").toString());
            if( jsonObject.getLong("size") != actualSize ){
                log.warn("Size mismatch.[size = {} , actualSize = {} , uuid = {} ]" , jsonObject.getLong("size") ,actualSize , uuid );
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            //commit
            MessageDigest messageDigest = MessageDisgestUtils.sha256();
            try(InputStream inputStream2 = new CalDigestInputStream(storageService.read(Paths.get(storageRoot, "temp", uuid).toString()) , messageDigest )){
                storageService.write( inputStream2 , Paths.get(storageRoot, "objects", jsonObject.getString("name")).toString(),true );
                try(InputStream inputStream3  = IO.textToInputStream("sha256-"+Base64.encodeBase64String( messageDigest.digest() ) ) ){
                    storageService.write(inputStream3,Paths.get(storageRoot, "objects", jsonObject.getString("name") + ".checksum").toString() );
                }
            }
        }
        return ResponseEntity.ok(null);
    }

    @PostMapping()
    public ResponseEntity post(String name , String size ) throws IOException {
        String uuid = UUIDS.uuid();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name" , name );
        jsonObject.put("size",size);
        jsonObject.put("uuid",uuid);
        try( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonObject.toJSONString().getBytes(Charset.forName("utf-8")))){
            storageService.write(byteArrayInputStream , Paths.get(storageRoot,"temp" , uuid ).toString());
        }
        return ResponseEntity.ok(uuid);
    }

    @PatchMapping(value = "/{uuid}")
    public ResponseEntity patch(@PathVariable("uuid") String uuid , HttpServletRequest request) throws IOException {
        storageService.write(request.getInputStream() ,Paths.get(storageRoot,"temp" , uuid + ".bat" ).toString() );
        return ResponseEntity.ok(null);
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity delete(@PathVariable("uuid") String uuid) throws IOException {
        storageService.remove( Paths.get(storageRoot,"temp" , uuid  ).toString() );
        storageService.remove( Paths.get(storageRoot,"temp" , uuid + ".bat" ).toString() );
        return ResponseEntity.ok(null);
    }





}
