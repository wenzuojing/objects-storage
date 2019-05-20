package org.wens.os.apiserver.controller;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.wens.os.apiserver.service.ActiveDataServerService;
import org.wens.os.apiserver.stream.RSConfig;
import org.wens.os.apiserver.stream.RSGetStream;
import org.wens.os.apiserver.stream.RSPutStream;
import org.wens.os.common.io.CalDigestInputStream;
import org.wens.os.common.util.MessageDisgestUtils;
import org.wens.os.locate.LocateService;
import org.wens.os.matedata.MetaData;
import org.wens.os.matedata.MetaDataService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.util.List;

@Controller
@RequestMapping("/objects")
public class ObjectsController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ActiveDataServerService activeDataServerService;

    @Resource
    private LocateService locateService;

    @Resource
    private MetaDataService metaDataService ;


    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name , @RequestParam(value = "version",required = false) Integer version, HttpServletResponse response) throws IOException {
        MetaData metaData = null ;
        if(version == null ){
            metaData = metaDataService.findLastVersion(name);
        }else{
            metaData = metaDataService.find(name,version);
        }

        if(metaData == null ){
            return ResponseEntity.notFound().build();
        }

        try (RSGetStream rsGetStream = new RSGetStream( metaData.getChecksum(), locateService)) {
            InputStream inputStream = rsGetStream.read();
            int n = IOUtils.copy(inputStream, response.getOutputStream());
            log.info("write {} byte",n );
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{name}")
    public ResponseEntity put(@PathVariable("name") String name, HttpServletRequest request) throws IOException {
        List<String> servers = activeDataServerService.random(RSConfig.DATA_SHARDS + RSConfig.PARITY_SHARDS);
        RSPutStream rsPutStream = new RSPutStream(servers);
        MessageDigest messageDigest = MessageDisgestUtils.sha256();
        CalDigestInputStream inputStream = new CalDigestInputStream(request.getInputStream(), messageDigest);
        long size = rsPutStream.write(inputStream);
        String checksum = Hex.encodeHexString(messageDigest.digest());
        if (!rsPutStream.commit(checksum)) {
            rsPutStream.rollback();
            log.error("commit fail.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        MetaData metaData = metaDataService.findLastVersion(name);
        if(metaData == null ){
            metaData = new MetaData();
            metaData.setVersion(1);
        }
        metaData.setName(name);
        metaData.setChecksum(checksum);
        metaData.setSize(size);
        metaData.setProps(resolveProps(request));
        metaDataService.save(metaData);
        return ResponseEntity.ok(metaData);
    }

    private JSONObject resolveProps(HttpServletRequest request) {
        String str = request.getHeader("props");
        if(str == null ){
            return new JSONObject();
        }
        return JSONObject.parseObject( new String( Base64.decodeBase64(str), Charset.forName("utf-8") ) );
    }
}
