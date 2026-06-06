package com.zhangyuan.system.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.system.client.UserServiceClient;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/users")
@SaCheckPermission("system:user:list")
public class AdminUserController {
    private final UserServiceClient userServiceClient;
    public AdminUserController(UserServiceClient userServiceClient) { this.userServiceClient = userServiceClient; }

    @GetMapping
    public Object listUsers(@RequestParam(defaultValue = "1") int page,
                            @RequestParam(defaultValue = "20") int pageSize) {
        return userServiceClient.listUsers(page, pageSize);
    }

    @GetMapping("/{id}")
    public Object getUser(@PathVariable Long id) {
        return userServiceClient.getUser(id);
    }

    @PutMapping("/{id}/status")
    public Object updateStatus(@PathVariable Long id, @RequestParam String status) {
        return userServiceClient.updateUserStatus(id, status);
    }
}
