package com.cutec.common;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class RedisKey {

    public static String mediaCacheKey(Integer id) {
        return "media:" + id;
    }

    public static String logUpdateMedia() {
        return "daily:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":update";
    }

    public static String logAddMedia() {
        return "daily:" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + ":add";
    }

}
