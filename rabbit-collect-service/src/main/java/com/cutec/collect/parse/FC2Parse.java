package com.cutec.collect.parse;

import com.cutec.coral.repository.Media;
import com.cutec.coral.service.MediaService;
import com.cutec.coral.utils.ProxyUtils;
import com.cutec.coral.utils.RequestConfigUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.Date;

@Slf4j
@Component
@Data
public class FC2Parse {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProxyUtils proxyUtils;
    private final MediaService mediaService;
    private final RequestConfigUtils requestConfigUtils;

    public FC2Parse(RedisTemplate<String, String> redisTemplate, ProxyUtils proxyUtils, MediaService mediaService, RequestConfigUtils requestConfigUtils) {
        this.redisTemplate = redisTemplate;
        this.proxyUtils = proxyUtils;
        this.mediaService = mediaService;
        this.requestConfigUtils = requestConfigUtils;
    }


    public void parsePage(Integer number) {
        if (!proxyUtils.checkProxy()) {
            log.info("fix tag fc2 proxy error");
        }

        Document doc;
        try {
            String url = String.format("https://adult.contents.fc2.com/article/%s/", number);
            doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort())
                    .headers(requestConfigUtils.parseHeaders()).get();
//                        .cookies(requestConfig.parseHeaders("website:basic").getCookies())
        } catch (IOException e) {
            redisTemplate.opsForSet().add("tag-fix-err", String.valueOf(number));
            log.info("fix tag fc2 visit error");
            return;
        }

        Media foo = new Media();
        foo.setNumber(number);
//
        String htmlTitle = doc.select("title").text();
        if (htmlTitle.contains("お探しの商品が見つかりません")) {
            redisTemplate.opsForSet().add("tag-fix-err", String.valueOf(number));
            foo.setFcEndSale(true);
        }

        if (htmlTitle.contains("没有发现您要找的商品")) {
            redisTemplate.opsForSet().add("tag-fix-err", String.valueOf(number));
            foo.setFcEndSale(true);
        }

        if (!StringUtils.hasText(htmlTitle)) {
            log.info("fix tag fc2 visit error");
        }

        String title = doc.select(".items_article_headerInfo h3").text();
        if (StringUtils.hasText(title)) {
            foo.setTitle(title);
        }

        String tag = doc.select(".tagTag").text();
        if (StringUtils.hasText(tag)) {
            foo.setTag(tag);
        }

        if (!StringUtils.hasText(foo.getTag())) {
            redisTemplate.opsForSet().add("actors-fix-err", String.valueOf(number));
            log.info("fix actors fc2db actors error");
            // return 可以
        }

        foo.setUpdateTime(new Date());
        mediaService.update("fc", foo);
        log.info("fix fc2 update title tag {} {} {}", foo.getNumber(), foo.getTitle(), foo.getTag());

    }
}
