package com.zhangyuan.auth.application.service;

import com.zhangyuan.auth.domain.model.LoginResult;
import com.zhangyuan.auth.domain.model.User;
import com.zhangyuan.auth.domain.repository.UserRepository;
import com.zhangyuan.auth.domain.service.AuthDomainService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 认证应用服务，负责用户创建和管理的编排。
 */
@Service
public class AuthApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);

    private final UserRepository userRepository;
    private final AuthDomainService authDomainService;

    public AuthApplicationService(UserRepository userRepository, AuthDomainService authDomainService) {
        this.userRepository = userRepository;
        this.authDomainService = authDomainService;
    }

    /**
     * 创建新用户。
     *
     * @param username     用户名
     * @param passwordHash 密码哈希值
     * @param nickname     昵称
     * @param email        邮箱
     * @return 创建后的用户
     */
    @Transactional
    public User createUser(String username, String passwordHash, String nickname, String email) {
        log.info("Creating user: username={}", username);
        authDomainService.validateNewUser(userRepository, username);
        User user = new User(username, passwordHash, nickname, email);
        User saved = userRepository.save(user);
        log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * 根据用户名查询用户。
     *
     * @param username 用户名
     * @return 用户 Optional
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取所有用户列表。
     *
     * @return 用户列表
     */
    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * 禁用指定用户。
     *
     * @param id 用户 ID
     */
    @Transactional
    public void disableUser(Long id) {
        log.info("Disabling user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("User not found: {}", id);
                    return new IllegalArgumentException("User not found: " + id);
                });
        user.disable();
        userRepository.save(user);
        log.info("User disabled: {}", id);
    }
}
