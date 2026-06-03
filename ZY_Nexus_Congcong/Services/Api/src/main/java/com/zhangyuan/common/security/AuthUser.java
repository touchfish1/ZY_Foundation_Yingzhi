package com.zhangyuan.common.security;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class AuthUser implements UserDetails {

    private final Long id;

    private final String username;
    private final String password;
    private final String nickname;
    private final List<String> permissions;
    private final boolean enabled;

    public AuthUser(Long id, String username, String password, String nickname, List<String> permissions, boolean enabled) {
        this.id = id;
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.permissions = permissions;
        this.enabled = enabled;
    }

    public Long getId() {
        return id;
    }

    public String getNickname() {
        return nickname;
    }

    public List<String> getPermissions() {
        return permissions;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.stream().map(SimpleGrantedAuthority::new).toList();
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
