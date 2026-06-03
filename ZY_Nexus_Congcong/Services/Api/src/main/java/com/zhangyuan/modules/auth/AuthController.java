package com.zhangyuan.modules.auth;

import com.zhangyuan.common.response.ApiResponse;
import com.zhangyuan.modules.auth.dto.LoginRequest;
import com.zhangyuan.modules.auth.dto.LoginResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/admin/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ApiResponse<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ApiResponse.ok(authService.login(request.username(), request.password()));
    }

    @GetMapping("/me")
    public ApiResponse<LoginResponse> me() {
        return ApiResponse.ok(authService.currentUser());
    }
}
