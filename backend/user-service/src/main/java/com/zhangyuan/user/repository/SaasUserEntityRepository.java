package com.zhangyuan.user.repository;

import com.zhangyuan.user.adapter.out.persistence.SaasUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaasUserEntityRepository extends JpaRepository<SaasUserEntity, Long> {
    Optional<SaasUserEntity> findByEmail(String email);
    Optional<SaasUserEntity> findByApiKey(String apiKey);
    boolean existsByEmail(String email);
}
