# Gateway Service Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Create a Spring Cloud Gateway service as the single external entry point, replacing Nginx reverse proxy.

**Architecture:** Gateway on port 8080 routes API calls to `zhangyuan-api:8088` and `system-service:8081` via Nacos service discovery, and proxies web frontend directly to `web:3000`. Nginx is removed — admin SPA static files remain served by its own container on port 81.

**Tech Stack:** Spring Cloud Gateway 2023.0.x, Nacos Discovery, LoadBalancer, Java 21

---

### Task 1: Create Gateway build.gradle and settings.gradle

**Files:**
- Create: `backend/gateway/build.gradle`
- Create: `backend/gateway/settings.gradle`

- [ ] **Step 1: Create `backend/gateway/settings.gradle`**

```groovy
pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
    repositories {
        mavenCentral()
    }
}

rootProject.name = 'gateway'
```

- [ ] **Step 2: Create `backend/gateway/build.gradle`**

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.3.5'
    id 'io.spring.dependency-management' version '1.1.6'
}

group = 'com.zhangyuan'
version = '0.1.0-SNAPSHOT'

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

dependencyManagement {
    imports {
        mavenBom 'org.springframework.cloud:spring-cloud-dependencies:2023.0.6'
        mavenBom 'com.alibaba.cloud:spring-cloud-alibaba-dependencies:2023.0.3.4'
    }
}

