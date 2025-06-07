package com.cutec.collect.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.OrderItem;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.cutec.coral.common.constant.RedisKey;
import com.cutec.coral.repository.Media;
import com.cutec.coral.repository.MediaMapper;
import com.cutec.coral.repository.Se;
import com.cutec.coral.repository.SeMapper;
import com.cutec.coral.utils.CopySkipNull;
import com.cutec.coral.utils.Mp4FileFinder;
import com.cutec.coral.utils.RedisGetUtils;
import com.cutec.coral.utils.ToolBox;
import com.cutec.coral.vo.MediaQuery;
import com.cutec.coral.vo.SeeReqData;
import com.cutec.coral.vo.SupReqData;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.lang.reflect.Field;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class CollectService {

    private final RedissonClient redissonClient;
    private final SeMapper seMapper;
    private final MediaMapper mediaMapper;
    private final RedisGetUtils redisGetUtils;
    private final RedisTemplate<String, String> redisTemplate;

    public CollectService(RedissonClient redissonClient, KafkaTemplate<String, String> kafkaTemplate, MediaMapper mediaMapper, RedisGetUtils redisGetUtils, SeMapper seMapper, RedisTemplate<String, String> redisTemplate) {
        this.redissonClient = redissonClient;
        this.kafkaTemplate = kafkaTemplate;
        this.mediaMapper = mediaMapper;
        this.redisGetUtils = redisGetUtils;
        this.seMapper = seMapper;
        this.redisTemplate = redisTemplate;
    }

    /**
     * 当你在「查询 + 回写缓存」时，必须加锁，防止并发写入旧值覆盖新值。
     * 但如果你采用了推荐的「延迟双删 + 不主动写缓存」，就不需要加锁了。
     *
     * @param number
     * @return
     */
    public Media getUseCache(Integer number) {
        if (number == null) {
            return null;
        }
        String mediaRedisKey = RedisKey.mediaCacheKey(number);

        Media media = redisGetUtils.get(mediaRedisKey, Media.class);
        if (media != null) {
            return media;
        }

        RLock lock = redissonClient.getLock("lock:media:" + number);
        try {
            // 加锁：最多等待100秒，成功后自动持有锁10秒
            boolean locked = lock.tryLock(5, 2, TimeUnit.SECONDS);
            if (locked) {
                media = mediaMapper.findByNumber(number);
                redisGetUtils.save(mediaRedisKey, media, 15 * 60);
            } else {
                log.info("未能获取到锁，处理失败逻辑...");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock(); // 释放锁
            }
        }
        return media;
    }

    public List<Media> list(MediaQuery query) {
        if (!ToolBox.isInputSafe(query.getField())) {
            return new ArrayList<>();
        }
        if (!ToolBox.isInputSafe(query.getSort())) {
            return new ArrayList<>();
        }
        return mediaMapper.list((query.getPageNum() - 1) * query.getPageSize(), query.getPageSize(), query.getField(), query.getSort());
    }


    public Page<Media> page(MediaQuery query) {
        QueryWrapper<Media> queryWrapper = new QueryWrapper<>();
        Page<Media> page = new Page<Media>(query.getPageNum(), query.getPageSize()).addOrder(OrderItem.desc(query.getField()));
        return mediaMapper.selectPage(page, queryWrapper);
    }


    /**
     * 说明
     * 多线程同时判断“无记录”然后都去插入 → 唯一键冲突
     * 查和更新不是原子操作 → 数据不一致
     * <p>
     * 方法 | 说明 | 并发支持 | 适合场景
     * 1. 查询 + 更新/插入 | 手动判断是否存在，再执行相应操作 |  易出并发问题 | 低并发、无唯一键
     * 2. 数据库层 Upsert（推荐） | 使用数据库语法保证原子性 |  高 | 高并发、MySQL/PostgreSQL 支持
     * 3. 乐观锁 / 重试机制 | 冲突时重试 |  中高 | 业务允许重试延迟
     * 4. 悲观锁 | 锁记录防止并发写 |  但慢 | 事务小并发低的情况
     *
     * @param src
     * @param media
     * @Lock(LockModeType.PESSIMISTIC_WRITE)
     * @Query("SELECT s FROM UserStats s WHERE s.userId = :userId")
     */
    @Transactional
    public void update(String src, Media media) {
        Media query = mediaMapper.findByNumber(media.getNumber());
        if (query == null) {
            query = new Media();
            BeanUtils.copyProperties(media, query);
            query.setCreateTime(new Date());
            query.setUpdateTime(new Date());
            mediaMapper.insert(query);
        } else {
            media.setUpdateTime(new Date());
            BeanUtils.copyProperties(media, query, CopySkipNull.getNullPropertyNames(media));
            mediaMapper.updateById(query);
        }

        // 缓存双删除
        String key = RedisKey.mediaCacheKey(media.getNumber());
        redisGetUtils.getRedisTemplate().delete(key);
        // 3. 可选：延迟再次删除（解决并发读取旧缓存问题）
        CompletableFuture.runAsync(() -> {
            try {
                Thread.sleep(300); // 延迟 300ms
                redisGetUtils.getRedisTemplate().delete(key);
            } catch (InterruptedException ignored) {
            }
        });

//        redisGetUtils.getRedisTemplate().opsForList().leftPush(RedisKey.logUpdateMedia(), media);
//        kafkaTemplate.send("media-update", JsonUtils.toJson(media));

//        log.info("update media {} {}", src, media.getNumber());

        // 简单 更新redis
        // 脏读：在数据库更新完成但 Redis 尚未更新时，其他线程读取到旧的缓存数据。
        // 更新丢失：并发线程中较晚更新的 Redis 数据可能覆盖较早线程写入的正确数据。
    }

    public List<Media> queryMissTag() {
        QueryWrapper<Media> queryWrapper = new QueryWrapper<>();
        Set<String> members = redisTemplate.opsForSet().members("tag-fix-err");
        if (members != null && !members.isEmpty()) {
            queryWrapper.notIn("number", members);
        }

        queryWrapper
                .and(qw -> qw.isNull("tag").or().eq("tag", "")).orderByDesc("number").last("limit 10");
        return mediaMapper.selectList(queryWrapper);
    }

    public List<Media> queryMissStudio() {
        //ASAA
        QueryWrapper<Media> queryWrapper = new QueryWrapper<>();
        Set<String> members = redisTemplate.opsForSet().members("studio-fix-err");
        if (members != null && !members.isEmpty()) {
            queryWrapper.notIn("number", members);
        }
        queryWrapper.and(qw -> qw.isNull("studio").or().eq("studio", "")).orderByDesc("number").last("limit 10");
        return mediaMapper.selectList(queryWrapper);
    }

    public List<Media> queryMissActors() {
        QueryWrapper<Media> queryWrapper = new QueryWrapper<>();
        Set<String> members = redisTemplate.opsForSet().members("actors-fix-err");
        if (members != null && !members.isEmpty()) {
            queryWrapper.notIn("number", members);
        }
        queryWrapper.and(qw -> qw.isNull("actors").or().eq("actors", "")).orderByDesc("number").last("limit 10");
        return mediaMapper.selectList(queryWrapper);
    }

    public Media updatePart(Integer number, HashMap<String, Object> param) {
        Media mediaQuery = mediaMapper.findByNumber(number);
        if (mediaQuery == null) {
            return null;
        }
        param.forEach((key, value) -> {
            Field field = ReflectionUtils.findField(Media.class, key);
            if (field != null) {
                field.setAccessible(true);
                ReflectionUtils.setField(field, mediaQuery, value);
            }
        });
        mediaMapper.updateById(mediaQuery);
        return mediaQuery;
    }

    public void saveSee(SeeReqData param) {
        if (StringUtils.hasText(param.getTitle())) {
            Integer number = ToolBox.getNumberReg(param.getTitle());
            if (number != null) {
                Media media = new Media();
                media.setNumber(number);
                media.setFromSe(Boolean.TRUE);
                media.setMagnet(param.getMagnet());
                media.setSeDownloadNum(param.getDownloadCount());
                update("se", media);
            }
        }
    }

    public void saveSe(Se param) {
        List<Se> all = seMapper.findByMagnet(param.getMagnet());
        if (all.isEmpty()) {
            param.setCreateTime(new Date());
            param.setUpdateTime(new Date());
            seMapper.insert(param);
        } else {
            Se se = all.get(0);
            BeanUtils.copyProperties(param, se, CopySkipNull.getNullPropertyNames(param));
            se.setUpdateTime(new Date());
            seMapper.updateById(se);
        }
    }


    public void updateFile(String folderPath) {
        HashSet<Integer> numberSet = new HashSet<>();
        List<Path> mp4Files = Mp4FileFinder.findMp4Files(folderPath);
        for (Path mp4File : mp4Files) {
            String name = mp4File.getFileName().toString();
            Integer numberReg = ToolBox.getNumberReg(name);
            if (numberReg == null) {
                continue;
            }
            numberSet.add(numberReg);
        }
        numberSet.forEach(t -> {
            Media media = mediaMapper.findByNumberForUpdate(t);
            if (media != null) {
                media.setDownload(Boolean.TRUE);
                update("f", media);
            } else {
                Media add = new Media();
                add.setNumber(t);
                update("f", add);
            }
        });
    }

    public void nfo() {
        String folderPath = "C:\\repo\\fc2\\all"; // 替换为你的文件夹路径
        HashMap<Integer, List<Path>> fileInfo = new HashMap<>();
        List<Path> mp4Files = Mp4FileFinder.findMp4Files(folderPath);
        for (Path mp4File : mp4Files) {
            String name = mp4File.getFileName().toString();
            Integer numberReg = ToolBox.getNumberReg(name);
            if (fileInfo.get(numberReg) == null) {
                List<Path> list = new ArrayList<>();
                list.add(mp4File);
                fileInfo.put(numberReg, list);
            } else {
                fileInfo.get(numberReg).add(mp4File);
            }
        }
        MediaQuery query = new MediaQuery();
        query.setSort("desc");
        query.setField("number");
        query.setPageSize(Integer.MAX_VALUE);
        List<Media> media = list(query);
        for (Media item : media) {
            try {
                if (fileInfo.containsKey(item.getNumber())) {
                    List<Path> fileNameList = fileInfo.get(item.getNumber());
                    for (Path fileName : fileNameList) {
                        log.info("x {} ", fileName);
                        generateSimpleNfo(item.getNumber() + " " + item.getTitle(), Objects.toString(item.getActors(), ""), Objects.toString(item.getStudio(), ""), Objects.toString(item.getTag(), ""), fileName.getParent() + "/" + ToolBox.getFileNameWithoutExtension(fileName.getFileName().toString()) + ".nfo");
                    }
                }
            } catch (Exception e) {
                log.info("{}", e.getMessage());
            }
        }
    }


    private void generateSimpleNfo(String title, String actors, String studio, String tags, String outputPath) throws Exception {


        // 创建 XML 文档
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.newDocument();
        // 创建根节点 <movie>
        Element movieElement = doc.createElement("movie");
        doc.appendChild(movieElement);
        // 添加 <title> 节点
        Element titleElement = doc.createElement("title");
        titleElement.appendChild(doc.createTextNode(title));
        movieElement.appendChild(titleElement);
        // 添加 <actors> 节点
        Element actorsElement = doc.createElement("actors");
        for (String actor : actors.split(", ")) {
            Element actorElement = doc.createElement("actor");
            Element nameElement = doc.createElement("name");
            nameElement.appendChild(doc.createTextNode(actor));
            actorElement.appendChild(nameElement);
            actorsElement.appendChild(actorElement);
        }
        movieElement.appendChild(actorsElement);
        // 添加 <studio> 节点
        Element studioElement = doc.createElement("studio");
        studioElement.appendChild(doc.createTextNode(studio));
        movieElement.appendChild(studioElement);
        // 添加 <tag> 节点
        for (String tag : tags.split(" ")) {
            Element tagElement = doc.createElement("tag");
            tagElement.appendChild(doc.createTextNode(tag));
            movieElement.appendChild(tagElement);
        }
        // 保存 XML 到文件
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
        transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
        DOMSource domSource = new DOMSource(doc);
        StreamResult streamResult = new StreamResult(new File(outputPath));
        transformer.transform(domSource, streamResult);
        log.info("NFO 文件已生成: {} ", outputPath);
    }

    public void collectSee(SeeReqData param) {
        if (param.getTitle() == null) {
            return;
        }
        if (param.getTitle().toLowerCase().contains("fc2")) {
            saveSee(param);
        } else {
            Se se = new Se();
            BeanUtils.copyProperties(param, se);
            se.setDownload(param.getDownloadCount());
            saveSe(se);
        }
    }

    public void collectSup(List<SupReqData> param) {
        log.info("sup req {}", param);
        for (SupReqData supReqData : param) {
            Media media = new Media();
            media.setNumber(ToolBox.getNumberReg(supReqData.getTitle()));
            if (supReqData.getTitle().contains("[cen")) {
                media.setCen(Boolean.TRUE);
            }
            media.setSupViews(supReqData.getSupViews());
            media.setFromSup(Boolean.TRUE);
            if (media.getNumber() == null) {
                continue;
            }
            update("sup", media);
        }
    }
}
