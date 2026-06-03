package com.zhangyuan.modules.auth.domain.repository;

import com.zhangyuan.modules.auth.domain.model.Role;
import java.util.List;
import java.util.Optional;

public interface RoleRepository {

    Optional<Role> findById(Long id);

    Optional<Role> findByCode(String code);

    List<Role> findAll();

    Role save(Role role);
}
