package com.zhangyuan.common.security;

import java.util.List;

public class AuthUser {

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

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }

    public boolean isEnabled() {
        return enabled;
    }
}
