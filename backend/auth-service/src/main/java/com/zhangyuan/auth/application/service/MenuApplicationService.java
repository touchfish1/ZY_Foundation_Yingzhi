package com.zhangyuan.auth.application.service;

import com.zhangyuan.auth.domain.model.Menu;
import com.zhangyuan.auth.domain.repository.MenuRepository;
import com.zhangyuan.auth.dto.MenuRequest;
import com.zhangyuan.auth.dto.MenuResponse;
import com.zhangyuan.auth.dto.SortItem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class MenuApplicationService {

    private final MenuRepository menuRepository;

    public MenuApplicationService(MenuRepository menuRepository) {
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getMenuTree() {
        List<Menu> allMenus = menuRepository.findAll();
        return buildTree(allMenus, null);
    }

    @Transactional(readOnly = true)
    public List<MenuResponse> getCurrentUserMenus(List<String> userPermissions) {
        List<Menu> allMenus = menuRepository.findAll();
        Set<Long> accessibleIds = findAccessibleIds(allMenus, userPermissions);
        return buildFilteredTree(allMenus, null, accessibleIds);
    }

    @Transactional
    public MenuResponse createMenu(MenuRequest request) {
        Menu menu = new Menu(request.name(), request.menuType(),
                request.sortOrder() != null ? request.sortOrder() : 0);
        menu.setParentId(request.parentId());
        menu.setPath(request.path());
        menu.setIcon(request.icon());
        menu.setStatus(request.status() != null ? request.status() : "enabled");
        if (request.permissionCodes() != null) {
            menu.setPermissionCodes(new ArrayList<>(request.permissionCodes()));
        }
        Menu saved = menuRepository.save(menu);
        return toResponse(saved);
    }

    @Transactional
    public MenuResponse updateMenu(Long id, MenuRequest request) {
        Menu menu = menuRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + id));
        menu.setName(request.name());
        menu.setParentId(request.parentId());
        menu.setPath(request.path());
        menu.setIcon(request.icon());
        menu.setMenuType(request.menuType());
        menu.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        menu.setStatus(request.status() != null ? request.status() : "enabled");
        if (request.permissionCodes() != null) {
            menu.setPermissionCodes(new ArrayList<>(request.permissionCodes()));
        } else {
            menu.setPermissionCodes(new ArrayList<>());
        }
        Menu saved = menuRepository.save(menu);
        return toResponse(saved);
    }

    @Transactional
    public void deleteMenu(Long id) {
        List<Menu> all = menuRepository.findAll();
        Set<Long> toDelete = new HashSet<>();
        collectDescendantIds(id, all, toDelete);
        toDelete.add(id);
        menuRepository.deleteAllById(toDelete);
    }

    @Transactional
    public void updateSort(List<SortItem> sortList) {
        for (SortItem item : sortList) {
            menuRepository.findById(item.id()).ifPresent(menu -> {
                menu.setSortOrder(item.sortOrder());
                menuRepository.save(menu);
            });
        }
    }

    // --- tree building helpers ---

    private List<MenuResponse> buildTree(List<Menu> allMenus, Long parentId) {
        return allMenus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 0))
                .map(m -> new MenuResponse(
                        m.getId(), m.getParentId(), m.getName(), m.getPath(), m.getIcon(),
                        m.getMenuType(), m.getSortOrder(), m.getStatus(),
                        m.getPermissionCodes() != null ? m.getPermissionCodes() : List.of(),
                        buildTree(allMenus, m.getId()),
                        OffsetDateTime.now(), OffsetDateTime.now()
                ))
                .toList();
    }

    private List<MenuResponse> buildFilteredTree(List<Menu> allMenus, Long parentId, Set<Long> accessibleIds) {
        return allMenus.stream()
                .filter(m -> Objects.equals(m.getParentId(), parentId))
                .filter(m -> accessibleIds.contains(m.getId()))
                .sorted(Comparator.comparingInt(m -> m.getSortOrder() != null ? m.getSortOrder() : 0))
                .map(m -> new MenuResponse(
                        m.getId(), m.getParentId(), m.getName(), m.getPath(), m.getIcon(),
                        m.getMenuType(), m.getSortOrder(), m.getStatus(),
                        m.getPermissionCodes() != null ? m.getPermissionCodes() : List.of(),
                        buildFilteredTree(allMenus, m.getId(), accessibleIds),
                        OffsetDateTime.now(), OffsetDateTime.now()
                ))
                .toList();
    }

    private Set<Long> findAccessibleIds(List<Menu> allMenus, List<String> userPermissions) {
        Set<Long> result = new HashSet<>();
        Map<Long, List<Long>> childrenByParent = new HashMap<>();
        for (Menu m : allMenus) {
            if (m.getParentId() != null) {
                childrenByParent.computeIfAbsent(m.getParentId(), k -> new ArrayList<>()).add(m.getId());
            }
        }
        // First pass: page/button items
        for (Menu m : allMenus) {
            if ("group".equals(m.getMenuType())) continue;
            if ("disabled".equals(m.getStatus())) continue;
            List<String> codes = m.getPermissionCodes();
            if (codes == null || codes.isEmpty() || userPermissions.stream().anyMatch(codes::contains)) {
                result.add(m.getId());
            }
        }
        // Second pass: propagate up to parents
        boolean changed = true;
        while (changed) {
            changed = false;
            for (Menu m : allMenus) {
                if (result.contains(m.getId())) continue;
                List<Long> kids = childrenByParent.get(m.getId());
                if (kids != null && kids.stream().anyMatch(result::contains)) {
                    result.add(m.getId());
                    changed = true;
                }
            }
        }
        return result;
    }

    private void collectDescendantIds(Long parentId, List<Menu> allMenus, Set<Long> ids) {
        allMenus.stream()
                .filter(m -> parentId.equals(m.getParentId()))
                .forEach(m -> {
                    ids.add(m.getId());
                    collectDescendantIds(m.getId(), allMenus, ids);
                });
    }

    private MenuResponse toResponse(Menu menu) {
        return new MenuResponse(
                menu.getId(), menu.getParentId(), menu.getName(), menu.getPath(), menu.getIcon(),
                menu.getMenuType(), menu.getSortOrder(), menu.getStatus(),
                menu.getPermissionCodes() != null ? menu.getPermissionCodes() : List.of(),
                List.of(), OffsetDateTime.now(), OffsetDateTime.now()
        );
    }
}
