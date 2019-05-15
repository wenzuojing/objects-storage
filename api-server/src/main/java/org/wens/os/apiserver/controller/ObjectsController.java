package org.wens.os.apiserver.controller;

import org.apache.commons.io.IOUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;

@Controller
@RequestMapping("/objects")
public class ObjectsController {

    @GetMapping("/{name}")
    public ResponseEntity get(@PathVariable("name") String name){

        return ResponseEntity.ok(null);
    }

    @PutMapping(value = "/{name}")
    public ResponseEntity put(@PathVariable("name") String name,@RequestBody byte[] bytes) throws IOException {
        return ResponseEntity.ok(null);
    }
}
