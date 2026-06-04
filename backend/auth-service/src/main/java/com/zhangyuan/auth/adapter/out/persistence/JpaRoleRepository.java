package com.zhangyuan.auth.adapter.out.persistence;

import com.zhangyuan.auth.domain.model.Role;
import com.zhangyuan.auth.domain.repository.RoleRepository;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class JpaRoleRepository implements RoleRepository {

    private final AdminRoleRepository jpaRepository;

    public JpaRoleRepository(AdminRoleRepository jpaRepository) {
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Role> findById(Long id) {
        return jpaRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Role> findByCode(String code) {
        return jpaRepository.findByCode(code).map(this::toDomain);
    }

    @Override
    public List<Role> findAll() {
        return jpaRepository.findAll().stream().map(this::toDomain).toList();
    }

    @Override
    public Role save(Role role) {
        AdminRole entity;
        if (role.getId() != null) {
            entity = jpaRepository.findById(role.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Role not found: " + role.getId()));
            entity.setName(role.getName());
        } else {
            entity = new AdminRole(role.getCode(), role.getName());
        }
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    @Override
    public void deleteById(Long id) {
        jpaRepository.deleteById(id);
    }

    private Role toDomain(AdminRole entity) {
        Role role = new Role(entity.getCode(), entity.getName());
        role.setId(entity.getId());
        role.setCreatedAt(entity.getCreatedAt());
        return role;
    }
}
