package com.zhangyuan.modules.auth.adapter.out.persistence;

import com.zhangyuan.modules.auth.domain.model.Role;
import com.zhangyuan.modules.auth.domain.repository.RoleRepository;
import com.zhangyuan.modules.auth.repository.AdminRoleRepository;
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
        com.zhangyuan.modules.auth.domain.AdminRole entity = new com.zhangyuan.modules.auth.domain.AdminRole(
                role.getCode(), role.getName());
        entity = jpaRepository.save(entity);
        return toDomain(entity);
    }

    private Role toDomain(com.zhangyuan.modules.auth.domain.AdminRole entity) {
        return new Role(entity.getCode(), entity.getName());
    }
}
