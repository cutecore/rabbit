package com.cutec.collect.service;

import com.cutec.coral.repository.Media;
import com.cutec.coral.service.parse.*;
import com.cutec.coral.utils.JsonUtils;
import com.cutec.coral.utils.MediaThreadPool;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class TaskService {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final CollectService collectService;
    private final MediaThreadPool mediaThreadPool;
    private final PDBParse pdbParse;
    private final FC2Parse fc2Parse;
    private final NyaaParse nyaaParse;
    private final SukebeiParse sukebeiParse;
    private final SupParse supParse;
    private final FC2DBParse fc2DBParse;
    private final RedisTemplate<String, String> redisTemplate;

    public TaskService(KafkaTemplate<String, String> kafkaTemplate, CollectService collectService, MediaThreadPool mediaThreadPool, PDBParse pdbParse, FC2Parse fc2Parse, NyaaParse nyaaParse, SukebeiParse sukebeiParse, SupParse supParse, FC2DBParse fc2DBParse, RedisTemplate<String, String> redisTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        this.collectService = collectService;
        this.mediaThreadPool = mediaThreadPool;
        this.pdbParse = pdbParse;
        this.fc2Parse = fc2Parse;
        this.nyaaParse = nyaaParse;
        this.sukebeiParse = sukebeiParse;
        this.supParse = supParse;
        this.fc2DBParse = fc2DBParse;
        this.redisTemplate = redisTemplate;
    }


    @Scheduled(cron = "0 0 */6 * * ? ")
    public void collectPdbDaily() {
        Set<String> urlSet = new HashSet<>();
        urlSet.add("https://ppvdatabank.com/ranking/daily/");
        urlSet.add("https://ppvdatabank.com/ranking/daily/page2.php");
        urlSet.add("https://ppvdatabank.com/ranking/daily/page3.php");
        urlSet.add("https://ppvdatabank.com/ranking/daily/page4.php");
        urlSet.add("https://ppvdatabank.com/ranking/daily/page5.php");
        urlSet.add("https://ppvdatabank.com/ranking/weekly/");
        urlSet.add("https://ppvdatabank.com/ranking/weekly/page2.php");
        urlSet.add("https://ppvdatabank.com/ranking/weekly/page3.php");
        urlSet.add("https://ppvdatabank.com/ranking/weekly/page4.php");
        urlSet.add("https://ppvdatabank.com/ranking/weekly/page5.php");
        urlSet.add("https://ppvdatabank.com/ranking/monthly/");
        urlSet.add("https://ppvdatabank.com/ranking/monthly/page2.php");
        urlSet.add("https://ppvdatabank.com/ranking/monthly/page3.php");
        urlSet.add("https://ppvdatabank.com/ranking/monthly/page4.php");
        urlSet.add("https://ppvdatabank.com/ranking/monthly/page5.php");
        urlSet.add("https://ppvdatabank.com/ranking/yearly/");
        urlSet.add("https://ppvdatabank.com/ranking/yearly/page2.php");
        urlSet.add("https://ppvdatabank.com/ranking/yearly/page3.php");
        urlSet.add("https://ppvdatabank.com/ranking/yearly/page4.php");
        urlSet.add("https://ppvdatabank.com/ranking/yearly/page5.php");

        for (String urlHome : urlSet) {
            Set<String> pageUrl = pdbParse.getPageUrl(urlHome);
            for (String url : pageUrl) {
                mediaThreadPool.getSinglePool().submit(() -> {
                    try {
                        pdbParse.parse(url);
                    } catch (Exception e) {
                        log.info("err parse type: pdb url:{}", url);
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }


    // @Scheduled(cron = "0 30 */6 * * ? ")
    public void collectSup() {

        Set<String> urlSet = new HashSet<>();
        urlSet.add("https://supjav.com/category/uncensored-jav");
        urlSet.add("https://supjav.com/category/uncensored-jav?sort=views");
        for (int i = 2; i < 10; i++) {
            urlSet.add("https://supjav.com/category/uncensored-jav/page/" + i);
            urlSet.add("https://supjav.com/category/uncensored-jav/page/" + i + "?sort=views");
        }

        for (String url : urlSet) {
            mediaThreadPool.getSinglePool().submit(() -> {
                try {
                    TimeUnit.SECONDS.sleep(5);
                    supParse.parsePage(url);
                } catch (Exception e) {
                    log.info("parse sup err:{}", url);
//                    throw new RuntimeException(e);
                    try {
                        supParse.parsePage(url);
                    } catch (Exception ex) {
                        log.info("parse sup try err  {}", url);
                        throw new RuntimeException(ex);
                    }
                }
                log.info("parse sup success:{}", url);
            });
        }
    }

    @Scheduled(cron = "30 45 */1 * * ? ")
    public void pushFixTask() {
        List<Media> tag = collectService.queryMissTag();
        tag.forEach(t -> {
            kafkaTemplate.send("tag-fix", JsonUtils.toJson(t));
        });

        List<Media> actors = collectService.queryMissActors();
        actors.forEach(t -> {
            kafkaTemplate.send("actors-fix", JsonUtils.toJson(t));
        });

        List<Media> studio = collectService.queryMissStudio();
        studio.forEach(t -> {
            kafkaTemplate.send("studio-fix", JsonUtils.toJson(t));
        });
    }


    @KafkaListener(topics = "tag-fix", groupId = "fix_fc2")
    public void collectFc2(String message) {
        Media item = JsonUtils.toObject(message, Media.class);
        try {
            fc2Parse.parsePage(item.getNumber());
        } catch (Exception e) {
            log.info("fc err {}", e.getMessage());
            redisTemplate.opsForSet().add("tag-fix-err", String.valueOf(item.getNumber()));
        }
    }

    @KafkaListener(topics = "studio-fix", groupId = "fix_pdb")
    public void collectPdb(String message) {
        Media item = JsonUtils.toObject(message, Media.class);
        try {
            pdbParse.parse(item.getNumber());
        } catch (Exception e) {
            redisTemplate.opsForSet().add("studio-fix-err", String.valueOf(item.getNumber()));
            log.info("studio fix err: pdb number:{}", item.getNumber());
        }
    }

    @KafkaListener(topics = "actors-fix", groupId = "fix_fc2db")
    public void collectFc2DB(String message) {
        Media item = JsonUtils.toObject(message, Media.class);
        fc2DBParse.parsePage(item.getNumber());
    }

    // @Scheduled(cron = "0 0 0 */3 * ? ")
    public void collectNyaa() {
        for (int i = 0; i < 5; i++) {
            nyaaParse.parsePage("https://sukebei.nyaa.si/?f=0&c=0_0&q=NoWaterMark&s=downloads&o=desc&p=" + i);
        }
    }


    @Scheduled(cron = "0 30 */6 * * ? ")
    public void updateFileTask() {
        collectService.updateFile("C:\\repo\\fc2");
        collectService.updateFile("D:\\repo_d\\fc2");
        collectService.updateFile("/mnt/repo/fc2");
        collectService.updateFile("/mnt/repo_d/fc2");
    }


    @Scheduled(cron = "0 30 */6 * * ? ")
    public void collectSukebei() {
        Set<String> urlSet = new HashSet<>();
        for (int i = 1; i < 5; i++) {
            if (i == 1) {
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版");
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=size&o=desc");
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=seeders&o=desc");
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=leechers&o=desc");
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=downloads&o=desc");
            } else {
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&p=" + i);
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=size&o=desc&p=" + i);
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=seeders&o=desc&p=" + i);
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=leechers&o=desc&p=" + i);
                urlSet.add("https://sukebei.nyaa.si/?f=0&c=0_0&q=原版&s=downloads&o=desc&p=" + i);
            }
        }
        for (String url : urlSet) {
            try {
                TimeUnit.SECONDS.sleep(3);
                sukebeiParse.parsePage(url);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }

    public void nfoTask() {
        collectService.nfo();
    }
}
