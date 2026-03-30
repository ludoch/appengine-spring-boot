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
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotEmpty;
import lombok.NonNull;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.security.Principal;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Modernized Controller using Java 25 Records.
 */
@Controller
public class Area51Controller {

    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(Area51Controller.class);

    private final DatastoreService datastoreService;

    public Area51Controller(DatastoreService datastoreService) {
        this.datastoreService = Objects.requireNonNull(datastoreService);
    }

    @RequestMapping("/")
    public ModelAndView home() {
        log.info("Loading home page...");
        return new ModelAndView("index");
    }

    @RequestMapping(value = "/aliens", method = RequestMethod.GET)
    @ResponseBody
    public AliensResponse aliens() {
        log.info("Returning a static list of aliens...");
        return new AliensResponse(List.of(new Alien("E.T.", "Home"), new Alien("Marvin the Martian", "Mars")));
    }

    @RequestMapping(value = "/admin", method = RequestMethod.GET)
    @ResponseBody
    public AliensResponse admin(HttpServletRequest request) {
        log.info("Returning the admin info...");
        Principal userPrincipal = request.getUserPrincipal();
        return new AliensResponse(
            Collections.singletonList(new Alien("userPrincipal", userPrincipal == null ? "null" : userPrincipal.toString()))
        );
    }

    @RequestMapping(value = "/stacktrace", method = RequestMethod.GET)
    public void stacktrace(HttpServletRequest request, HttpServletResponse response) throws Exception
    {
        log.info("Printing the stacktrace...");
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
        log.info("Testing Servlet 6.1 API...");
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

    public record AliensResponse(@NonNull List<Alien> aliens) {}

    public record Alien(@NonNull @NotEmpty String name, @NonNull @NotEmpty String home) {}

}
