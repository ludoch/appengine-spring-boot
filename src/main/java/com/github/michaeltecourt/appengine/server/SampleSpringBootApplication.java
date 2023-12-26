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

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * Spring Boot application entry point. Because it extends
 * {@link SpringBootServletInitializer} it should be detected without a web.xml
 * file (GAE team is fixing this).
 * 
 * @author michaeltecourt
 */
@SpringBootApplication
public class SampleSpringBootApplication extends SpringBootServletInitializer {

    public static void main(String[] args) {
        // This main method is not used by Google App Engine, which only needs
        // an empty @SpringBootApplication class from SpringBootServletInitializer.
        SpringApplication.run(SampleSpringBootApplication.class, args);
    }

}
