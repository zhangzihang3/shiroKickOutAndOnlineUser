package com.zzh.shirokickout.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.shiro.cache.Cache;
import org.apache.shiro.cache.CacheException;
import org.springframework.data.redis.cache.RedisCacheManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;

import static java.lang.System.out;

/**
 * @author 张子行
 * @class 自定义shiro缓存
 */
@Slf4j
public class shiroCache<k, v> implements Cache<k, v> {
    private org.springframework.cache.Cache springCache;
    private RedisCacheManager redisCacheManager;
    private String cacheName;

    public shiroCache(RedisCacheManager redisCacheManager, String cacheName) {
        this.springCache = redisCacheManager.getCache(cacheName);
        this.redisCacheManager = redisCacheManager;
        this.cacheName = cacheName;
    }

    /**
     * @param
     * @method 缓存的获取
     * 在访问需要权限的页面时会，从缓存中拉去权限信息，看你是否有权访问
     */
    @Override
    public v get(k k) throws CacheException {
        //log.info("shiroCache-get："+cacheName);
        org.springframework.cache.Cache.ValueWrapper valueWrapper = springCache.get(k);
        if (valueWrapper != null) {
            return (v) valueWrapper.get();
        }
        return null;
    }

    /**
     * @param
     * @method 缓存的放入，在会话开始的时候会把session信息放入缓存，
     * 在subject.login()的时候会把授权信息放入缓存
     */
    @Override
    public v put(k k, v v) throws CacheException {
        //log.info("shiroCache+put："+cacheName);
        springCache.put(k, v);
        return v;
    }

    /**
     * @param
     * @method 缓存的清除单个（在Subject subject = SecurityUtils.getSubject();
     * subject.logout();的时候会清除相应的授权认证缓存,我测试的时候session缓存也会被清除）
     */
    @Override
    public v remove(k k) throws CacheException {
        log.info("shiroCache+remove："+cacheName+k);
        v v = this.get(k);
        springCache.evict(k);
        return v;
    }

    /**
     * @param
     * @method 缓存的全部删除
     */
    @Override
    public void clear() throws CacheException {
        //log.info("shiroCache+clear： "+cacheName);
        springCache.clear();
    }

    /**
     * @param
     * @method 缓存的个数
     */
    @Override
    public int size() {
        //log.info("shiroCache+size： "+cacheName);
        int size = this.keys().size();
        return size;
    }

    /**
     * @param
     * @method 缓存的所有key
     */
    @Override
    public Set<k> keys() {
        //log.info("shiroCache+keys： "+cacheName);
        Collection<String> cacheNames = redisCacheManager.getCacheNames();
        return (Set<k>) cacheNames;
    }

    /**
     * @param
     * @method 缓存的所有values
     */
    @Override
    public Collection<v> values() {
        //log.info("shiroCache+values（）： "+cacheName);
        ArrayList<v> data = new ArrayList<>();
        Set<k> keys = this.keys();
        for (k k : keys) {
            data.add(this.get(k));
        }
        return data;
    }
}
