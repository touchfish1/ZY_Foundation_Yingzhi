package com.zhangyuan.system.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@FeignClient(name = "user-service")
public interface UserServiceClient {

    @GetMapping("/api/users/{id}")
    Map<String, Object> getUser(@PathVariable("id") Long id);

    @GetMapping("/api/users")
    Object listUsers(@RequestParam(defaultValue = "1") int page,
                     @RequestParam(defaultValue = "20") int pageSize);

    @PutMapping("/api/users/{id}/status")
    Object updateUserStatus(@PathVariable("id") Long id, @RequestParam String status);
}
