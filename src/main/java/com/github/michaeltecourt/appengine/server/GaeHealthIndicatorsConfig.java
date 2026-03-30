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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.memcache.MemcacheService;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Custom Actuator Health Indicators for GAE Services.
 */
@Configuration
public class GaeHealthIndicatorsConfig {

    @Bean
    public HealthIndicator datastoreHealthIndicator(DatastoreService datastoreService) {
        return () -> {
            try {
                datastoreService.prepare(new com.google.appengine.api.datastore.Query("__Stat_Total__")).asIterable();
                return Health.up().withDetail("service", "Google Cloud Datastore").build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }

    @Bean
    public HealthIndicator memcacheHealthIndicator(MemcacheService memcacheService) {
        return () -> {
            try {
                memcacheService.getStatistics();
                return Health.up().withDetail("service", "GAE Memcache").build();
            } catch (Exception e) {
                return Health.down(e).build();
            }
        };
    }
}
