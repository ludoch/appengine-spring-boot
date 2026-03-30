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

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.Stats;
import com.google.appengine.api.taskqueue.DeferredTask;
import com.google.appengine.api.taskqueue.Queue;
import com.google.appengine.api.taskqueue.TaskOptions;
import com.google.appengine.api.urlfetch.HTTPResponse;
import com.google.appengine.api.urlfetch.URLFetchService;
import com.google.appengine.api.users.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URL;
import java.security.Principal;
import java.util.*;

/**
 * Modernized Spring Boot Controller for App Engine APIs.
 */
@RestController
@RequestMapping("/api/gae")
public class AppEngineApiController {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AppEngineApiController.class);

    private final DatastoreService datastoreService;
    private final MemcacheService memcacheService;
    private final UserService userService;
    private final URLFetchService urlFetchService;
    private final Queue defaultQueue;

    public AppEngineApiController(DatastoreService datastoreService, MemcacheService memcacheService, 
                                UserService userService, URLFetchService urlFetchService, Queue defaultQueue) {
        this.datastoreService = datastoreService;
        this.memcacheService = memcacheService;
        this.userService = userService;
        this.urlFetchService = urlFetchService;
        this.defaultQueue = defaultQueue;
    }

    @GetMapping("/user")
    public Map<String, Object> getUserInfo(Principal principal, HttpServletRequest request) {
        Map<String, Object> info = new HashMap<>();
        if (principal != null) {
            info.put("name", principal.getName());
            info.put("logoutUrl", userService.createLogoutURL(request.getRequestURI()));
            info.put("isAdmin", userService.isUserAdmin());
        } else {
            info.put("loginUrl", userService.createLoginURL(request.getRequestURI()));
        }
        return info;
    }

    @PostMapping("/datastore/entities")
    @PreAuthorize("hasRole('USER')")
    public Map<String, Object> addEntities(@RequestParam(defaultValue = "1") int count) {
        List<Entity> entities = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < count; i++) {
            Entity entity = new Entity("Car");
            entity.setProperty("brand", List.of("Toyota", "Honda", "Tesla").get(random.nextInt(3)));
            entity.setProperty("color", List.of("Red", "Blue", "Green").get(random.nextInt(3)));
            entity.setProperty("createdAt", new Date());
            entities.add(entity);
        }
        datastoreService.put(entities);
        return Map.of("added", count, "kind", "Car");
    }

    @GetMapping("/cached-calculation")
    @Cacheable(value = "heavy-tasks", key = "#input")
    public Map<String, Object> heavyCalculation(@RequestParam String input) {
        log.info("Performing heavy calculation for: {} (This should only log once per input)", input);
        try { Thread.sleep(100); } catch (InterruptedException e) {}
        return Map.of("input", input, "result", "Calculated at " + new Date());
    }

    @GetMapping("/datastore/count")
    public Map<String, Object> countEntities(@RequestParam(defaultValue = "Car") String kind) {
        Query query = new Query(kind);
        int count = datastoreService.prepare(query).countEntities(FetchOptions.Builder.withDefaults());
        return Map.of("kind", kind, "count", count);
    }

    @GetMapping("/memcache/stats")
    public Map<String, Object> getMemcacheStats() {
        Stats stats = memcacheService.getStatistics();
        return Map.of(
            "hits", stats.getHitCount(),
            "misses", stats.getMissCount(),
            "itemCount", stats.getItemCount()
        );
    }

    @PostMapping("/tasks/deferred")
    @PreAuthorize("hasRole('ADMIN')")
    public Map<String, Object> addDeferredTask(@RequestParam String payload) {
        log.info("Scheduling a deferred task with payload: {}", payload);
        defaultQueue.add(TaskOptions.Builder.withPayload(new SampleDeferredTask(payload)));
        return Map.of("status", "deferred", "payload", payload);
    }

    @GetMapping("/fetch")
    public Map<String, Object> fetchUrl(@RequestParam String url) throws IOException {
        HTTPResponse response = urlFetchService.fetch(new URL(url));
        return Map.of(
            "url", url,
            "responseCode", response.getResponseCode(),
            "contentLength", response.getContent() != null ? response.getContent().length : 0
        );
    }

    @GetMapping("/status")
    public String status() {
        return "GAE APIs Integrated Successfully with Spring Boot 4.0.x on Virtual Threads";
    }

    @GetMapping("/_ah/warmup")
    public void warmup() {
        log.info("Warmup request received. Priming caches and context...");
    }

    public static class SampleDeferredTask implements DeferredTask {
        private static final long serialVersionUID = 1L;
        private final String data;

        public SampleDeferredTask(String data) {
            this.data = data;
        }

        @Override
        public void run() {
            System.out.println("Processing background task with data: " + data);
        }
    }
}
