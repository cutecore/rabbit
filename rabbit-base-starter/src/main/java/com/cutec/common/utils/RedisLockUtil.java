package com.cutec.common.utils;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
public class RedisLockUtil {

    private static final String LOCK_PREFIX = "redis_lock:";
    private static final long DEFAULT_EXPIRE_TIME = 30; // 默认锁过期时间，单位秒

    private final RedisTemplate<String, String> redisTemplate;

    public RedisLockUtil(RedisTemplate<String, String> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * 获取锁，如果获取成功则返回true，否则返回false。
     *
     * @param key        锁的名称
     * @param expireTime 锁的过期时间（单位：秒）
     * @return 是否成功获取锁
     */
    public boolean lock(String key, long expireTime) {
        String lockKey = LOCK_PREFIX + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(lockKey, "locked");
        if (success != null && success) {
            redisTemplate.expire(lockKey, expireTime, TimeUnit.SECONDS);
            return true;
        }
        return false;
    }

    /**
     * 获取锁，使用默认过期时间。
     *
     * @param key 锁的名称
     * @return 是否成功获取锁
     */
    public boolean lock(String key) {
        return lock(key, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 释放锁。
     *
     * @param key 锁的名称
     */
    public void unlock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.delete(lockKey);
    }
}
