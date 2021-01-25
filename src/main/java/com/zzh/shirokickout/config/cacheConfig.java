package com.zzh.shirokickout.config;

import com.zzh.shirokickout.cache.shiroCacheManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import java.time.Duration;

/**
 * @author 张子行
 * @class 缓存管理器
 */
@Configuration
public class cacheConfig {
    @Autowired
    RedisConnectionFactory redisConnectionFactory;

    @Bean(name = "shiroCacheManager")
    public shiroCacheManager shiroCacheManager() {
        RedisCacheConfiguration conf = RedisCacheConfiguration.defaultCacheConfig();
        //shiro配置的缓存管理器是这个，这里是设置登录的过期时间，及其session的过期时间，按秒为单位
        conf = conf.entryTtl(Duration.ofSeconds(300000));
        RedisCacheManager cacheManager = RedisCacheManager
                .builder(redisConnectionFactory)
                .cacheDefaults(conf)
                .build();
        shiroCacheManager shiroCacheManager = new shiroCacheManager(cacheManager);
        return shiroCacheManager;
    }

    @Bean(name = "redisCacheManager")
    public RedisCacheManager redisCacheManager() {
        RedisCacheConfiguration cacheConfig = RedisCacheConfiguration.defaultCacheConfig();
        RedisCacheManager redisCacheManager = RedisCacheManager.builder(redisConnectionFactory)
                .cacheDefaults(cacheConfig)
                .build();
        return redisCacheManager;
    }
}
