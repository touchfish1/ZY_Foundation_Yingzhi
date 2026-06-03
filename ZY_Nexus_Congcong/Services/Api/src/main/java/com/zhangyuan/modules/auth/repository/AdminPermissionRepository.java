package com.zhangyuan.modules.auth.repository;

import com.zhangyuan.modules.auth.domain.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {

    Optional<AdminPermission> findByCode(String code);
}
