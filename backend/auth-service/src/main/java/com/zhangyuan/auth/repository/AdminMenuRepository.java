package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminMenu;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AdminMenuRepository extends JpaRepository<AdminMenu, Long> {

    List<AdminMenu> findAllByOrderBySortOrderAsc();

    List<AdminMenu> findByParentIdOrderBySortOrderAsc(Long parentId);
}
