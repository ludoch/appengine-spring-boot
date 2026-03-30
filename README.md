# 🚀 App Engine + Spring Boot 4.0.x: The Modernization Blueprint

[![Java 25](https://img.shields.io/badge/Java-25-blue.svg)](https://openjdk.org/projects/jdk/25/)
[![Spring Boot 4.0.3](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![App Engine](https://img.shields.io/badge/App%20Engine-Standard-orange.svg)](https://cloud.google.com/appengine)

This project is a **best-in-class reference architecture** for running modern Spring Boot applications on Google App Engine (Standard) while leveraging high-performance Java 25 features and GAE Legacy APIs.

---

## 🌟 Why This Matters
Transitioning legacy GAE apps to modern Java runtimes often feels like a trade-off. This sample proves you can have it all: **Jakarta EE 11 compatibility**, **Virtual Threads for scale**, and **seamless Google Cloud integration** without the boilerplate of legacy XML configurations.

## 🛠 Pro-Grade Modernizations

### 🏎 High Performance & Caching
*   **Virtual Threads (Loom)**: Pre-configured to handle massive concurrency for blocking GAE API calls (Datastore, URLFetch) without thread pool exhaustion.
*   **Spring Cache (@Cacheable)**: Seamlessly integrated with **GAE Memcache** via JSR-107.
*   **Warmup Handling**: Implements `/_ah/warmup` to prime the Spring context and caches.

### 🔐 Modern Security (Goodbye `web.xml`)
*   **GAE + Spring Security 7.x**: Bridges Google Accounts directly to Spring roles.
*   **Automatic Role Mapping**: Google Project Admins are automatically mapped to `ROLE_ADMIN`.
*   **Annotation-Driven**: Use standard `@PreAuthorize` instead of legacy XML security constraints.

### 📊 Observability & Monitoring
*   **Custom Health Indicators**: Deep monitoring of Datastore and Memcache via `/actuator/health`.
*   **GAE Metrics in Actuator**: Real-time GAE statistics exposed via Micrometer.
*   **Unified Logging**: GAE system logs (JUL) are bridged into SLF4J/Logback.
*   **OpenAPI 3 / Swagger**: Auto-generated documentation and interactive UI at `/swagger-ui.html`.

---

## 🚀 How to test locally

```bash
mvn clean package appengine:run
```

Once started, visit `http://localhost:8080/` to see the home page. Try the following URLs:

### Sample Application Links
*   **Home (JSP)**: `http://localhost:8080/`
*   **Aliens (JSON)**: `http://localhost:8080/aliens`
*   **Admin Info**: `http://localhost:8080/admin` (Requires `ROLE_ADMIN` / GAE Admin)
*   **GAE API Status**: `http://localhost:8080/api/gae/status`
*   **Swagger UI**: `http://localhost:8080/swagger-ui.html`

### Actuator Endpoints (Monitoring)
*   **Health**: `http://localhost:8080/actuator/health`
*   **Metrics**: `http://localhost:8080/actuator/metrics`
*   **Environment**: `http://localhost:8080/actuator/env`
*   **Beans**: `http://localhost:8080/actuator/beans`
*   **Thread Dump**: `http://localhost:8080/actuator/threaddump`
*   **Loggers**: `http://localhost:8080/actuator/loggers`

---

## 🏗 Modernized Architecture

### Injecting GAE Services
No more static factories. Inject GAE services as standard Spring Beans:
```java
@RestController
public class MyController {
    private final DatastoreService datastore; // Auto-wired from AppEngineConfig
}
```

### Role-Based Access Control
Leverage standard Spring Security annotations to protect your GAE application:
```java
@PostMapping("/admin-only")
@PreAuthorize("hasRole('ADMIN')")
public String adminAction() {
    return "Only Google App Engine Admins can see this!";
}
```

---

## 📦 Deployment
```bash
mvn appengine:deploy
```

## 🧪 Technical Stack
*   **Runtime**: App Engine Standard (Java 25)
*   **Framework**: Spring Boot 4.0.3 (Jakarta EE 11)
*   **Caching**: JSR-107 / GAE Memcache
*   **Web Server**: Jetty 12.1
*   **Testing**: JUnit 5 + MockMvc + GAE LocalServiceTestHelper
