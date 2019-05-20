package org.wens.os.apiserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.wens.os.matedata.MetaData;
import org.wens.os.matedata.MetaDataService;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;

/**
 * @author wens
 */
@Controller
@RequestMapping("/versions")
public class VersionsController {

    @Resource
    private MetaDataService metaDataService ;

    @GetMapping(value = "/{name}")
    public ResponseEntity versions(@PathVariable("name") String name) throws IOException {
        List<MetaData> metaDatas = metaDataService.findAllVersion(name);
        return ResponseEntity.ok(metaDatas);
    }

}
