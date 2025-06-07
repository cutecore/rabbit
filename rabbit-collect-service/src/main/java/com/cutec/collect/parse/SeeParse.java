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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Slf4j
@Component
public class SeeParse {
    private final ProxyUtils proxyUtils;
    private final MediaService mediaService;
    private final RequestConfigUtils requestConfigUtils;

    public SeeParse(ProxyUtils proxyUtils, MediaService mediaService, RequestConfigUtils requestConfigUtils) {
        this.proxyUtils = proxyUtils;
        this.mediaService = mediaService;
        this.requestConfigUtils = requestConfigUtils;
    }


    public List<String> parseIndexPage(String url) {
        if (!proxyUtils.checkProxy()) {
            log.info("{}", "proxy err");
            throw new CustomizeException(Error.PROXY_ERROR);
        }
        ParseCore<List<String>> listParseCore = new ParseCore<>();
        return listParseCore.parse(() -> {
            Document doc;
            try {
                doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).
                        headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return doc;
        }, (document -> {
//            
            ArrayList<String> result = new ArrayList<>();
            for (Element element : document.select(".s.xst")) {
                String href = element.attr("href");
                String page_url = "https://sehuatang.org/" + href;
                result.add(page_url);
            }
            return result;
        }));
    }

    public void parsePage(String url) {
        if (!proxyUtils.checkProxy()) {
            log.info("{}", "proxy err");
            throw new CustomizeException(Error.PROXY_ERROR);
        }
        ParseCore<Media> listParseCore = new ParseCore<>();
        listParseCore.parse(() -> {
            Document doc;
            try {
                doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return doc;
        }, doc -> {
            Media media = new Media();
            String title = doc.select("#thread_subject").text();
            String description = doc.select("meta[name='description']").attr("content");
            String number = ToolBox.getNumber(title);
            String post_time = doc.select(".plc .pti .authi em span").attr("title");
            if (!StringUtils.hasText(post_time)) {
                String[] _t = doc.select(".plc .pi .pti .authi em").text().split("发表于");
                if (_t.length >= 3) {
                    post_time = _t[2];
                }
            }
            String magnet = doc.select("div .blockcode div ol li").text();
            media.setMagnet(magnet);
            for (Element element : doc.select("div .tattl dd p")) {
                String text = element.text();
                if (text.contains("下载次数")) {
                    String _down = text.split("下载次数: ")[1];
                    if (ToolBox.isNumeric(_down)) {
                        media.setSeDownloadNum(Integer.valueOf(_down));
                    }
                }
            }
            StringBuilder torrentUrl = new StringBuilder();
            for (Element element : doc.select(".attnm a")) {
                torrentUrl.append(element.attr("href")).append(",");
            }
            StringBuilder imageUrl = new StringBuilder();
            for (Element element : doc.select(".zoom")) {
                imageUrl.append(element.attr("file")).append(",");
            }
            if (ToolBox.isNumeric(number)) {
                media.setNumber(Integer.valueOf(number));
            } else {
                media.setNumber(getFakeNumber(url));
            }
//            media.setImageUrl(imageUrl.toString());
//            media.setTorrentUrl(torrentUrl.toString());
            if (description.contains("【出演女优】") && description.contains("【影片名称】：")) {
//                media.setSeTitle(description.split("【出演女优】")[0].split("【影片名称】：")[1]);
            } else {
//                media.setSeTitle(description);
            }
            media.setUpdateTime(new Date());
//            media.setPageUrl(url);
//            media.setRate(0);
            return media;
        }, media -> {
            mediaService.update("see", media);
//            String imageUrlList = media.getImageUrl();
//            int index = 1;
//            for (String imageUrl : imageUrlList.split(",")) {
//                int finalIndex = index;
//                mediaThreadPool.getDownloadPool().submit(() -> {
//                    DownloadHelper.download(imageUrl, new FileConfig().getPicturePath() + media.getNumber() + "_" + finalIndex + ".jpg");
//                });
//                index++;
//            }
        });

    }

    Integer getFakeNumber(String url) {
        //https://sehuatang.org/thread-1825131-1-1.html
        //https://sehuatang.org/forum.php?mod=viewthread&tid=1630099&extra=page%3D15%26filter%3Dtypeid%26typeid%3D368"
        return -Integer.valueOf(url.split("&extra")[0].replace("https://sehuatang.org/forum.php?mod=viewthread&tid=", ""));
    }

    public void parseOtherPage(String url) {
        ParseCore<Media> listParseCore = new ParseCore<>();
        listParseCore.parse(() -> {
            Document doc;
            try {
                doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return doc;
        }, doc -> {
            Media media = new Media();
            String title = doc.select("#thread_subject").text();
            String magnet = doc.select("div .blockcode div ol li").text();
            media.setMagnet(magnet);
            return media;
        }, media -> {
        });

    }
}
/**
 *
 */


