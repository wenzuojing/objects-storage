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
import org.wens.os.apiserver.service.ActiveDataServerService;

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

        OkHttpClient okHttpClient = new OkHttpClient().newBuilder().callTimeout(6, TimeUnit.SECONDS).connectTimeout(1, TimeUnit.SECONDS).build();


        Request requestOfPost = new Request.Builder().url(String.format("http://%s/temp", dataServerAddress)).post(new FormBody.Builder().add("name" ,name).add("size" , "11").build()).build();

        String uuid = okHttpClient.newCall(requestOfPost).execute().body().string();



        Request requestOfPatch = new Request.Builder().url(String.format("http://%s/temp/%s" , dataServerAddress , uuid )).patch(new RequestBody() {
            @Override
            public MediaType contentType() {
                return MediaType.get("application/octet-stream");
            }

            @Override
            public void writeTo(BufferedSink bufferedSink) throws IOException {
                IOUtils.copy(request.getInputStream() , bufferedSink.outputStream());
            }
        }).build();

        int code = okHttpClient.newCall(requestOfPatch).execute().code();

        System.out.println(code);


        return ResponseEntity.ok(null);
    }
}
