package com.zhangyuan.auth.domain.repository;

import com.zhangyuan.auth.domain.model.Menu;
import java.util.List;
import java.util.Optional;

public interface MenuRepository {
    List<Menu> findAll();
    Optional<Menu> findById(Long id);
    Menu save(Menu menu);
    void deleteById(Long id);
    void deleteAllById(Iterable<Long> ids);
}
