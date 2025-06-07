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
import java.util.List;

@Slf4j
@Component
public class SupParse {
    private final ProxyUtils proxyUtils;
    private final MediaService mediaService;
    private final RequestConfigUtils requestConfigUtils;

    public SupParse(ProxyUtils proxyUtils, MediaService mediaService, RequestConfigUtils requestConfigUtils) {
        this.proxyUtils = proxyUtils;
        this.mediaService = mediaService;
        this.requestConfigUtils = requestConfigUtils;
    }


    public void parsePage(String url) {
        if (!proxyUtils.checkProxy()) {
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
            Elements line = doc.select(".con");
            for (Element element : line) {
                Media media = new Media();
                String title = element.select("a").attr("title");
                Integer numberReg = ToolBox.getNumberReg(title);
                String views = element.select(".meta .date").text();
                //  90757 Views 解析字符串 提取数字
                if (StringUtils.hasText(views)) {
                    views = views.replace("Views", "").trim();
                    if (ToolBox.isInputSafe(views)) {
                        Integer viewsNum = Integer.parseInt(views);
                        media.setSupViews(viewsNum);
                    }
                }


//                log.info("SUP {} {} ", title, views);
                if (numberReg != null) {
                    if (title.contains("[cen")) {
                        media.setCen(Boolean.TRUE);
                    }
//                    if (title.length() > 255) {
//                        title = title.substring(0, 254);
//                    }
//                    media.setSupTitle(title);
                    media.setFromSup(Boolean.TRUE);
                    media.setNumber(numberReg);
                    result.add(media);
                }
            }
            if (result.isEmpty()) {
                throw new CustomizeException(Error.DATA_EMPTY);
            }
            return result;
        }, mediaList -> {
            for (Media media : mediaList) {
                if (media.getNumber() != null) {
                    mediaService.update("sup", media);
                }
            }
        });
    }
}
