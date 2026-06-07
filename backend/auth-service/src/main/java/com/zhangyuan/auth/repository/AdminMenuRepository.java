package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminMenuRepository extends JpaRepository<AdminMenu, Long> {

    List<AdminMenu> findAllByOrderBySortOrderAsc();

    List<AdminMenu> findByParentIdOrderBySortOrderAsc(Long parentId);

    Optional<AdminMenu> findByPath(String path);

    Optional<AdminMenu> findByNameAndParentId(String name, Long parentId);
}
