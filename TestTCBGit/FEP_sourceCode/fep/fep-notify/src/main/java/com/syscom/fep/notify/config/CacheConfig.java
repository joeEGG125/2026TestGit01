package com.syscom.fep.notify.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeUnit;

@EnableCaching
@Configuration
public class CacheConfig {
    @Value("${cache.expireAfterWrite:300}")
    private int expireAfterWrite;

    @Value("${cache.maximumSize:2000}")
    private int maximumSize;

    @Bean
    public CacheManager cacheManager() {
        CaffeineCacheManager cacheManager = new CaffeineCacheManager();
        // Caffeine 配置
        com.syscom.fep.common.log.LogHelperFactory.getGeneralLogger().info("Cache expireAfterWrite:", expireAfterWrite, "s, maximumSize:", maximumSize);
        Caffeine<Object, Object> caffeine = Caffeine.newBuilder()
                // 最後一次寫入後經過固定時間過期
                .expireAfterWrite(expireAfterWrite, TimeUnit.SECONDS)
                // 快取的最大條數
                .maximumSize(maximumSize);
        // 開啟 Cache 操作數據統計
        //.recordStats();
        cacheManager.setCaffeine(caffeine);
        return cacheManager;
    }
}
