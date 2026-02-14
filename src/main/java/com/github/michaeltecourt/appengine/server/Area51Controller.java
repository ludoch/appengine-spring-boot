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

import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.google.appengine.api.datastore.DatastoreService;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import java.io.IOException;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

@Controller
@Slf4j
public class Area51Controller {

    /** Do something with the app engine datastore... */
    private final DatastoreService datastoreService;

    @Autowired
    public Area51Controller(DatastoreService datastoreService) {
        this.datastoreService = Objects.requireNonNull(datastoreService);
    }

    /**
     * Home page -> {@literal /WEB-INF/jsp/index.jsp}
     * 
     * @return home page view.
     */
    @RequestMapping("/")
    public ModelAndView home() {
        LOGGER.info("Loading home page...");
        return new ModelAndView("index");
    }

    /**
     * Sample JSON/HTTP service.
     * 
     * @return a list of aliens.
     */
    @RequestMapping(value = "/aliens", method = RequestMethod.GET)
    @ResponseBody
    public AliensResponse aliens() {
        LOGGER.info("Returning a static list of aliens...");
        return AliensResponse.of(Arrays.asList(Alien.of("E.T.", "Home"), Alien.of("Marvin the Martian", "Mars")));
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    @ResponseBody
    public AliensResponse admin(HttpServletRequest request) {
        LOGGER.info("Returning the admin info...");
        Principal userPrincipal = request.getUserPrincipal();
        return AliensResponse.of(
            Collections.singletonList(Alien.of("userPrincipal", userPrincipal == null ? "null" : userPrincipal.toString()))
        );
    }

    @RequestMapping(value = "/stacktrace", method = RequestMethod.GET)
    public void stacktrace(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        LOGGER.info("Printing the stacktrace...");
        response.setContentType("text/plain");
        try (PrintWriter writer = response.getWriter())
        {
            new Throwable("Stacktrace from Area51Controller").printStackTrace(writer);
            writer.flush();
        }
    }

    @RequestMapping(value = "/ee11Test", method = RequestMethod.GET)
    public void ee11Test(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        LOGGER.info("Testing Servlet 6.1 API...");
        response.setContentType("text/plain");
        ServletContext ctx = request.getServletContext();

        try (ServletOutputStream outputStream = response.getOutputStream();)
        {
            PrintWriter writer = new PrintWriter(outputStream, false, StandardCharsets.UTF_8);
            writer.println("Testing Servlet 6.1 API");
            writer.println("ServletContext.getMajorVersion: " + ctx.getMajorVersion());
            writer.println("ServletContext.getMinorVersion: " + ctx.getMinorVersion());
            writer.println("RequestClass: " + request.getClass().getName());
            writer.println("ServletJarLocation: " + HttpServletRequest.class.getProtectionDomain().getCodeSource().getLocation().toString());
            writer.flush();
            try
            {
                String message = "Successfully used 6.1+ API to set req charset.\n";
                outputStream.write(ByteBuffer.wrap(message.getBytes(StandardCharsets.UTF_8)));
                outputStream.flush();
            }
            catch (IOException t)
            {
                t.printStackTrace(writer);
            }
            writer.flush();
        }
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    public static class AliensResponse {
        @NonNull
        private final List<Alien> aliens;
    }

    @Data
    @AllArgsConstructor(staticName = "of")
    // Only used by Jackson through reflection
    @NoArgsConstructor(access = AccessLevel.PRIVATE, force = true)
    public static class Alien {
        @NonNull
        @NotEmpty
        private final String name;
        @NonNull
        @NotEmpty
        private final String home;
    }

}
