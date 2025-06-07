package com.cutec.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Data
@Component
public class RedisGetUtils {

    private final ObjectMapper objectMapper = new ObjectMapper();

    private final RedisTemplate<String, String> redisTemplate;

    public <T> T get(String key, Class<T> clazz) {
        String o = redisTemplate.opsForValue().get(key);
        if (o != null) {
            try {
                return objectMapper.readValue(o, clazz);
            } catch (JsonProcessingException e) {
                return null;
            }
        }
        return null;
    }

    public void save(String key, Object data, int time) {
        String str = "";
        try {
            str = objectMapper.writeValueAsString(data);
        } catch (JsonProcessingException ignored) {

        }
        redisTemplate.opsForValue().set(key, str, time, TimeUnit.SECONDS);
    }
}
