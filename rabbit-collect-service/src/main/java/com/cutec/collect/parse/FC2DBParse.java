package com.cutec.collect.parse;

import com.cutec.coral.repository.Media;
import com.cutec.coral.service.MediaService;
import com.cutec.coral.utils.ProxyUtils;
import com.cutec.coral.utils.RequestConfigUtils;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;

@Slf4j
@Component
public class FC2DBParse {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProxyUtils proxyUtils;
    private final MediaService mediaService;
    private final RequestConfigUtils requestConfigUtils;

    public FC2DBParse(RedisTemplate<String, String> redisTemplate, ProxyUtils proxyUtils, MediaService mediaService, RequestConfigUtils requestConfigUtils) {
        this.redisTemplate = redisTemplate;
        this.proxyUtils = proxyUtils;
        this.mediaService = mediaService;
        this.requestConfigUtils = requestConfigUtils;
    }


    public void parsePage(Integer number) {
        if (!proxyUtils.checkProxy()) {
            log.info("fix actors fc2db proxy error");
            return;
        }

        Document doc;
        try {
            String url = String.format("https://fc2ppvdb.com/articles/%s", number);
            doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort())
                    .headers(requestConfigUtils.parseHeaders()).get();
        } catch (IOException e) {
            log.info("fix actors fc2db visit error");
            return;
        }
        String actors = doc.select(".lg\\:pl-8 > div:nth-child(4) > span:nth-child(1)").text();

        if (!StringUtils.hasText(actors)) {
            redisTemplate.opsForSet().add("actors-fix-err", String.valueOf(number));
            log.info("fix actors fc2db actors error");
            return;
        }

        Media media = new Media();
        media.setNumber(number);
        media.setActors(actors);
        mediaService.update("fc2db", media);
        log.info("fix fc2db update studio {} {}", media.getNumber(), media.getActors());

    }
}
