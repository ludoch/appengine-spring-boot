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

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.authentication.preauth.PreAuthenticatedAuthenticationToken;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Modernized Spring Security configuration for Google App Engine.
 * Integrates GAE UserService roles (USER/ADMIN) with Spring Security.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class GaeSecurityConfig {

    private final UserService userService;

    public GaeSecurityConfig(UserService userService) {
        this.userService = userService;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .exceptionHandling(ex -> ex
                // Trigger App Engine Login flow when authentication is required
                .authenticationEntryPoint((request, response, authException) -> {
                    String loginUrl = userService.createLoginURL(request.getRequestURI());
                    response.sendRedirect(loginUrl);
                })
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/index.jsp", "/aliens", "/api/gae/status", "/api/gae/cached-calculation").permitAll()
                .requestMatchers("/_ah/**").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/swagger-ui.html").permitAll()
                .requestMatchers("/actuator/**").permitAll()
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/api/gae/tasks/**").hasRole("ADMIN")
                .anyRequest().permitAll()
            )
            .addFilterBefore(new GaeAuthenticationFilter(userService), 
                org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public ServletContextInitializer initializer() {
        return servletContext -> {
            servletContext.addListener(new ServletContextListener() {
                @Override
                public void contextInitialized(ServletContextEvent sce) {
                    System.err.println("GAE Context Initialized: " + sce);
                }
            });
        };
    }

    public static class GaeAuthenticationFilter extends OncePerRequestFilter {
        private final UserService userService;

        public GaeAuthenticationFilter(UserService userService) {
            this.userService = userService;
        }

        @Override
        protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                throws ServletException, IOException {
            
            try {
                User gaeUser = userService.getCurrentUser();
                if (gaeUser != null) {
                    List<GrantedAuthority> authorities = new ArrayList<>();
                    authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
                    if (userService.isUserAdmin()) {
                        authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
                    }

                    PreAuthenticatedAuthenticationToken authentication = 
                        new PreAuthenticatedAuthenticationToken(gaeUser, null, authorities);
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            } catch (Exception e) {
                // GAE services might not be fully initialized in some test/local contexts
            }

            filterChain.doFilter(request, response);
        }
    }
}
