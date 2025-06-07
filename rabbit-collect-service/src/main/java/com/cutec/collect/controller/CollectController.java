package com.cutec.collect.controller;

import com.cutec.common.response.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import com.cutec.collect.param.SeeReqData;
import com.cutec.collect.param.SupReqData;
import com.cutec.collect.service.CollectService;

import java.util.List;

@Slf4j
@RestController
public class CollectController {
    private final CollectService mediaService;


    public CollectController(CollectService mediaService) {
        this.mediaService = mediaService;
    }


    
    @PostMapping("/see")
    public Result<String> see(@RequestBody SeeReqData param) {
        mediaService.collectSee(param);
        return new Result<>("");
    }

    
    @PostMapping("/sup")
    public Result<String> see(@RequestBody List<SupReqData> param) {
        mediaService.collectSup(param);
        return new Result<>("");
    }
}
