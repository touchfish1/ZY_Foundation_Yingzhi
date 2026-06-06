package com.zhangyuan.system.adapter.in.rest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.Map;

@RestController
@RequestMapping("/api/system")
public class SystemHealthController {

    private static final Logger log = LoggerFactory.getLogger(SystemHealthController.class);

    @GetMapping("/ping")
    public Map<String, Object> ping() {
        log.info("Health check ping");
        return Map.of(
                "service", "system-service",
                "time", Instant.now().toString()
        );
    }
}
