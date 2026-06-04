package com.zhangyuan.auth.domain.repository;

import com.zhangyuan.auth.domain.model.Permission;

import java.util.List;
import java.util.Optional;

public interface PermissionRepository {

    List<Permission> findAll();

    Optional<Permission> findById(Long id);

    Optional<Permission> findByCode(String code);

    Permission save(Permission permission);

    void deleteById(Long id);
}
