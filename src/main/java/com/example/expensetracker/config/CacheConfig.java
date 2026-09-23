package com.example.expensetracker.config;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.time.Duration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CacheConfig {
    @Bean CacheManager cacheManager() {
        CaffeineCacheManager manager = new CaffeineCacheManager("summaries");
        manager.setCaffeine(Caffeine.newBuilder().maximumSize(2000).expireAfterWrite(Duration.ofMinutes(10)));
        return manager;
    }
}
