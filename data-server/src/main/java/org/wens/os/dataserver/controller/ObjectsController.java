package org.wens.os.dataserver.controller;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wens.os.common.io.CalDigestInputStream;
import org.wens.os.common.util.MessageDisgestUtils;
import org.wens.os.dataserver.service.StorageInstanceService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.zip.GZIPInputStream;

@Controller
@RequestMapping("/objects")
public class ObjectsController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private StorageInstanceService storageInstanceService;

    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name, HttpServletResponse response) throws IOException {

        try (InputStream inputStream = storageInstanceService.getObjectsStorageService().read(name)) {
            if (inputStream == null) {
                log.warn("Can not read inputStream. [ hash = {} ]", name);
                return ResponseEntity.notFound().build();
            }

            MessageDigest messageDigest = MessageDisgestUtils.sha256();
            try (InputStream inputStream2 = new GZIPInputStream(new CalDigestInputStream(inputStream, messageDigest));
                 InputStream inputStream3 = storageInstanceService.getObjectsStorageService().read(name + ".checksum")) {
                IOUtils.copy(inputStream2, response.getOutputStream());
                String sha256 = IOUtils.toString(inputStream3, Charset.forName("utf-8"));
                String actualSha256 = Hex.encodeHexString(messageDigest.digest());
                if (sha256.indexOf(actualSha256) == -1) {
                    log.error("checksum mismatch.[ expired = {} , actual = {} ]", sha256, actualSha256);
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
                }
            }
        }
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{hash}")
    public ResponseEntity delete(@PathVariable("name") String name) throws IOException {
        try (
                InputStream src1 = storageInstanceService.getObjectsStorageService().read(name);
                InputStream src2 = storageInstanceService.getObjectsStorageService().read(name + ".checksum");
                OutputStream to1 = storageInstanceService.garbageStorageService.write(name);
                OutputStream to2 = storageInstanceService.garbageStorageService.write(name + ".checksum")
        ) {
            IOUtils.copy(src1, to1);
            IOUtils.copy(src2, to2);
        }
        return ResponseEntity.ok().build();
    }


}
