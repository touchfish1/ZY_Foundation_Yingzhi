package com.zhangyuan.modules.auth.domain.model;

import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class User {

    private Long id;
    private String username;
    private String passwordHash;
    private String nickname;
    private String email;
    private UserStatus status;
    private Set<String> roleCodes;
    private Instant createdAt;
    private Instant updatedAt;

    public User(String username, String passwordHash, String nickname, String email) {
        this.username = username;
        this.passwordHash = passwordHash;
        this.nickname = nickname;
        this.email = email;
        this.status = UserStatus.ENABLED;
        this.roleCodes = new HashSet<>();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public void updateProfile(String nickname, String email) {
        this.nickname = nickname;
        this.email = email;
        this.updatedAt = Instant.now();
    }

    public void disable() {
        this.status = UserStatus.DISABLED;
        this.updatedAt = Instant.now();
    }

    public void enable() {
        this.status = UserStatus.ENABLED;
        this.updatedAt = Instant.now();
    }

    public boolean authenticate(String rawPassword, java.util.function.BiFunction<String, String, Boolean> passwordMatcher) {
        if (status != UserStatus.ENABLED) {
            throw new IllegalStateException("User is disabled");
        }
        return passwordMatcher.apply(rawPassword, this.passwordHash);
    }

    public boolean hasRole(String roleCode) {
        return roleCodes.contains(roleCode);
    }

    public void addRole(String roleCode) {
        roleCodes.add(roleCode);
        this.updatedAt = Instant.now();
    }

    public Long getId() { return id; }
    public String getUsername() { return username; }
    public String getPasswordHash() { return passwordHash; }
    public String getNickname() { return nickname; }
    public String getEmail() { return email; }
    public UserStatus getStatus() { return status; }
    public Set<String> getRoleCodes() { return Collections.unmodifiableSet(roleCodes); }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
