package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import java.util.Optional;

public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long>,
        JpaSpecificationExecutor<AdminPermission> {
    Optional<AdminPermission> findByCode(String code);
}
