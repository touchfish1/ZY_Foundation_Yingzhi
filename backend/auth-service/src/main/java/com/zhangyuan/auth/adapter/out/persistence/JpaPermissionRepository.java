package com.zhangyuan.auth.adapter.out.persistence;

import com.zhangyuan.auth.domain.model.Permission;
import com.zhangyuan.auth.domain.repository.PermissionRepository;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaPermissionRepository implements PermissionRepository {

    private final AdminPermissionRepository repo;

    public JpaPermissionRepository(AdminPermissionRepository repo) {
        this.repo = repo;
    }

    @Override
    public List<Permission> findAll() {
        return repo.findAll().stream().map(this::toDomain).collect(Collectors.toList());
    }

    @Override
    public Optional<Permission> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Permission> findByCode(String code) {
        return repo.findByCode(code).map(this::toDomain);
    }

    @Override
    public Permission save(Permission permission) {
        AdminPermission entity;
        if (permission.getId() != null) {
            entity = repo.findById(permission.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + permission.getId()));
            entity.setName(permission.getName());
            entity.setModule(permission.getModule());
        } else {
            entity = new AdminPermission(permission.getCode(), permission.getName(), permission.getModule());
        }
        AdminPermission saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    private Permission toDomain(AdminPermission entity) {
        return new Permission(entity.getId(), entity.getCode(), entity.getName(), entity.getModule());
    }
}
