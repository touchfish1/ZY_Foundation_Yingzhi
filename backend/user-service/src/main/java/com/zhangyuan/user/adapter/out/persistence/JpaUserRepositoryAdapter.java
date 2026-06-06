package com.zhangyuan.user.adapter.out.persistence;

import com.zhangyuan.user.domain.model.User;
import com.zhangyuan.user.domain.repository.UserRepository;
import com.zhangyuan.user.repository.SaasUserEntityRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.util.Optional;

@Repository
public class JpaUserRepositoryAdapter implements UserRepository {

    private final SaasUserEntityRepository repo;

    public JpaUserRepositoryAdapter(SaasUserEntityRepository repo) {
        this.repo = repo;
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return repo.findByEmail(email).map(this::toDomain);
    }

    @Override
    public Optional<User> findByApiKey(String apiKey) {
        return repo.findByApiKey(apiKey).map(this::toDomain);
    }

    @Override
    public Optional<User> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public boolean existsByEmail(String email) {
        return repo.existsByEmail(email);
    }

    @Override
    public User save(User user) {
        SaasUserEntity entity;
        if (user.getId() != null) {
            entity = toEntity(user);  // Create detached entity from user domain
            // Set ID explicitly to trigger merge/update in JPA
        } else {
            entity = toEntity(user);
        }
        return toDomain(repo.save(entity));
    }

    private User toDomain(SaasUserEntity e) {
        User u = new User(e.getEmail(), e.getPasswordHash(), e.getNickname());
        u.setId(e.getId());
        u.setVersion(e.getVersion());
        u.setBalance(e.getBalance());
        u.setStatus(e.getStatus());
        u.setAvatarUrl(e.getAvatarUrl());
        u.setApiKey(e.getApiKey());
        u.setConcurrency(e.getConcurrency());
        u.setRpmLimit(e.getRpmLimit());
        u.setRole(e.getRole());
        u.setTotalRecharged(e.getTotalRecharged());
        u.setQuotaUsed(e.getQuotaUsed());
        u.setQuotaLimit(e.getQuotaLimit());
        u.setNotes(e.getNotes());
        u.setLastLoginAt(e.getLastLoginAt());
        u.setCreatedAt(e.getCreatedAt() != null ? e.getCreatedAt().toInstant() : null);
        u.setUpdatedAt(e.getUpdatedAt() != null ? e.getUpdatedAt().toInstant() : null);
        return u;
    }

    private SaasUserEntity toEntity(User u) {
        SaasUserEntity e = new SaasUserEntity(u.getEmail(), u.getPasswordHash(), u.getNickname());
        e.setId(u.getId());
        Long version = u.getVersion();
        e.setVersion(version != null ? version : 0L);
        e.setBalance(u.getBalance());
        e.setStatus(u.getStatus());
        e.setAvatarUrl(u.getAvatarUrl());
        e.setApiKey(u.getApiKey());
        e.setConcurrency(u.getConcurrency());
        e.setRpmLimit(u.getRpmLimit());
        e.setRole(u.getRole());
        e.setTotalRecharged(u.getTotalRecharged());
        e.setQuotaUsed(u.getQuotaUsed());
        e.setQuotaLimit(u.getQuotaLimit());
        e.setNotes(u.getNotes());
        e.setLastLoginAt(u.getLastLoginAt());
        if (u.getCreatedAt() != null) {
            e.setCreatedAt(OffsetDateTime.from(u.getCreatedAt().atZone(ZoneId.systemDefault())));
        }
        if (u.getUpdatedAt() != null) {
            e.setUpdatedAt(OffsetDateTime.from(u.getUpdatedAt().atZone(ZoneId.systemDefault())));
        }
        return e;
    }
}
