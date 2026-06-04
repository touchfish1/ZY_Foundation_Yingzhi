package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminPermissionRepository extends JpaRepository<AdminPermission, Long> {

    Optional<AdminPermission> findByCode(String code);
}
