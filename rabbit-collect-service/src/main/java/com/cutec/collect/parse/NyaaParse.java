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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class NyaaParse {
    private final ProxyUtils proxyUtils;
    private final MediaService mediaService;
    private final RequestConfigUtils requestConfigUtils;

    public NyaaParse(ProxyUtils proxyUtils, MediaService mediaService, RequestConfigUtils requestConfigUtils) {
        this.proxyUtils = proxyUtils;
        this.mediaService = mediaService;
        this.requestConfigUtils = requestConfigUtils;
    }


    public void parsePage(String url) {
        if (!proxyUtils.checkProxy()) {
            log.info("{}", "proxy err");
            throw new CustomizeException(Error.PROXY_ERROR);
        }
        ParseCore<List<Media>> listParseCore = new ParseCore<>();
        listParseCore.parse(() -> {
            Document doc;
            try {
                doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return doc;
        }, doc -> {
            List<Media> result = new ArrayList<>();
            Elements line = doc.select("tbody tr");
            for (Element element : line) {
                Media item = new Media();
                String title = element.select("td:nth-child(2) a").attr("title");
//                item.setNyaaTitle(title);
                item.setFromNyaa(Boolean.TRUE);
                String magnet = element.select("td:nth-child(3) a:nth-child(2)").attr("href");
                if (StringUtils.hasText(magnet) && magnet.length() > "magnet:?xt=urn:sha1:199006246E1994505B4F7BE849A52C2BC2C24874".length()) {
                    item.setNyaaMagnet(magnet.substring(0, "magnet:?xt=urn:sha1:199006246E1994505B4F7BE849A52C2BC2C24874".length()));
                }
                String fileSize = element.select("td:nth-child(4)").text();
                item.setNyaaFileSize(fileSize);
                String _date = element.select("td:nth-child(5)").attr("data-timestamp");
                Date createTime = new Date(Long.parseLong(_date) * 1000);
                item.setCreateTime(createTime);
                String download = element.select("td:nth-child(8)").text();
                if (StringUtils.hasText(download)) {
                    item.setNyaaDownloadNum(Integer.valueOf(download));
                }
                Integer numberReg = ToolBox.getNumberReg(title);
                item.setNumber(numberReg);
                result.add(item);
            }
            return result;
        }, mediaList -> {
            for (Media media : mediaList) {
                if (media.getNumber() != null) {
                    mediaService.update("nyaa_no_water", media);
                }
            }
        });
    }
}
