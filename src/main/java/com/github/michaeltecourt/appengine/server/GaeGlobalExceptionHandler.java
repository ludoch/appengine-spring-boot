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

import com.google.appengine.api.datastore.DatastoreFailureException;
import com.google.appengine.api.datastore.DatastoreTimeoutException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;

/**
 * Standardizes GAE-specific error responses for all controllers.
 */
@ControllerAdvice
public class GaeGlobalExceptionHandler {

    @ExceptionHandler({DatastoreTimeoutException.class, DatastoreFailureException.class})
    public ResponseEntity<Object> handleDatastoreError(Exception ex) {
        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of(
            "timestamp", Instant.now(),
            "status", 503,
            "error", "Datastore Unavailable",
            "message", "Google Cloud Datastore is experiencing issues. Please try again later.",
            "type", ex.getClass().getSimpleName()
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public void handleAccessDenied(AccessDeniedException ex) {
        throw ex; // Let Spring Security handle it
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleGeneralError(Exception ex) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Map.of(
            "timestamp", Instant.now(),
            "status", 500,
            "error", "Internal Server Error",
            "message", ex.getMessage()
        ));
    }
}
