package com.zhangyuan.modules.cms.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "system-service")
public interface SystemServiceClient {

    @PutMapping("/api/ddd/settings/{key}")
    void upsertSetting(@PathVariable("key") String key, @RequestBody Map<String, String> body);
}
