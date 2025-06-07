package com.cutec.collect.parse;

import com.cutec.coral.common.enums.Error;
import com.cutec.coral.common.error.CustomizeException;
import com.cutec.coral.repository.NyaaMapper;
import com.cutec.coral.repository.Sukebei;
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
public class SukebeiParse {


    private final ProxyUtils proxyUtils;
    private final NyaaMapper nyaaMapper;
    private final RequestConfigUtils requestConfigUtils;

    public SukebeiParse(ProxyUtils proxyUtils, NyaaMapper nyaaMapper, RequestConfigUtils requestConfigUtils) {
        this.proxyUtils = proxyUtils;
        this.nyaaMapper = nyaaMapper;
        this.requestConfigUtils = requestConfigUtils;
    }


    public void parsePage(String url) {
        if (!proxyUtils.checkProxy()) {
            log.info("{}", "proxy err");
            throw new CustomizeException(Error.PROXY_ERROR);
        }
        ParseCore<List<Sukebei>> listParseCore = new ParseCore<>();
        listParseCore.parse(() -> {
            Document doc;
            try {
                doc = Jsoup.connect(url).proxy(proxyUtils.getHost(), proxyUtils.getPort()).headers(requestConfigUtils.parseHeaders()).cookies(requestConfigUtils.parseCookies()).get();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            return doc;
        }, doc -> {
            List<Sukebei> result = new ArrayList<>();
            Elements line = doc.select("tbody tr");
            for (Element element : line) {
                Sukebei item = new Sukebei();
                String title = element.select("td:nth-child(2)").text();
                item.setTitle(title);
                String magnet = element.select("td:nth-child(3) a:nth-child(2)").attr("href");
                if (StringUtils.hasText(magnet) && magnet.length() > "magnet:?xt=urn:sha1:199006246E1994505B4F7BE849A52C2BC2C24874".length()) {
                    item.setMagnet(magnet.substring(0, "magnet:?xt=urn:sha1:199006246E1994505B4F7BE849A52C2BC2C24874".length()));
                }
                String fileSize = element.select("td:nth-child(4)").text();
                item.setFileSize(fileSize);
                String _date = element.select("td:nth-child(5)").attr("data-timestamp");
                String download = element.select("td:nth-child(8)").text();
                if (StringUtils.hasText(download)) {
                    item.setDownload(Integer.valueOf(download));
                }
                item.setSrc("nyaa_no_water");
                item.setNumber(ToolBox.getNumberReg(title));
//                item.setJp(MediaUtils.containsJapanese(title));
                item.setCreateTime(new Date());
                item.setUpdateTime(new Date());
                result.add(item);
            }
            return result;
        }, mediaList -> {
            for (Sukebei se : mediaList) {
                List<Sukebei> all = nyaaMapper.findByMagnet(se.getMagnet());
                if (all.isEmpty()) {
                    nyaaMapper.insert(se);
                }
            }
        });
    }
}
