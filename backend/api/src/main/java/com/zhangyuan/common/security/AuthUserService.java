package com.zhangyuan.common.security;

import com.zhangyuan.modules.auth.domain.AdminPermission;
import com.zhangyuan.modules.auth.domain.AdminRole;
import com.zhangyuan.modules.auth.domain.AdminUser;
import com.zhangyuan.modules.auth.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

/**
 * Spring Security 用户加载服务，根据用户名加载后台管理员用户信息。
 */
@Service
public class AuthUserService implements UserDetailsService {

    private static final Logger log = LoggerFactory.getLogger(AuthUserService.class);

    private final AdminUserRepository userRepository;

    public AuthUserService(AdminUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 根据用户名加载用户详情，包含权限列表。
     *
     * @param username 用户名
     * @return 用户详情（AuthUser 对象）
     * @throws UsernameNotFoundException 未找到用户时抛出
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        AdminUser user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    log.warn("Admin user not found: {}", username);
                    return new UsernameNotFoundException("Admin user not found");
                });
        // 提取用户所有角色的权限编码，去重并排序
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

    /**
     * 查询指定用户名对应的角色编码列表。
     *
     * @param username 用户名
     * @return 角色编码列表
     */
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