dependencies {
    implementation 'org.springframework.cloud:spring-cloud-starter-gateway'
    implementation 'com.alibaba.cloud:spring-cloud-starter-alibaba-nacos-discovery'
    implementation 'org.springframework.cloud:spring-cloud-starter-loadbalancer'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'org.junit.platform:junit-platform-launcher'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

- [ ] **Step 3: Commit**

```bash
git add backend/gateway/build.gradle backend/gateway/settings.gradle
git commit -m "feat(gateway): add build configuration with Spring Cloud Gateway and Nacos"
```

---

### Task 2: Create Gateway Application class and configuration

**Files:**
- Create: `backend/gateway/src/main/java/com/zhangyuan/gateway/GatewayApplication.java`
- Create: `backend/gateway/src/main/resources/application.yml`

- [ ] **Step 1: Create directory structure and `GatewayApplication.java`**

```bash
mkdir -p backend/gateway/src/main/java/com/zhangyuan/gateway
mkdir -p backend/gateway/src/main/resources
```

```java
package com.zhangyuan.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class GatewayApplication {

    public static void main(String[] args) {
        SpringApplication.run(GatewayApplication.class, args);
    }
}
```

- [ ] **Step 2: Create `backend/gateway/src/main/resources/application.yml`**

```yaml
server:
  port: ${SERVER_PORT:8080}

spring:
  application:
    name: gateway
  cloud:
    nacos:
      discovery:
        server-addr: ${NACOS_HOST:localhost}:${NACOS_PORT:8848}
        username: ${NACOS_USERNAME:nacos}
        password: ${NACOS_PASSWORD:chengccn}
    gateway:
      routes:
        # System service admin API
        - id: system-admin
          uri: lb://system-service
          predicates:
            - Path=/admin/system/**
        # System service public API
        - id: system-api
          uri: lb://system-service
          predicates:
            - Path=/api/system/**
        # System service DDD settings (CMS seed data)
        - id: system-ddd
          uri: lb://system-service
          predicates:
            - Path=/api/ddd/settings/**
        # Main API — admin routes
        - id: api-admin
          uri: lb://zhangyuan-api
          predicates:
            - Path=/admin/**
        # Main API — public routes
        - id: api-public
          uri: lb://zhangyuan-api
          predicates:
            - Path=/api/**
        # Main API — actuator
        - id: api-actuator
          uri: lb://zhangyuan-api
          predicates:
            - Path=/actuator/**
        # Web frontend (Nuxt, direct URI — not registered with Nacos)
        - id: web
          uri: http://web:3000
          predicates:
            - Path=/**

logging:
  level:
    org.springframework.cloud.gateway: INFO
```

- [ ] **Step 3: Commit**

```bash
git add backend/gateway/src/
git commit -m "feat(gateway): add application class and routing configuration"
```

---

### Task 3: Create Gateway Dockerfile

**Files:**
- Create: `backend/gateway/Dockerfile`

- [ ] **Step 1: Create `backend/gateway/Dockerfile`**

```dockerfile
FROM gradle:8.10-jdk21 AS build
WORKDIR /app
COPY build.gradle settings.gradle ./
COPY src ./src
RUN gradle build -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENV JAVA_OPTS="-Xms128m -Xmx256m"
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
```

- [ ] **Step 2: Commit**

```bash
git add backend/gateway/Dockerfile
git commit -m "feat(gateway): add Dockerfile for container build"
```

---

### Task 4: Update zhangyuan-api server port

**Files:**
- Modify: `backend/api/src/main/resources/application.yml:1`

- [ ] **Step 1: Change server port from 8080 to 8088**

In `backend/api/src/main/resources/application.yml`, change line 2:
```yaml
# Before:
server:
  port: ${SERVER_PORT:8080}

# After:
server:
  port: ${SERVER_PORT:8088}
```

- [ ] **Step 2: Commit**

```bash
git add backend/api/src/main/resources/application.yml
git commit -m "refactor(api): change server port to 8088, gateway takes 8080"
```

---

### Task 5: Update Docker Compose — add gateway, remove nginx, update api port

**Files:**
- Modify: `infrastructure/docker/docker-compose.app.yml`

- [ ] **Step 1: Edit `infrastructure/docker/docker-compose.app.yml`**

Changes needed:
1. Add `gateway` service (before `web` service)
2. Remove `nginx` service
3. Change `api` port mapping from `8080:8080` to `8088:8088`

Add gateway service:
```yaml
  gateway:
    build:
      context: ../../backend/gateway
      dockerfile: Dockerfile
    container_name: zhangyuan-gateway
    environment:
      SERVER_PORT: 8080
      NACOS_HOST: nacos
      NACOS_PORT: 8848
      NACOS_USERNAME: nacos
      NACOS_PASSWORD: chengccn
    ports:
      - "8080:8080"
    depends_on:
      - api
      - system-service
      - web
    restart: unless-stopped
    networks:
      - zhangyuan-dev
```

Change api service port:
```yaml
# Before:
    ports:
      - "8080:8080"

# After:
    ports:
      - "8088:8088"
```

Remove nginx service entirely (the `nginx:` block including `image:`, `container_name`, `ports:`, `volumes:`, `depends_on:`, `restart:`, `networks:`).

- [ ] **Step 2: Commit**

```bash
git add infrastructure/docker/docker-compose.app.yml
git commit -m "feat(infra): add gateway container, remove nginx, update api port to 8088"
```

---

### Task 6: Update frontend Vite proxy target

**Files:**
- Modify: `frontend/admin/vite.config.ts:13-15`

- [ ] **Step 1: Change proxy target from 8080 to 8088**

```typescript
// Before:
  server: {
    proxy: {
      '/admin': 'http://localhost:8080',
      '/api': 'http://localhost:8080',
      '/actuator': 'http://localhost:8080'
    }
  }

// After:
  server: {
    proxy: {
      '/admin': 'http://localhost:8088',
      '/api': 'http://localhost:8088',
      '/actuator': 'http://localhost:8088'
    }
  }
```

Note: dev mode Vite proxy bypasses Gateway and calls zhangyuan-api directly on 8088. Only production traffic goes through Gateway on 8080.

- [ ] **Step 2: Commit**

```bash
git add frontend/admin/vite.config.ts
git commit -m "fix(admin): update Vite proxy target to api:8088, gateway takes 8080"
```
