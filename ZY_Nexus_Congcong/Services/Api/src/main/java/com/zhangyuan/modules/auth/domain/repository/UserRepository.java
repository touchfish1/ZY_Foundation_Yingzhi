package com.zhangyuan.modules.auth.domain.repository;

import com.zhangyuan.modules.auth.domain.model.User;
import java.util.List;
import java.util.Optional;

public interface UserRepository {

    Optional<User> findById(Long id);

    Optional<User> findByUsername(String username);

    List<User> findAll();

    User save(User user);

    void deleteById(Long id);

    boolean existsByUsername(String username);
}
