package com.vince.web.manager;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.concurrent.TimeUnit;

/**
 * 缓存操作
 */
@Component
public class CacheManager {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 本地缓存
     */
    Cache<String, Object> localCache = Caffeine.newBuilder()
            .expireAfterWrite(100, TimeUnit.MINUTES)
            .maximumSize(10_000)
            .build();


    /**
     * 写入缓存
     *
     * @param key
     * @param value
     */
    public void put(String key, Object value) {
        localCache.put(key, value);
        redisTemplate.opsForValue().set(key, value, 100, TimeUnit.MINUTES);
    }

    /**
     * 读缓存
     *
     * @param key
     * @return
     */
    public Object get(String key) {
        // 先从本地缓存中获取
        // 查找一个缓存元素， 没有查找到的时候返回null
        Object value = localCache.getIfPresent(key);
        if (value != null) {
            return value;
        }
        // 本地缓存未命中，尝试从Redis获取
        value = redisTemplate.opsForValue().get(key);
        if (value != null) {
            // 将redis 的值写入本地缓存
            localCache.put(key, value);
        }
        return value;
    }

    /**
     * 删除缓存
     *
     * @param key
     */
    public void delete(String key) {
        // 移除一个缓存元素
        localCache.invalidate(key);
        redisTemplate.delete(key);
    }


}
