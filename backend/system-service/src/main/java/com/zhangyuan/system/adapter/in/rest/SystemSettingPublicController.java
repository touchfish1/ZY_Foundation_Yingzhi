package com.zhangyuan.system.adapter.in.rest;

import com.zhangyuan.system.application.service.SystemSettingApplicationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/system/settings")
public class SystemSettingPublicController {

    private static final Logger log = LoggerFactory.getLogger(SystemSettingPublicController.class);

    private final SystemSettingApplicationService service;

    public SystemSettingPublicController(SystemSettingApplicationService service) {
        this.service = service;
    }

    @GetMapping
    public Map<String, String> getSettings() {
        log.info("Getting public system settings");
        return service.getPublicMap();
    }
}
