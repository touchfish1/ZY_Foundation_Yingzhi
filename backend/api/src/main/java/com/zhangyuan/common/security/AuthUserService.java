package com.zhangyuan.common.security;

import com.zhangyuan.modules.auth.domain.AdminPermission;
import com.zhangyuan.modules.auth.domain.AdminRole;
import com.zhangyuan.modules.auth.domain.AdminUser;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class AuthUserService {

    private static final Logger log = LoggerFactory.getLogger(AuthUserService.class);

    private final AdminUserRepository userRepository;

    public AuthUserService(AdminUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AuthUser loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        AdminUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Admin user not found: {}", username);
                    return new UsernameNotFoundException("Admin user not found");
                });
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(AdminPermission::getCode)
                .distinct()
                .sorted()
                .toList();

        return new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getNickname(),
                permissions,
                user.isEnabled()
        );
    }

    @Transactional(readOnly = true)
    public AuthUser loadUserById(Long id) {
        AdminUser user = userRepository.findById(id)
                .orElseThrow(() -> new UsernameNotFoundException("Admin user not found: " + id));
        List<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(AdminPermission::getCode)
                .distinct()
                .sorted()
                .toList();

        return new AuthUser(
                user.getId(),
                user.getUsername(),
                user.getPasswordHash(),
                user.getNickname(),
                permissions,
                user.isEnabled()
        );
    }

    @Transactional(readOnly = true)
    public List<String> findRoleCodes(String username) {
        return userRepository.findByUsername(username)
                .map(user -> user.getRoles().stream()
                        .map(AdminRole::getCode)
                        .sorted(Comparator.naturalOrder())
                        .toList())
                .orElse(List.of());
    }
}
