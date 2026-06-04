package com.zhangyuan.auth.domain.repository;

import com.zhangyuan.auth.domain.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findById(Long id);

    Optional<Role> findByCode(String code);

    List<Role> findAll();

    Role save(Role role);

    void deleteById(Long id);
}
