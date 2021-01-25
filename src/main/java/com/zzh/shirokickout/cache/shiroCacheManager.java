package com.zzh.shirokickout.cache;

import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.apache.shiro.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheManager;

public class shiroCacheManager implements CacheManager {

    private static RedisCacheManager redisCacheManager;

    public shiroCacheManager(RedisCacheManager redisCacheManager) {
        this.redisCacheManager = redisCacheManager;
    }

    @Override
    public <K, V> Cache<K, V> getCache(String cacheName) throws CacheException {
        return new shiroCache(redisCacheManager, cacheName);
    }
}
