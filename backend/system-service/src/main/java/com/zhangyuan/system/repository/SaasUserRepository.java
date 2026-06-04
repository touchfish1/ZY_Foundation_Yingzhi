package com.zhangyuan.system.repository;

import com.zhangyuan.system.adapter.out.persistence.SaasUserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SaasUserRepository extends JpaRepository<SaasUserEntity, Long> {
    Optional<SaasUserEntity> findByEmail(String email);
    Optional<SaasUserEntity> findByApiKey(String apiKey);
    boolean existsByEmail(String email);
}
