package org.wens.os.dataserver.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wens.os.common.io.CalDigestOutputStream;
import org.wens.os.common.util.IO;
import org.wens.os.common.util.MessageDisgestUtils;
import org.wens.os.common.util.UUIDS;
import org.wens.os.dataserver.service.StorageInstanceService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;

@Controller
@RequestMapping("/temp")
public class TempController {

    private Logger log = LoggerFactory.getLogger(this.getClass());


    @Resource
    private StorageInstanceService storageInstanceService;


    @PutMapping("/{uuid}")
    public ResponseEntity put(@PathVariable("uuid") String uuid) throws IOException {
        try (InputStream inputStream = storageInstanceService.getTempStorageService().read(uuid)) {
            if (inputStream == null) {
                log.warn("Can not read inputStream. [ uuid = {} ]", uuid);
                return ResponseEntity.notFound().build();
            }

            JSONObject jsonObject = JSONObject.parseObject(IOUtils.toString(inputStream, Charset.forName("utf-8")));
            long actualSize = storageInstanceService.getTempStorageService().size(uuid + ".bat");
            if (jsonObject.getLong("size") != actualSize) {
                log.warn("Size mismatch.[size = {} , actualSize = {} , uuid = {} ]", jsonObject.getLong("size"), actualSize, uuid);
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
            }

            //commit
            MessageDigest messageDigest = MessageDisgestUtils.sha256();
            try (InputStream inputStream2 = storageInstanceService.getTempStorageService().read(uuid);
                 OutputStream outputStream2 = new CalDigestOutputStream(storageInstanceService.getObjectsStorageService().write(jsonObject.getString("name")), messageDigest);
                 OutputStream outputStream3 = storageInstanceService.getObjectsStorageService().write(jsonObject.getString("name") + ".checksum")

            ) {
                IOUtils.copy(inputStream2, outputStream2);
                try (InputStream inputStream3 = IO.textToInputStream("sha256-" + Base64.encodeBase64String(messageDigest.digest()))) {
                    IOUtils.copy(inputStream3, outputStream3);
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping()
    public ResponseEntity post(@RequestParam("name") String name, @RequestParam("size") String size) throws IOException {
        String uuid = UUIDS.uuid();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("name", name);
        jsonObject.put("size", size);
        jsonObject.put("uuid", uuid);
        try (InputStream inputStream = new ByteArrayInputStream(jsonObject.toJSONString().getBytes(Charset.forName("utf-8")));
             OutputStream outputStream = storageInstanceService.getTempStorageService().write(uuid)) {
            IOUtils.copy(inputStream, outputStream);
        }
        return ResponseEntity.ok(uuid);
    }

    @PatchMapping(value = "/{uuid}")
    public ResponseEntity patch(@PathVariable("uuid") String uuid, HttpServletRequest request) throws IOException {
        try (OutputStream outputStream = storageInstanceService.getTempStorageService().write(uuid + ".bat")) {
            IOUtils.copy(request.getInputStream(), outputStream);
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{uuid}")
    public ResponseEntity delete(@PathVariable("uuid") String uuid) throws IOException {
        storageInstanceService.getTempStorageService().remove(uuid);
        storageInstanceService.getTempStorageService().remove(uuid + ".bat");
        return ResponseEntity.ok(null);
    }


}
