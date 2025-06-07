package com.cutec.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cutec.common.response.PageContent;
import com.cutec.common.response.Result;
import com.cutec.entity.Media;
import com.cutec.param.MediaQuery;
import com.cutec.service.MediaService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;

@Slf4j
@RestController
public class MediaController {
    private final MediaService mediaService;


    public MediaController(MediaService mediaService) {
        this.mediaService = mediaService;
    }
    
    
    @GetMapping("/media/{number}")
    public Result<Media> get(@PathVariable(name = "number") Integer number) {
        Media media = mediaService.getUseCache(number);
        return new Result<>(media);
    }

    @PatchMapping("/media/{number}")
    public Result<Media> update(@PathVariable(name = "number") Integer number, @RequestBody HashMap<String, Object> param) {
        Media media = mediaService.updatePart(number, param);
        return new Result<>(media);
    }

    //    
    @PostMapping("/media/list")
    public Result<PageContent<Media>> list(@RequestBody MediaQuery query) {
        Page<Media> page = mediaService.page(query);
        PageContent<Media> build = PageContent.<Media>builder().items(page.getRecords()).total(page.getTotal()).build();
        return new Result<>(build);
    }

}
