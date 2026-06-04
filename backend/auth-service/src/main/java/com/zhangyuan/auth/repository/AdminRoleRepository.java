package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminRole;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface AdminRoleRepository extends JpaRepository<AdminRole, Long> {
    Optional<AdminRole> findByCode(String code);

    @Query("SELECT r FROM AdminRole r WHERE " +
           "LOWER(r.code) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(r.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<AdminRole> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}
