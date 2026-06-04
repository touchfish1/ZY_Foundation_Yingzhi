package com.zhangyuan.auth.adapter.out.persistence;

import com.zhangyuan.auth.domain.model.User;
import com.zhangyuan.auth.domain.model.UserStatus;
import com.zhangyuan.auth.domain.repository.UserRepository;
import com.zhangyuan.auth.repository.AdminUserRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaUserRepository implements UserRepository {

    private final AdminUserRepository jpaRepository;

    public JpaUserRepository(AdminUserRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<User> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jpaRepository.findByUsername(username).map(this::toDomain);
    }

    @Override
    public List<User> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public User save(User user) {
        AdminUser entity;
        if (user.getId() != null) {
            entity = jpaRepository.findById(user.getId())
                    .orElseThrow(() -> new IllegalArgumentException("User not found: " + user.getId()));
            entity.setNickname(user.getNickname());
            entity.setEmail(user.getEmail());
            entity.setStatus(user.getStatus() == UserStatus.ENABLED ? "enabled" : "disabled");
        } else {
            entity = new AdminUser(
                    user.getUsername(), user.getPasswordHash(), user.getNickname(), user.getEmail());
        }
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByUsername(String username) {
        return jpaRepository.findByUsername(username).isPresent();
    }

    private User toDomain(AdminUser entity) {
        User user = new User(entity.getUsername(), entity.getPasswordHash(), entity.getNickname(), entity.getEmail());
        user.setId(entity.getId());
        user.setCreatedAt(entity.getCreatedAt());
        user.setUpdatedAt(entity.getCreatedAt());
        if (!entity.isEnabled()) {
            user.disable();
        }
        return user;
    }
}
