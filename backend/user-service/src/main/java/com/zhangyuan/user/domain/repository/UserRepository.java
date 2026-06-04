package com.zhangyuan.user.domain.repository;

import com.zhangyuan.user.domain.model.User;

import java.util.Optional;

public interface UserRepository {
    Optional<User> findByEmail(String email);
    Optional<User> findByApiKey(String apiKey);
    Optional<User> findById(Long id);
    boolean existsByEmail(String email);
    User save(User user);
}
