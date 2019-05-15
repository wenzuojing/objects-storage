package org.wens.os.dataserver.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wens.os.common.util.UUIDS;
import org.wens.os.dataserver.service.StorageService;

import javax.annotation.Resource;
import javax.servlet.ServletInputStream;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringBufferInputStream;
import java.nio.charset.Charset;
import java.nio.file.Paths;

@Controller
@RequestMapping("/temp")
public class TempController {

    @Value("${storageRoot}")
    private String storageRoot ;

    @Resource
    private StorageService storageService ;



    @PutMapping("/{uuid}")
    public ResponseEntity put(@PathVariable("uuid") String uuid){
        return ResponseEntity.ok(null);
    }

    @PostMapping("/")
    public ResponseEntity post(String name , String size ) throws IOException {
        String uuid = UUIDS.uuid();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name" , name );
        jsonObject.put("size",size);
        jsonObject.put("size",size);
        try( ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(jsonObject.toJSONString().getBytes(Charset.forName("utf-8")))){
            storageService.write(byteArrayInputStream , Paths.get(storageRoot,"temp" , uuid ).toString());
        }
        return ResponseEntity.ok(uuid);
    }

    @PatchMapping("/{uuid}")
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
