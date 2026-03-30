/*
 * Copyright © 2017 the original authors (@michaeltecourt)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.github.michaeltecourt.appengine.server;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.QueueFactory;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.urlfetch.URLFetchServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Declares App Engine services as Spring beans and bridges JUL logs.
 */
@Configuration
public class AppEngineConfig {

    static {
        // Bridge GAE System Logs (JUL) to SLF4J
        org.slf4j.bridge.SLF4JBridgeHandler.removeHandlersForRootLogger();
        org.slf4j.bridge.SLF4JBridgeHandler.install();
    }

    @Bean
    @ConditionalOnMissingBean
    public DatastoreService datastoreService() {
        return DatastoreServiceFactory.getDatastoreService();
    }

    @Bean
    @ConditionalOnMissingBean
    public UserService userService() {
        return UserServiceFactory.getUserService();
    }

    @Bean
    @ConditionalOnMissingBean
    public MemcacheService memcacheService() {
        return MemcacheServiceFactory.getMemcacheService();
    }

    @Bean
    @ConditionalOnMissingBean
    public URLFetchService urlFetchService() {
        return URLFetchServiceFactory.getURLFetchService();
    }

    @Bean
    @ConditionalOnMissingBean
    public Queue defaultQueue() {
        return QueueFactory.getDefaultQueue();
    }
}
