package com.zhangyuan.auth.adapter.out.persistence;

import com.zhangyuan.auth.domain.model.Menu;
import com.zhangyuan.auth.domain.repository.MenuRepository;
import com.zhangyuan.auth.repository.AdminMenuRepository;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class JpaMenuRepository implements MenuRepository {

    private final AdminMenuRepository repo;
    private final AdminPermissionRepository permissionRepo;

    public JpaMenuRepository(AdminMenuRepository repo, AdminPermissionRepository permissionRepo) {
        this.repo = repo;
        this.permissionRepo = permissionRepo;
    }

    @Override
    public List<Menu> findAll() {
        return repo.findAllByOrderBySortOrderAsc().stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Menu> findById(Long id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public Menu save(Menu menu) {
        AdminMenu entity = toEntity(menu);
        AdminMenu saved = repo.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    @Override
    public void deleteAllById(Iterable<Long> ids) {
        repo.deleteAllById(ids);
    }

    private Menu toDomain(AdminMenu entity) {
        Menu menu = new Menu(entity.getName(), entity.getMenuType(), entity.getSortOrder());
        menu.setId(entity.getId());
        menu.setParentId(entity.getParentId());
        menu.setPath(entity.getPath());
        menu.setIcon(entity.getIcon());
        menu.setStatus(entity.getStatus());
        menu.setPermissionCodes(
                entity.getPermissions().stream()
                        .map(AdminPermission::getCode)
                        .toList()
        );
        return menu;
    }

    private AdminMenu toEntity(Menu domain) {
        AdminMenu entity;
        if (domain.getId() != null) {
            entity = repo.findById(domain.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + domain.getId()));
            entity.setName(domain.getName());
            entity.setParentId(domain.getParentId());
            entity.setPath(domain.getPath());
            entity.setIcon(domain.getIcon());
            entity.setMenuType(domain.getMenuType());
            entity.setSortOrder(domain.getSortOrder());
            entity.setStatus(domain.getStatus());
        } else {
            entity = new AdminMenu(domain.getName(), domain.getMenuType(), domain.getSortOrder());
            entity.setParentId(domain.getParentId());
            entity.setPath(domain.getPath());
            entity.setIcon(domain.getIcon());
            entity.setStatus(domain.getStatus() != null ? domain.getStatus() : "enabled");
        }
        // Map permission codes
        entity.getPermissions().clear();
        if (domain.getPermissionCodes() != null && !domain.getPermissionCodes().isEmpty()) {
            entity.getPermissions().addAll(
                    domain.getPermissionCodes().stream()
                            .map(code -> permissionRepo.findByCode(code).orElse(null))
                            .filter(Objects::nonNull)
                            .collect(Collectors.toSet())
            );
        }
        return entity;
    }
}
