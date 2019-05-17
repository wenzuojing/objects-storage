package org.wens.os.apiserver.controller;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wens.os.apiserver.service.ActiveDataServerService;
import org.wens.os.apiserver.stream.GetStream;
import org.wens.os.apiserver.stream.PutStream;
import org.wens.os.apiserver.stream.RSGetStream;
import org.wens.os.apiserver.stream.RSPutStream;
import org.wens.os.locate.LocateService;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/objects")
public class ObjectsController {

    private Logger log = LoggerFactory.getLogger(this.getClass());

    @Resource
    private ActiveDataServerService activeDataServerService;

    @Resource
    private LocateService locateService ;

    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name , HttpServletResponse response ) throws IOException {
        try(RSGetStream rsGetStream = new RSGetStream(name,locateService )){
            InputStream inputStream = rsGetStream.read();
            IOUtils.copy(inputStream,response.getOutputStream());
        }
        return ResponseEntity.ok().build();
    }

    @PutMapping(value = "/{name}")
    public ResponseEntity put(@PathVariable("name") String name, HttpServletRequest request ) throws IOException {
        List<String> servers = activeDataServerService.random(2);
        RSPutStream rsPutStream = new RSPutStream(servers);
        rsPutStream.write(request.getInputStream());
        if(!rsPutStream.commit()){
            rsPutStream.rollback();
            log.error("commit fail.");
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
        return ResponseEntity.ok().build();
    }
}
