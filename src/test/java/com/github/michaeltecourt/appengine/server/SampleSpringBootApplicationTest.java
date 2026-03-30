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

import com.google.appengine.tools.development.testing.LocalMemcacheServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import static org.hamcrest.Matchers.containsString;

/**
 * Modernized Spring Boot test using JUnit 5 and manual MockMvc setup with Security.
 */
@SpringBootTest
class SampleSpringBootApplicationTest {

    private final LocalServiceTestHelper helper =
        new LocalServiceTestHelper(
            new LocalUserServiceTestConfig(),
            new LocalMemcacheServiceTestConfig()
        );

    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext webApplicationContext;

    @BeforeEach
    void setup() {
        helper.setUp();
        this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext)
            .apply(springSecurity())
            .build();
    }

    @AfterEach
    void tearDown() {
        helper.tearDown();
    }

    @Test
    void aliens() throws Exception {
        mockMvc.perform(get("/aliens"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.aliens[0].name").value("E.T."))
            .andExpect(jsonPath("$.aliens[0].home").value("Home"))
            .andExpect(jsonPath("$.aliens[1].name").value("Marvin the Martian"))
            .andExpect(jsonPath("$.aliens[1].home").value("Mars"));
    }

    @Test
    void gaeStatus() throws Exception {
        mockMvc.perform(get("/api/gae/status"))
            .andExpect(status().isOk())
            .andExpect(content().string("GAE APIs Integrated Successfully with Spring Boot 4.0.x on Virtual Threads"));
    }

    @Test
    void unauthenticatedAccessRedirectsToLogin() throws Exception {
        // Accessing a secured endpoint without a session should redirect to GAE Login
        mockMvc.perform(post("/api/gae/datastore/entities"))
            .andExpect(status().is3xxRedirection())
            .andExpect(header().string("Location", containsString("/_ah/login")));
    }

    @Test
    @WithMockUser(roles = "USER")
    void secureEndpointAllowedWithUser() throws Exception {
        mockMvc.perform(post("/api/gae/datastore/entities"))
            .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(roles = "USER")
    void adminEndpointForbiddenForUser() throws Exception {
        // Authenticated but wrong role -> 403 Forbidden
        mockMvc.perform(post("/api/gae/tasks/deferred").param("payload", "test"))
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEndpointAllowedForAdmin() throws Exception {
        mockMvc.perform(post("/api/gae/tasks/deferred").param("payload", "test"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("deferred"));
    }
}
