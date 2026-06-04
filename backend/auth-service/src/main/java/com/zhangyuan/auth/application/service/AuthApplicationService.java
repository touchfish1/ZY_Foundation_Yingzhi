package com.zhangyuan.auth.application.service;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import com.zhangyuan.auth.adapter.out.persistence.AdminRole;
import com.zhangyuan.auth.adapter.out.persistence.AdminUser;
import com.zhangyuan.auth.common.security.AuthUser;
import com.zhangyuan.auth.common.security.AuthUserService;
import com.zhangyuan.auth.domain.model.LoginResult;
import com.zhangyuan.auth.domain.model.User;
import com.zhangyuan.auth.domain.model.UserStatus;
import com.zhangyuan.auth.domain.repository.UserRepository;
import com.zhangyuan.auth.domain.service.AuthDomainService;
import com.zhangyuan.auth.dto.AdminUserResponse;
import com.zhangyuan.auth.dto.LoginResponse;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import com.zhangyuan.auth.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * 认证应用服务，负责认证和用户管理的编排。
 */
@Service
public class AuthApplicationService {

    private static final Logger log = LoggerFactory.getLogger(AuthApplicationService.class);

    private final UserRepository userRepository;
    private final AuthDomainService authDomainService;
    private final AuthUserService authUserService;
    private final PasswordEncoder passwordEncoder;
    private final AdminUserRepository adminUserRepository;
    private final AdminRoleRepository adminRoleRepository;

    public AuthApplicationService(UserRepository userRepository, AuthDomainService authDomainService,
                                   AuthUserService authUserService, PasswordEncoder passwordEncoder,
                                   AdminUserRepository adminUserRepository,
                                   AdminRoleRepository adminRoleRepository) {
        this.userRepository = userRepository;
        this.authDomainService = authDomainService;
        this.authUserService = authUserService;
        this.passwordEncoder = passwordEncoder;
        this.adminUserRepository = adminUserRepository;
        this.adminRoleRepository = adminRoleRepository;
    }

    /**
     * 用户登录，验证用户名密码并创建 SaToken 会话。
     */
    public LoginResponse login(String username, String password) {
        log.info("User login: username={}", username);
        AuthUser user = authUserService.loadUserByUsername(username);
        if (!passwordEncoder.matches(password, user.getPassword())) {
            log.warn("Invalid password for user: {}", username);
            throw new BadCredentialsException("Invalid username or password");
        }
        if (!user.isEnabled()) {
            log.warn("Disabled user attempted login: {}", username);
            throw new BadCredentialsException("Admin user is disabled");
        }

        StpUtil.login(user.getId());
        SaSession session = StpUtil.getSession();
        session.set("username", user.getUsername());
        session.set("nickname", user.getNickname());
        session.set(SaSession.PERMISSION_LIST, user.getPermissions());
        session.set(SaSession.ROLE_LIST, authUserService.findRoleCodes(user.getUsername()));
        String tokenValue = StpUtil.getTokenValue();

        log.info("User logged in: username={}", username);
        return new LoginResponse(tokenValue, (int) StpUtil.getTokenTimeout(), toResponse(user), user.getPermissions());
    }

    /**
     * 获取当前登录用户的信息。
     */
    public LoginResponse currentUser() {
        long loginId = StpUtil.getLoginIdAsLong();
        AuthUser user = authUserService.loadUserById(loginId);
        log.info("Getting current user: username={}", user.getUsername());
        return new LoginResponse(null, 0, toResponse(user), user.getPermissions());
    }

    /**
     * 创建新用户（使用原始密码，内部加密）。
     */
    @Transactional
    public User createUser(String username, String rawPassword, String nickname, String email) {
        log.info("Creating user: username={}", username);
        authDomainService.validateNewUser(userRepository, username);
        User user = new User(username, passwordEncoder.encode(rawPassword), nickname, email);
        User saved = userRepository.save(user);
        log.info("User created: id={}, username={}", saved.getId(), saved.getUsername());
        return saved;
    }

    /**
     * 更新用户信息。
     */
    @Transactional
    public User updateUser(Long id, String nickname, String email, String status) {
        log.info("Updating user: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
        if (nickname != null || email != null) {
            user.updateProfile(nickname != null ? nickname : user.getNickname(),
                               email != null ? email : user.getEmail());
        }
        if (status != null) {
            user.setStatus("enabled".equals(status) ? UserStatus.ENABLED : UserStatus.DISABLED);
        }
        User saved = userRepository.save(user);
        log.info("User updated: {}", id);
        return saved;
    }

    /**
     * 删除用户。
     */
    @Transactional
    public void deleteUser(Long id) {
        log.info("Deleting user: {}", id);
        userRepository.deleteById(id);
        log.info("User deleted: {}", id);
    }

    /**
     * 根据 ID 查询用户。
     */
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * 根据用户名查询用户。
     */
    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    /**
     * 获取所有用户列表。
     */
    @Transactional(readOnly = true)
    public List<User> listAll() {
        return userRepository.findAll();
    }

    /**
     * 禁用指定用户。
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

    // User-Role assignment (needs JPA entity access for the join table)
    @Transactional(readOnly = true)
    public List<Long> getUserRoleIds(Long userId) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        return user.getRoles().stream()
                .map(AdminRole::getId)
                .toList();
    }

    @Transactional
    public void setUserRoles(Long userId, List<Long> roleIds) {
        AdminUser user = adminUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found: " + userId));
        List<AdminRole> roles = adminRoleRepository.findAllById(roleIds);
        user.getRoles().clear();
        user.getRoles().addAll(roles);
        adminUserRepository.save(user);
    }

    private AdminUserResponse toResponse(AuthUser user) {
        return new AdminUserResponse(
                user.getId(),
                user.getUsername(),
                user.getNickname(),
                null,
                authUserService.findRoleCodes(user.getUsername())
        );
    }
}
