package com.cutec.meida.api;

import com.cutec.common.response.PageContent;
import com.cutec.common.response.Result;
import com.cutec.meida.api.param.MediaQuery;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import com.cutec.meida.api.vo.Media;

@FeignClient(name = "media")
public interface MediaApi {

    @GetMapping("/media/{number}")
    Result<Media> get(@PathVariable(name = "number") Integer number);

    @PostMapping("/media/list")
    Result<PageContent<Media>> list(@RequestBody MediaQuery query);

}
