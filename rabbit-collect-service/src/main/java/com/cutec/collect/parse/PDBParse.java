package com.cutec.collect.parse;

import com.cutec.coral.common.enums.Error;
import com.cutec.coral.common.error.CustomizeException;
import com.cutec.coral.repository.Media;
import com.cutec.coral.service.MediaService;
import com.cutec.coral.utils.ProxyUtils;
import com.cutec.coral.utils.RequestConfigUtils;
import com.cutec.coral.utils.ToolBox;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

@Slf4j
@Component
public class PDBParse {

    private final RedisTemplate<String, String> redisTemplate;
    private final ProxyUtils proxyUtils;
    private final RequestConfigUtils requestConfigUtils;
    private final MediaService mediaService;

    public PDBParse(RedisTemplate<String, String> redisTemplate, ProxyUtils proxyUtils, RequestConfigUtils requestConfigUtils, MediaService mediaService) {
        this.redisTemplate = redisTemplate;
        this.proxyUtils = proxyUtils;
        this.requestConfigUtils = requestConfigUtils;
        this.mediaService = mediaService;
    }

    public Set<String> getPageUrl(String url) {
        if (!proxyUtils.checkProxy()) {
            log.info("{}", "proxy err");
            throw new CustomizeException(Error.PROXY_ERROR);
        }
        ParseCore<Set<String>> listParseCore = new ParseCore<>();
        return listParseCore.parse(() -> {
            Document doc;
            try {
                doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return doc;
        }, (document -> {
            Set<String> res = new HashSet<>();
            for (Element element : document.select("div.article_title")) {
                String text = element.select("a").attr("href");
                res.add(text);
            }
            return res;
        }));
    }

    public void parse(String url) {

        if (!proxyUtils.checkProxy()) {
            log.info("fix actors pdb proxy error");
            return;
        }

        Document doc;
        try {
            doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
        } catch (IOException e) {
            log.info("fix actors pdb get error");
            return;
        }

        Integer number = ToolBox.getNumberReg(url);
        Media media = new Media();
        media.setNumber(number);

        String actors = doc.select(".meta li").get(3).text();
        Elements title = doc.select(".sp_none > a:nth-child(1)");
        for (Element element : doc.select(".meta li")) {
            String text = element.text();
//                System.out.println(text);
            if (text.contains("販売日")) {
//                     media.setPostTime(text.split(" : ")[1]);
            }
            if (text.contains("再生時間")) {
//                     media.setDuration(text.split(" : ")[1]);
            }
            if (text.contains("発売価格")) {
//                     media.setPrices(text.split(" : ")[1]);
            }
            if (text.contains("販売者")) {
                media.setStudio(text.split(" : ")[1]);
            }
        }


        if (!StringUtils.hasText(media.getStudio())) {
            redisTemplate.opsForSet().add("studio-fix-err", String.valueOf(media.getNumber()));
            log.info("fix studio pdb actors error");
            return;
        }

        media.setFromPdb(Boolean.TRUE);
        mediaService.update("pdb", media);
        log.info("fix pdb update studio {} {}", media.getNumber(), media.getStudio());

    }

    public void parse(Integer number) {
        if (!proxyUtils.checkProxy()) {
            log.info("{}", "proxy err");
            throw new CustomizeException(Error.PROXY_ERROR);
        }
        String url = String.format("https://ppvdatabank.com/article/%s/", number);
        parse(url);
    }
}
