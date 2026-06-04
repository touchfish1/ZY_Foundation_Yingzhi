package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminRole;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {

    Optional<AdminRole> findByCode(String code);
}
