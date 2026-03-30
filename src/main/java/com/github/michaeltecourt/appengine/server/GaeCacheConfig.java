/*
 * Copyright 2024 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.michaeltecourt.appengine.server;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.jcache.JCacheCacheManager;
import org.springframework.cache.support.NoOpCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.cache.Caching;
import javax.cache.spi.CachingProvider;
import java.util.Properties;

/**
 * Plugs App Engine Memcache into Spring's @Cacheable abstraction using JSR-107.
 */
@Configuration
@EnableCaching
public class GaeCacheConfig {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(GaeCacheConfig.class);

    @Bean
    public CacheManager cacheManager() {
        try {
            CachingProvider provider = Caching.getCachingProvider();
            if (provider == null) {
                log.warn("No JCache CachingProvider found. Falling back to NoOpCacheManager.");
                return new NoOpCacheManager();
            }
            
            javax.cache.CacheManager jCacheManager = provider.getCacheManager(
                provider.getDefaultURI(), 
                provider.getDefaultClassLoader(), 
                new Properties()
            );
            return new JCacheCacheManager(jCacheManager);
        } catch (Exception e) {
            log.warn("Failed to initialize GAE JCache Provider: {}. Falling back to NoOpCacheManager.", e.getMessage());
            return new NoOpCacheManager();
        }
    }
}
