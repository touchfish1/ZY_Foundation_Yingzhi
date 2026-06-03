package com.zhangyuan.modules.auth.domain.service;

import com.zhangyuan.modules.auth.domain.model.User;
import com.zhangyuan.modules.auth.domain.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * 认证领域服务，包含用户认证和用户验证的核心业务规则。
 */
@Service
public class AuthDomainService {

    private static final Logger log = LoggerFactory.getLogger(AuthDomainService.class);

    /**
     * 验证用户密码是否匹配。
     *
     * @param user            用户领域对象
     * @param rawPassword     明文密码
     * @param passwordMatcher 密码匹配函数
     * @return 认证通过的用户对象
     * @throws IllegalArgumentException 认证失败时抛出
     */
    public User authenticate(User user, String rawPassword,
                             java.util.function.BiFunction<String, String, Boolean> passwordMatcher) {
        if (!user.authenticate(rawPassword, passwordMatcher)) {
            log.warn("Authentication failed for user: {}", user.getUsername());
            throw new IllegalArgumentException("Invalid username or password");
        }
        return user;
    }

    /**
     * 验证新用户用户名是否已存在。
     *
     * @param userRepository 用户仓库
     * @param username       待检查的用户名
     * @throws IllegalArgumentException 用户名已存在时抛出
     */
    public void validateNewUser(UserRepository userRepository, String username) {
        if (userRepository.existsByUsername(username)) {
            log.warn("Username already exists: {}", username);
            throw new IllegalArgumentException("Username already exists: " + username);
        }
    }
}
