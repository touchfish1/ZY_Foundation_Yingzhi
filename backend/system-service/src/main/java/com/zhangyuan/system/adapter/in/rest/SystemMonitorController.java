package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.adapter.out.persistence.SystemSettingJpaRepository;
import com.zhangyuan.system.common.ApiResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.lang.management.ManagementFactory;
import java.lang.management.RuntimeMXBean;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;

@RestController
@RequestMapping("/admin/system/monitor")
public class SystemMonitorController {

    private static final Logger log = LoggerFactory.getLogger(SystemMonitorController.class);

    private final SystemSettingJpaRepository settingJpaRepository;

    public SystemMonitorController(SystemSettingJpaRepository settingJpaRepository) {
        this.settingJpaRepository = settingJpaRepository;
    }

    @GetMapping("/stats")
    public ApiResponse<Map<String, Object>> stats() {
        log.info("Fetching system monitor stats");

        Runtime runtime = Runtime.getRuntime();
        RuntimeMXBean mxBean = ManagementFactory.getRuntimeMXBean();

        // JVM memory
        long maxMemory = runtime.maxMemory();
        long totalMemory = runtime.totalMemory();
        long freeMemory = runtime.freeMemory();
        long usedMemory = totalMemory - freeMemory;
        double usagePercent = maxMemory > 0
                ? Math.round(usedMemory * 10000.0 / maxMemory) / 100.0
                : 0.0;

        Map<String, Object> jvm = new LinkedHashMap<>();
        jvm.put("maxMemory", maxMemory);
        jvm.put("totalMemory", totalMemory);
        jvm.put("freeMemory", freeMemory);
        jvm.put("usedMemory", usedMemory);
        jvm.put("usagePercent", usagePercent);

        // System info
        Map<String, Object> system = new LinkedHashMap<>();
        system.put("osName", System.getProperty("os.name"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("javaVersion", System.getProperty("java.version"));
        system.put("availableProcessors", runtime.availableProcessors());

        // Uptime — human-readable
        long uptimeMs = mxBean.getUptime();
        long hours = uptimeMs / (1000 * 60 * 60);
        long minutes = (uptimeMs % (1000 * 60 * 60)) / (1000 * 60);
        String uptime = hours + "h " + minutes + "m";

        // Settings count
        long totalCount = settingJpaRepository.count();

        Map<String, Object> settings = new LinkedHashMap<>();
        settings.put("totalCount", totalCount);

        Map<String, Object> data = new LinkedHashMap<>();
        data.put("jvm", jvm);
        data.put("system", system);
        data.put("uptime", uptime);
        data.put("startTime", Instant.ofEpochMilli(mxBean.getStartTime()).toString());
        data.put("settings", settings);

        return ApiResponse.ok(data);
    }
}
