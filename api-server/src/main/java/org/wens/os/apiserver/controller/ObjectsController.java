package org.wens.os.apiserver.controller;

import okhttp3.*;
import okhttp3.RequestBody;
import okio.BufferedSink;
import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;
import org.wens.os.apiserver.putstream.PutStream;
import org.wens.os.apiserver.service.ActiveDataServerService;
import org.wens.os.common.http.OKHttps;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Controller
@RequestMapping("/objects")
public class ObjectsController {

    @Resource
    private ActiveDataServerService activeDataServerService;

    @Resource
    private RestTemplate restTemplate ;

    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name) {

        return ResponseEntity.ok(null);
    }

    @PutMapping(value = "/{name}")
    public ResponseEntity put(@PathVariable("name") String name, HttpServletRequest request ) throws IOException {

        List<String> allActiveDataServers = activeDataServerService.getAllActiveDataServers();
        String dataServerAddress = allActiveDataServers.get(0);

        PutStream putStream = new PutStream(dataServerAddress);
        PutStream.WriteResult writeResult = putStream.write(request.getInputStream());
        if( !putStream.commit(writeResult.checksum.split("-")[1]) ){
            putStream.delete();
        }

        return ResponseEntity.ok(null);
    }
}
