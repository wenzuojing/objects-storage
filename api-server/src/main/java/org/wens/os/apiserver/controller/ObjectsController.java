package org.wens.os.apiserver.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wens.os.apiserver.service.ActiveDataServerService;
import org.wens.os.apiserver.stream.GetStream;
import org.wens.os.apiserver.stream.PutStream;
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

    @Resource
    private ActiveDataServerService activeDataServerService;

    @Resource
    private LocateService locateService ;

    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name , HttpServletResponse response ) throws IOException {

        List<String> servers = locateService.locate(Arrays.asList(name), 1);

        if(servers.size() == 0 ){
            return ResponseEntity.notFound().build();
        }
        try(GetStream getStream = new GetStream(servers.get(0),name)){
            InputStream inputStream = getStream.read();
            IOUtils.copy(inputStream,response.getOutputStream());
        }
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
