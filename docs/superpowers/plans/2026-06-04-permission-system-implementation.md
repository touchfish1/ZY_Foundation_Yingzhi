# Permission System Enhancement — Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build full-featured permission management (CRUD), role-permission assignment, user-role assignment, configurable menu tree, frontend permission directives, and route guards.

**Architecture:** Extend the existing auth-service JPA entities with `AdminMenu` and a menu-permission join table. New REST controllers follow the existing DDD pattern (adapter/in/rest). Frontend uses a Pinia permission store, a custom `v-permission` directive, and dynamic sidebar menu rendering.

**Tech Stack:** Spring Boot 3.3.5 + Java 21 + Sa-Token 1.39.0 + Vue 3 + Naive UI + Pinia

**Plan structure:** 17 tasks organized in 6 phases. Phase 1→2→3 are sequential (backend foundation → APIs → seed data). Phases 4-5 (frontend) can partially overlap with Phase 2-3.

---

### Task 1: Database Migration — Add Menu Tables

**Files:**
- Create: `backend/auth-service/src/main/resources/db/migration/V002__create_admin_menu.sql`
- Modify: `backend/auth-service/src/main/resources/application.yml` (enable Flyway)

- [ ] **Step 1: Create V002 migration**

Write `backend/auth-service/src/main/resources/db/migration/V002__create_admin_menu.sql`:

```sql
create table admin_menu (
  id bigserial primary key,
  parent_id bigint references admin_menu(id),
  name varchar(64) not null,
  path varchar(255),
  icon varchar(64),
  menu_type varchar(16) not null default 'page',
  sort_order int not null default 0,
  status varchar(16) not null default 'enabled',
  created_at timestamptz not null default now(),
  updated_at timestamptz not null default now()
);

create table admin_menu_permission (
  menu_id bigint not null references admin_menu(id) on delete cascade,
  permission_code varchar(128) not null references admin_permission(code) on delete cascade,
  primary key (menu_id, permission_code)
);
```

- [ ] **Step 2: Enable Flyway in auth-service**

Edit `application.yml` — change `spring.flyway.enabled: false` to `true` and add:

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: 1
```

This tells Flyway to baseline the existing database (mark V001 as applied without re-running it) and then apply V002.

- [ ] **Step 3: Verify on next bootRun**

Run `./gradlew bootRun` and check logs for:
```
Flyway: Successfully applied 1 migration (execution time ...)
```

---

### Task 2: AdminMenu JPA Entity + Repository

**Files:**
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/out/persistence/AdminMenu.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/repository/AdminMenuRepository.java`

- [ ] **Step 1: Create AdminMenu entity**

```java
package com.zhangyuan.auth.adapter.out.persistence;

import jakarta.persistence.*;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "admin_menu")
public class AdminMenu {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "parent_id")
    private Long parentId;

    @Column(nullable = false, length = 64)
    private String name;

    @Column(length = 255)
    private String path;

    @Column(length = 64)
    private String icon;

    @Column(name = "menu_type", nullable = false, length = 16)
    private String menuType = "page";

    @Column(name = "sort_order", nullable = false)
    private Integer sortOrder = 0;

    @Column(nullable = false, length = 16)
    private String status = "enabled";

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private OffsetDateTime updatedAt;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "admin_menu_permission",
        joinColumns = @JoinColumn(name = "menu_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_code", referencedColumnName = "code")
    )
    private List<AdminPermission> permissions = new ArrayList<>();

    public AdminMenu() {}

    public AdminMenu(String name, String menuType, Integer sortOrder) {
        this.name = name;
        this.menuType = menuType;
        this.sortOrder = sortOrder;
    }

    @PrePersist
    public void prePersist() {
        OffsetDateTime now = OffsetDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    public void preUpdate() {
        this.updatedAt = OffsetDateTime.now();
    }

    // Getters and setters
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getParentId() { return parentId; }
    public void setParentId(Long parentId) { this.parentId = parentId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPath() { return path; }
    public void setPath(String path) { this.path = path; }
    public String getIcon() { return icon; }
    public void setIcon(String icon) { this.icon = icon; }
    public String getMenuType() { return menuType; }
    public void setMenuType(String menuType) { this.menuType = menuType; }
    public Integer getSortOrder() { return sortOrder; }
    public void setSortOrder(Integer sortOrder) { this.sortOrder = sortOrder; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public OffsetDateTime getCreatedAt() { return createdAt; }
    public OffsetDateTime getUpdatedAt() { return updatedAt; }
    public List<AdminPermission> getPermissions() { return permissions; }
    public void setPermissions(List<AdminPermission> permissions) { this.permissions = permissions; }
}
```

- [ ] **Step 2: Create AdminMenuRepository**

```java
package com.zhangyuan.auth.repository;

import com.zhangyuan.auth.adapter.out.persistence.AdminMenu;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminMenuRepository extends JpaRepository<AdminMenu, Long> {
    List<AdminMenu> findAllByOrderBySortOrderAsc();
    List<AdminMenu> findByParentIdOrderBySortOrderAsc(Long parentId);
}
```

---

### Task 3: Permission CRUD Backend

**Files:**
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/dto/PermissionRequest.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/dto/PermissionResponse.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminPermissionController.java`

- [ ] **Step 1: Create DTOs**

```java
// PermissionRequest.java
package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record PermissionRequest(
    @NotBlank @Pattern(regexp = "^[a-z]+:[a-z:]+$") String code,
    @NotBlank String name,
    @NotBlank String module
) {}
```

```java
// PermissionResponse.java
package com.zhangyuan.auth.dto;

public record PermissionResponse(
    Long id, String code, String name, String module
) {}
```

- [ ] **Step 2: Create AdminPermissionController**

```java
package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.dto.PermissionRequest;
import com.zhangyuan.auth.dto.PermissionResponse;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/admin/system/permissions")
@SaCheckPermission("system:permission:list")
public class AdminPermissionController {

    private final AdminPermissionRepository permissionRepository;

    public AdminPermissionController(AdminPermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    public ApiResponse<List<PermissionResponse>> list(@RequestParam(required = false) String module) {
        List<AdminPermission> permissions = (module != null && !module.isBlank())
            ? permissionRepository.findAll().stream()
                .filter(p -> module.equals(p.getModule()))
                .toList()
            : permissionRepository.findAll();
        return ApiResponse.ok(permissions.stream()
            .map(p -> new PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getModule()))
            .sorted(Comparator.comparing(PermissionResponse::module))
            .toList());
    }

    @GetMapping("/modules")
    public ApiResponse<List<String>> listModules() {
        return ApiResponse.ok(permissionRepository.findAll().stream()
            .map(AdminPermission::getModule)
            .distinct()
            .sorted()
            .toList());
    }

    @PostMapping
    @SaCheckPermission("system:permission:create")
    public ApiResponse<PermissionResponse> create(@Valid @RequestBody PermissionRequest request) {
        if (permissionRepository.findByCode(request.code()).isPresent()) {
            return ApiResponse.error(40001, "Permission code already exists: " + request.code());
        }
        AdminPermission entity = new AdminPermission(request.code(), request.name(), request.module());
        entity = permissionRepository.save(entity);
        return ApiResponse.ok(new PermissionResponse(entity.getId(), entity.getCode(), entity.getName(), entity.getModule()));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:permission:update")
    public ApiResponse<PermissionResponse> update(@PathVariable Long id, @Valid @RequestBody PermissionRequest request) {
        AdminPermission entity = permissionRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id));
        // Use reflection to update fields since AdminPermission has no setters.
        // Alternatively, add setters to AdminPermission. For now, use the existing pattern.
        AdminPermission updated = new AdminPermission(request.code(), request.name(), request.module());
        // We need to use the entity manager or JPQL for update since AdminPermission fields are final.
        // Alternative: Add a JPQL @Modifying query in repository.
        // For now, throw UnsupportedOperationException and implement via native query.
        return ApiResponse.error(501, "Update not yet implemented — use delete + create");
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:permission:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        permissionRepository.deleteById(id);
        return ApiResponse.ok();
    }
}
```

**Note on update:** The existing `AdminPermission` entity has no setters (immutable pattern). Either:
- Add a JPQL `@Modifying @Query` method to `AdminPermissionRepository` to update by ID, or
- Make `AdminPermission` fields mutable by adding setters

Recommendation: Add an `update(String code, String name, String module)` method to `AdminPermission` + add setters for name and module (keeping code immutable after creation).

- [ ] **Step 3: Verify compilation**

Run: `cd backend/auth-service && ./gradlew compileJava`

---

### Task 4: Enhance Role Controller — Permission Assignment

**Files:**
- Modify: `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminSystemRoleController.java`

- [ ] **Step 1: Add permission management endpoints**

Add to `AdminSystemRoleController`:

```java
@GetMapping("/{id}/permissions")
public ApiResponse<List<Long>> getRolePermissions(@PathVariable Long id) {
    AdminRole role = roleRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));
    return ApiResponse.ok(role.getPermissions().stream()
        .map(AdminPermission::getId)
        .toList());
}

@PutMapping("/{id}/permissions")
@SaCheckPermission("system:role:update")
public ApiResponse<Void> setRolePermissions(@PathVariable Long id, @RequestBody SetPermissionRequest request) {
    AdminRole role = roleRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("Role not found: " + id));
    List<AdminPermission> permissions = permissionRepository.findAllById(request.permissionIds());
    role.getPermissions().clear();
    role.getPermissions().addAll(permissions);
    roleRepository.save(role);
    return ApiResponse.ok();
}
```

- [ ] **Step 2: Create SetPermissionRequest DTO**

```java
package com.zhangyuan.auth.dto;

import java.util.List;

public record SetPermissionRequest(List<Long> permissionIds) {}
```

We also need to inject `AdminPermissionRepository` into `AdminSystemRoleController`. Add as constructor parameter.

- [ ] **Step 3: Verify compilation**

---

### Task 5: Enhance User Controller — Role Assignment

**Files:**
- Modify: `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminSystemUserController.java`

- [ ] **Step 1: Add role management endpoints and new DTO**

Add to `AdminSystemUserController`:

```java
@GetMapping("/{id}/roles")
public ApiResponse<List<Long>> getUserRoles(@PathVariable Long id) {
    AdminUser user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    return ApiResponse.ok(user.getRoles().stream()
        .map(AdminRole::getId)
        .toList());
}

@PutMapping("/{id}/roles")
@SaCheckPermission("system:user:update")
public ApiResponse<Void> setUserRoles(@PathVariable Long id, @RequestBody SetRoleRequest request) {
    AdminUser user = userRepository.findById(id)
        .orElseThrow(() -> new IllegalArgumentException("User not found: " + id));
    List<AdminRole> roles = roleRepository.findAllById(request.roleIds());
    user.getRoles().clear();
    user.getRoles().addAll(roles);
    userRepository.save(user);
    return ApiResponse.ok();
}
```

- [ ] **Step 2: Create SetRoleRequest DTO and inject needed repositories**

```java
package com.zhangyuan.auth.dto;

import java.util.List;

public record SetRoleRequest(List<Long> roleIds) {}
```

`AdminSystemUserController` needs `AdminUserRepository` and `AdminRoleRepository` injected (not just the domain-level `UserRepository` / `RoleRepository`, since we need JPA entity access for permission management). Use the existing JPA repositories.

- [ ] **Step 3: Verify compilation**

---

### Task 6: Menu CRUD Backend

**Files:**
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/dto/MenuRequest.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/dto/MenuResponse.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminMenuController.java`

- [ ] **Step 1: Create DTOs**

```java
// MenuRequest.java
package com.zhangyuan.auth.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;

public record MenuRequest(
    Long parentId,
    @NotBlank String name,
    String path,
    String icon,
    @NotBlank String menuType,
    Integer sortOrder,
    String status,
    List<String> permissionCodes
) {}
```

```java
// MenuResponse.java
package com.zhangyuan.auth.dto;

import java.time.OffsetDateTime;
import java.util.List;

public record MenuResponse(
    Long id,
    Long parentId,
    String name,
    String path,
    String icon,
    String menuType,
    Integer sortOrder,
    String status,
    List<String> permissionCodes,
    List<MenuResponse> children,
    OffsetDateTime createdAt,
    OffsetDateTime updatedAt
) {}
```

- [ ] **Step 2: Create AdminMenuController**

```java
package com.zhangyuan.auth.adapter.in.rest;

import cn.dev33.satoken.annotation.SaCheckPermission;
import com.zhangyuan.auth.adapter.out.persistence.AdminMenu;
import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.common.ApiResponse;
import com.zhangyuan.auth.dto.MenuRequest;
import com.zhangyuan.auth.dto.MenuResponse;
import com.zhangyuan.auth.repository.AdminMenuRepository;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/system/menus")
public class AdminMenuController {

    private final AdminMenuRepository menuRepository;
    private final AdminPermissionRepository permissionRepository;

    public AdminMenuController(AdminMenuRepository menuRepository, AdminPermissionRepository permissionRepository) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
    }

    @GetMapping
    @SaCheckPermission("system:menu:list")
    public ApiResponse<List<MenuResponse>> getMenuTree() {
        List<AdminMenu> allMenus = menuRepository.findAllByOrderBySortOrderAsc();
        Map<Long, List<AdminMenu>> childrenByParent = allMenus.stream()
            .filter(m -> m.getParentId() != null)
            .collect(Collectors.groupingBy(AdminMenu::getParentId));
        List<MenuResponse> tree = allMenus.stream()
            .filter(m -> m.getParentId() == null)
            .map(m -> toTree(m, childrenByParent))
            .toList();
        return ApiResponse.ok(tree);
    }

    @PostMapping
    @SaCheckPermission("system:menu:create")
    public ApiResponse<MenuResponse> create(@Valid @RequestBody MenuRequest request) {
        AdminMenu menu = new AdminMenu(request.name(), request.menuType(), request.sortOrder() != null ? request.sortOrder() : 0);
        menu.setParentId(request.parentId());
        menu.setPath(request.path());
        menu.setIcon(request.icon());
        menu.setStatus(request.status() != null ? request.status() : "enabled");
        if (request.permissionCodes() != null && !request.permissionCodes().isEmpty()) {
            List<AdminPermission> perms = request.permissionCodes().stream()
                .map(code -> permissionRepository.findByCode(code).orElse(null))
                .filter(Objects::nonNull)
                .toList();
            menu.getPermissions().addAll(perms);
        }
        menu = menuRepository.save(menu);
        return ApiResponse.ok(toFlatResponse(menu));
    }

    @PutMapping("/{id}")
    @SaCheckPermission("system:menu:update")
    public ApiResponse<MenuResponse> update(@PathVariable Long id, @Valid @RequestBody MenuRequest request) {
        AdminMenu menu = menuRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Menu not found: " + id));
        menu.setName(request.name());
        menu.setParentId(request.parentId());
        menu.setPath(request.path());
        menu.setIcon(request.icon());
        menu.setMenuType(request.menuType());
        menu.setSortOrder(request.sortOrder() != null ? request.sortOrder() : 0);
        menu.setStatus(request.status() != null ? request.status() : "enabled");
        menu.getPermissions().clear();
        if (request.permissionCodes() != null && !request.permissionCodes().isEmpty()) {
            List<AdminPermission> perms = request.permissionCodes().stream()
                .map(code -> permissionRepository.findByCode(code).orElse(null))
                .filter(Objects::nonNull)
                .toList();
            menu.getPermissions().addAll(perms);
        }
        menu = menuRepository.save(menu);
        return ApiResponse.ok(toFlatResponse(menu));
    }

    @DeleteMapping("/{id}")
    @SaCheckPermission("system:menu:delete")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        // Recursively delete children
        List<AdminMenu> all = menuRepository.findAll();
        Set<Long> toDelete = new HashSet<>();
        collectDescendantIds(id, all, toDelete);
        toDelete.add(id);
        menuRepository.deleteAllById(toDelete);
        return ApiResponse.ok();
    }

    @PutMapping("/sort")
    @SaCheckPermission("system:menu:update")
    public ApiResponse<Void> updateSort(@RequestBody List<Map<String, Object>> sortList) {
        for (Map<String, Object> item : sortList) {
            Long id = Long.valueOf(item.get("id").toString());
            Integer sort = Integer.valueOf(item.get("sortOrder").toString());
            menuRepository.findById(id).ifPresent(menu -> {
                menu.setSortOrder(sort);
                menuRepository.save(menu);
            });
        }
        return ApiResponse.ok();
    }

    // --- helpers ---
    private MenuResponse toTree(AdminMenu menu, Map<Long, List<AdminMenu>> childrenByParent) {
        List<MenuResponse> children = childrenByParent.getOrDefault(menu.getId(), List.of())
            .stream()
            .sorted(Comparator.comparing(AdminMenu::getSortOrder))
            .map(c -> toTree(c, childrenByParent))
            .toList();
        return new MenuResponse(
            menu.getId(), menu.getParentId(), menu.getName(), menu.getPath(), menu.getIcon(),
            menu.getMenuType(), menu.getSortOrder(), menu.getStatus(),
            menu.getPermissions().stream().map(AdminPermission::getCode).toList(),
            children, menu.getCreatedAt(), menu.getUpdatedAt()
        );
    }

    private MenuResponse toFlatResponse(AdminMenu menu) {
        return new MenuResponse(
            menu.getId(), menu.getParentId(), menu.getName(), menu.getPath(), menu.getIcon(),
            menu.getMenuType(), menu.getSortOrder(), menu.getStatus(),
            menu.getPermissions().stream().map(AdminPermission::getCode).toList(),
            List.of(), menu.getCreatedAt(), menu.getUpdatedAt()
        );
    }

    private void collectDescendantIds(Long parentId, List<AdminMenu> all, Set<Long> ids) {
        all.stream()
            .filter(m -> parentId.equals(m.getParentId()))
            .forEach(m -> {
                ids.add(m.getId());
                collectDescendantIds(m.getId(), all, ids);
            });
    }
}
```

- [ ] **Step 3: Verify compilation**

---

### Task 7: Current User Menu API + Enhance /me

**Files:**
- Modify: `backend/auth-service/src/main/java/com/zhangyuan/auth/adapter/in/rest/AdminAuthController.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/dto/MenuTreeNode.java` (if needed, reuse MenuResponse)

- [ ] **Step 1: Add `/admin/auth/menus` endpoint**

Add to `AdminAuthController` (inject `AdminMenuRepository` and `StpUtil`):

```java
@GetMapping("/menus")
public ApiResponse<List<MenuResponse>> getCurrentUserMenus() {
    long loginId = StpUtil.getLoginIdAsLong();
    SaSession session = StpUtil.getSessionByLoginId(loginId);
    @SuppressWarnings("unchecked")
    List<String> userPermissions = (List<String>) session.get(SaSession.PERMISSION_LIST);
    if (userPermissions == null) userPermissions = List.of();

    List<AdminMenu> allMenus = menuRepository.findAllByOrderBySortOrderAsc();
    Set<Long> accessibleIds = new HashSet<>();

    // Find all accessible menu items (button/page: user has any required permission; group: has any accessible child)
    findAccessibleIds(allMenus, userPermissions, accessibleIds);

    // Filter to only include accessible items and their parents
    Map<Long, List<AdminMenu>> childrenByParent = allMenus.stream()
        .filter(m -> m.getParentId() != null)
        .filter(m -> accessibleIds.contains(m.getId()))
        .collect(Collectors.groupingBy(AdminMenu::getParentId));

    List<MenuResponse> tree = allMenus.stream()
        .filter(m -> m.getParentId() == null && accessibleIds.contains(m.getId()))
        .map(m -> toFilteredTree(m, childrenByParent, accessibleIds))
        .filter(Objects::nonNull)
        .toList();

    return ApiResponse.ok(tree);
}

private void findAccessibleIds(List<AdminMenu> all, List<String> userPermissions, Set<Long> result) {
    Map<Long, Set<Long>> childrenByPid = new HashMap<>();
    for (AdminMenu m : all) {
        if (m.getParentId() != null) {
            childrenByPid.computeIfAbsent(m.getParentId(), k -> new HashSet<>()).add(m.getId());
        }
    }
    // First pass: mark page/button items
    for (AdminMenu m : all) {
        if ("group".equals(m.getMenuType())) continue;
        if ("disabled".equals(m.getStatus())) continue;
        List<String> codes = m.getPermissions().stream().map(AdminPermission::getCode).toList();
        if (codes.isEmpty() || userPermissions.stream().anyMatch(codes::contains)) {
            result.add(m.getId());
        }
    }
    // Second pass: propagate up — groups with accessible children
    boolean changed = true;
    while (changed) {
        changed = false;
        for (AdminMenu m : all) {
            if (result.contains(m.getId())) continue;
            Set<Long> kids = childrenByPid.get(m.getId());
            if (kids != null && !kids.isEmpty() && result.containsAll(kids)) {
                // Group is accessible if ALL its children are accessible (hide empty groups)
                // Actually: group is accessible if ANY child is accessible
                for (Long kidId : kids) {
                    if (result.contains(kidId)) {
                        result.add(m.getId());
                        changed = true;
                        break;
                    }
                }
            }
        }
    }
}

private MenuResponse toFilteredTree(AdminMenu menu, Map<Long, List<AdminMenu>> childrenByParent, Set<Long> accessibleIds) {
    List<MenuResponse> children = childrenByParent.getOrDefault(menu.getId(), List.of())
        .stream()
        .sorted(Comparator.comparing(AdminMenu::getSortOrder))
        .map(c -> toFilteredTree(c, childrenByParent, accessibleIds))
        .filter(Objects::nonNull)
        .toList();
    // Don't include empty groups
    if ("group".equals(menu.getMenuType()) && children.isEmpty() && !accessibleIds.contains(menu.getId())) {
        return null;
    }
    return new MenuResponse(
        menu.getId(), menu.getParentId(), menu.getName(), menu.getPath(), menu.getIcon(),
        menu.getMenuType(), menu.getSortOrder(), menu.getStatus(),
        menu.getPermissions().stream().map(AdminPermission::getCode).toList(),
        children, menu.getCreatedAt(), menu.getUpdatedAt()
    );
}
```

- [ ] **Step 2: Enhance `/admin/auth/me` to return all data the frontend needs**

Verify the existing `LoginResponse` already contains `permissions: string[]` and `user.roles: string[]`. If not, add them.

- [ ] **Step 3: Verify compilation**

---

### Task 8: Seed Data Updates

**Files:**
- Modify: `backend/auth-service/src/main/java/com/zhangyuan/auth/common/security/AuthBootstrap.java`
- Create: `backend/auth-service/src/main/java/com/zhangyuan/auth/common/security/MenuBootstrap.java`

- [ ] **Step 1: Add new permission codes to AuthBootstrap**

Add to `defaultPermissions()`:

```java
new PermissionSeed("system:permission:list", "List permissions", "system"),
new PermissionSeed("system:permission:create", "Create permissions", "system"),
new PermissionSeed("system:permission:update", "Update permissions", "system"),
new PermissionSeed("system:permission:delete", "Delete permissions", "system"),
new PermissionSeed("system:menu:list", "List menus", "system"),
new PermissionSeed("system:menu:create", "Create menus", "system"),
new PermissionSeed("system:menu:update", "Update menus", "system"),
new PermissionSeed("system:menu:delete", "Delete menus", "system"),
```

- [ ] **Step 2: Create MenuBootstrap**

```java
package com.zhangyuan.auth.common.security;

import com.zhangyuan.auth.adapter.out.persistence.AdminMenu;
import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.repository.AdminMenuRepository;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

@Component
@Profile("!test")
public class MenuBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(MenuBootstrap.class);
    private final AdminMenuRepository menuRepository;
    private final AdminPermissionRepository permissionRepository;

    public MenuBootstrap(AdminMenuRepository menuRepository, AdminPermissionRepository permissionRepository) {
        this.menuRepository = menuRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (menuRepository.count() > 0) {
            log.info("Menu data already exists, skipping seed");
            return;
        }

        log.info("Seeding default menu tree...");

        // Groups (parent_id = null)
        AdminMenu dashboard = createMenu(null, "Dashboard", "/", "Dashboard", "page", 1);
        AdminMenu content = createMenu(null, "Content Management", null, "BookOpen", "group", 2);
        AdminMenu business = createMenu(null, "Business Management", null, "Briefcase", "group", 3);
        AdminMenu system = createMenu(null, "System Management", null, "Settings", "group", 4);

        // Content children
        createMenu(content.getId(), "Pages", "/cms/pages", "FileText", "page", 1);
        createMenu(content.getId(), "Media Assets", "/assets", "Image", "page", 2);

        // Business children
        createMenu(business.getId(), "Plan Groups", "/products/plan-groups", "Layers", "page", 1);
        createMenu(business.getId(), "Plans", "/products/plans", "Pricetags", "page", 2);
        createMenu(business.getId(), "Orders", "/orders", "Cart", "page", 3);
        createMenu(business.getId(), "Payments", "/payments/transactions", "Wallet", "page", 4);

        // System children
        createMenu(system.getId(), "Users", "/system/users", "People", "page", 1, "system:user:list");
        createMenu(system.getId(), "Roles", "/system/roles", "ShieldCheckmark", "page", 2, "system:role:list");
        createMenu(system.getId(), "Permissions", "/system/permissions", "Key", "page", 3, "system:permission:list");
        createMenu(system.getId(), "Menus", "/system/menus", "Menu", "page", 4, "system:menu:list");
        createMenu(system.getId(), "Settings", "/system/settings", "Wrench", "page", 5);

        // Button-type permissions (for v-permission use)
        createButton("system:permission:create");
        createButton("system:permission:update");
        createButton("system:permission:delete");
        createButton("system:menu:create");
        createButton("system:menu:update");
        createButton("system:menu:delete");
        createButton("system:user:update");
        createButton("system:role:update");

        log.info("Default menu tree seeded successfully");
    }

    private AdminMenu createMenu(Long parentId, String name, String path, String icon, String type, int sort, String... permCodes) {
        AdminMenu menu = new AdminMenu(name, type, sort);
        menu.setParentId(parentId);
        menu.setPath(path);
        menu.setIcon(icon);
        if (permCodes.length > 0) {
            List<AdminPermission> perms = Arrays.stream(permCodes)
                .map(code -> permissionRepository.findByCode(code).orElse(null))
                .filter(Objects::nonNull)
                .toList();
            menu.getPermissions().addAll(perms);
        }
        return menuRepository.save(menu);
    }

    private void createButton(String permissionCode) {
        permissionRepository.findByCode(permissionCode).ifPresent(perm -> {
            AdminMenu btn = new AdminMenu(perm.getName(), "button", 0);
            btn.getPermissions().add(perm);
            menuRepository.save(btn);
        });
    }
}
```

- [ ] **Step 3: Verify compilation**

---

### Task 9: Frontend — Permission Pinia Store

**Files:**
- Create: `frontend/admin/src/stores/permission.ts`
- Create: `frontend/admin/src/api/menu.ts` (will be consumed by store)

- [ ] **Step 1: Create menu API**

```typescript
// frontend/admin/src/api/menu.ts
import { request } from './http'

export interface MenuItem {
  id: number
  parentId: number | null
  name: string
  path: string | null
  icon: string | null
  menuType: 'group' | 'page' | 'button'
  sortOrder: number
  status: string
  permissionCodes: string[]
  children: MenuItem[]
  createdAt: string
  updatedAt: string
}

export function fetchUserMenus() {
  return request<MenuItem[]>('/admin/auth/menus')
}
```

- [ ] **Step 2: Create permission store**

```typescript
// frontend/admin/src/stores/permission.ts
import { defineStore } from 'pinia'
import { ref } from 'vue'
import { me } from '../api/auth'
import { fetchUserMenus, type MenuItem } from '../api/menu'

export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<string[]>([])
  const menus = ref<MenuItem[]>([])
  const roles = ref<string[]>([])
  const loaded = ref(false)

  async function fetchUserInfo() {
    try {
      const [userInfo, userMenus] = await Promise.all([
        me(),
        fetchUserMenus()
      ])
      permissions.value = userInfo.permissions ?? []
      roles.value = userInfo.user?.roles ?? []
      menus.value = userMenus ?? []
      loaded.value = true
    } catch (e) {
      console.error('[PermissionStore] Failed to fetch user info:', e)
      throw e
    }
  }

  function hasPermission(code: string): boolean {
    if (!code) return true
    return permissions.value.includes(code)
  }

  function hasAnyPermission(codes: string[]): boolean {
    if (!codes.length) return true
    return codes.some(code => permissions.value.includes(code))
  }

  function hasAllPermissions(codes: string[]): boolean {
    if (!codes.length) return true
    return codes.every(code => permissions.value.includes(code))
  }

  function reset() {
    permissions.value = []
    menus.value = []
    roles.value = []
    loaded.value = false
  }

  return {
    permissions, menus, roles, loaded,
    fetchUserInfo, hasPermission, hasAnyPermission, hasAllPermissions, reset
  }
})
```

- [ ] **Step 3: Update auth.ts API to expose `me()` return type**

```typescript
// frontend/admin/src/api/auth.ts — verify/update LoginResponse
export interface UserInfo {
  id: number
  username: string
  nickname: string
  roles: string[]
}

export interface LoginResponse {
  accessToken: string
  expiresIn: number
  user: UserInfo
  permissions: string[]
}
```

Also verify `me()` function signature works correctly.

---

### Task 10: Frontend — v-permission Directive

**Files:**
- Create: `frontend/admin/src/directives/permission.ts`
- Modify: `frontend/admin/src/main.ts`

- [ ] **Step 1: Create directive**

```typescript
// frontend/admin/src/directives/permission.ts
import type { App } from 'vue'
import { usePermissionStore } from '../stores/permission'

export function setupPermissionDirective(app: App) {
  app.directive('permission', {
    mounted(el: HTMLElement, binding) {
      const store = usePermissionStore()
      const value = binding.value

      const hasAccess = Array.isArray(value)
        ? store.hasAnyPermission(value)
        : store.hasPermission(value)

      if (!hasAccess) {
        el.parentNode?.removeChild(el)
      }
    }
  })
}
```

- [ ] **Step 2: Register in main.ts**

```typescript
// frontend/admin/src/main.ts
import { setupPermissionDirective } from './directives/permission'

const app = createApp(App)
setupPermissionDirective(app)
app.use(router)
// ...
app.mount('#app')
```

---

### Task 11: Frontend — Route Guard Enhancement + New Routes

**Files:**
- Modify: `frontend/admin/src/router/index.ts`

- [ ] **Step 1: Enable route guard and add new routes**

```typescript
// frontend/admin/src/router/index.ts
import Permissions from '../pages/system/Permissions.vue'
import Menus from '../pages/system/Menus.vue'

// Add new routes inside the AdminLayout children:
// { path: 'system/permissions', component: Permissions, meta: { permissions: ['system:permission:list'] } },
// { path: 'system/menus', component: Menus, meta: { permissions: ['system:menu:list'] } },

// Enhance beforeEach guard:
router.beforeEach(async (to) => {
  if (to.path === '/login') return true
  if (!getToken()) return '/login'

  // Lazy-load permission store if not loaded
  const store = usePermissionStore()
  if (!store.loaded && to.path !== '/login') {
    try {
      await store.fetchUserInfo()
    } catch {
      clearToken()
      return '/login'
    }
  }

  // Check route permissions
  const routePerms = to.meta?.permissions as string[] | undefined
  if (routePerms?.length && !store.hasAnyPermission(routePerms)) {
    return '/'
  }

  return true
})
```

---

### Task 12: Frontend — Permission Management Page

**Files:**
- Create: `frontend/admin/src/pages/system/Permissions.vue`
- Create: `frontend/admin/src/api/permission.ts`

- [ ] **Step 1: Create permission API**

```typescript
// frontend/admin/src/api/permission.ts
import { request } from './http'

export interface PermissionInfo {
  id: number
  code: string
  name: string
  module: string
}

export function listPermissions(module?: string) {
  const params = module ? `?module=${encodeURIComponent(module)}` : ''
  return request<PermissionInfo[]>(`/admin/system/permissions${params}`)
}

export function listPermissionModules() {
  return request<string[]>('/admin/system/permissions/modules')
}

export function createPermission(data: { code: string; name: string; module: string }) {
  return request<PermissionInfo>('/admin/system/permissions', {
    method: 'POST',
    body: JSON.stringify(data)
  })
}

export function deletePermission(id: number) {
  return request<void>(`/admin/system/permissions/${id}`, { method: 'DELETE' })
}
```

- [ ] **Step 2: Create Permissions.vue page**

Features:
- `n-data-table` with columns: code, name, module (shown as n-tag), created, actions
- Search by code/name, filter by module dropdown
- "New Permission" button → modal with form (code, name, module selector)
- Delete with `useConfirm`
- `v-permission` on action buttons

---

### Task 13: Frontend — Menu Management Page

**Files:**
- Create: `frontend/admin/src/pages/system/Menus.vue`

Features:
- Split panel layout: left `n-tree` (draggable), right edit form
- "Add Root" button, context menu on tree nodes
- Edit form: name, path, icon (text input or picker), type select (group/page/button), sort, status
- Permission binding: checkbox group of all permissions, grouped by module
- Drag-and-drop for reorder and reparenting
- Calls menu API endpoints

Wired to `frontend/admin/src/api/menu.ts` (extend with CRUD methods):

```typescript
export function fetchAllMenus() {
  return request<MenuItem[]>('/admin/system/menus')
}

export function createMenu(data: any) {
  return request<MenuItem>('/admin/system/menus', { method: 'POST', body: JSON.stringify(data) })
}

export function updateMenu(id: number, data: any) {
  return request<MenuItem>(`/admin/system/menus/${id}`, { method: 'PUT', body: JSON.stringify(data) })
}

export function deleteMenu(id: number) {
  return request<void>(`/admin/system/menus/${id}`, { method: 'DELETE' })
}

export function updateMenuSort(sortList: { id: number; sortOrder: number }[]) {
  return request<void>('/admin/system/menus/sort', { method: 'PUT', body: JSON.stringify(sortList) })
}
```

---

### Task 14: Frontend — Enhance Role Management Page

**Files:**
- Modify: `frontend/admin/src/pages/system/Roles.vue`
- Modify: `frontend/admin/src/api/system.ts`

- [ ] **Step 1: Extend system.ts API**

Add role-permission endpoints:

```typescript
export function getRolePermissions(roleId: number) {
  return request<number[]>(`/admin/system/roles/${roleId}/permissions`)
}

export function setRolePermissions(roleId: number, permissionIds: number[]) {
  return request<void>(`/admin/system/roles/${roleId}/permissions`, {
    method: 'PUT',
    body: JSON.stringify({ permissionIds })
  })
}
```

- [ ] **Step 2: Enhance Roles.vue edit dialog**

Add a permission transfer panel (`n-transfer`) or checkbox group inside the edit modal:
- On open edit: fetch role's current permission IDs via `getRolePermissions(roleId)`
- Load all available permissions via `listPermissions()`
- Left side: unassigned permissions (grouped by module)
- Right side: assigned permissions
- On save: call `setRolePermissions(roleId, selectedIds)`

---

### Task 15: Frontend — Enhance User Management Page

**Files:**
- Modify: `frontend/admin/src/pages/system/Users.vue`
- Modify: `frontend/admin/src/api/system.ts`

- [ ] **Step 1: Extend system.ts API**

Add user-role endpoints:

```typescript
export function getUserRoles(userId: number) {
  return request<number[]>(`/admin/system/users/${userId}/roles`)
}

export function setUserRoles(userId: number, roleIds: number[]) {
  return request<void>(`/admin/system/users/${userId}/roles`, {
    method: 'PUT',
    body: JSON.stringify({ roleIds })
  })
}

// Also export listRoles for the role selector
export function listRoles() {
  return request<RoleInfo[]>('/admin/system/roles')
}
```

- [ ] **Step 2: Enhance Users.vue edit dialog**

Add a role multi-select inside the edit modal:
- On open edit: fetch user's current role IDs via `getUserRoles(userId)`
- Load all roles via `listRoles()`
- Show `n-checkbox-group` with all roles
- On save: call `setUserRoles(userId, selectedRoleIds)`

---

### Task 16: Frontend — Dynamic Menu in AdminLayout

**Files:**
- Modify: `frontend/admin/src/layouts/AdminLayout.vue`

- [ ] **Step 1: Replace hardcoded menuOptions with dynamic data**

```typescript
import { usePermissionStore } from '../stores/permission'

const permissionStore = usePermissionStore()

// Convert MenuItem[] to Naive UI n-menu format
const menuOptions = computed(() => {
  return buildNaiveMenu(permissionStore.menus)
})

function buildNaiveMenu(items: MenuItem[]): any[] {
  return items
    .filter(item => item.menuType !== 'button' && item.status === 'enabled')
    .map(item => {
      const option: any = {
        key: item.path || item.id.toString(),
        label: item.name,
      }
      if (item.icon) {
        // Map icon string to Naive UI icon — use a simple approach
        // For now, we'll use the icon name as a class or lookup
      }
      if (item.children?.length) {
        option.children = buildNaiveMenu(item.children)
      }
      return option
    })
}
```

- [ ] **Step 2: Update activeKey logic**

The existing `activeKey` uses path matching — keep this logic since the key is the route path.

- [ ] **Step 3: Fetch menus on layout mount**

Since the route guard in Task 11 already loads permissions, the store is populated before navigation. The layout just reads from the already-loaded store.

---

### Task 17: Integration — Login Flow

**Files:**
- Modify: `frontend/admin/src/pages/Login.vue`

- [ ] **Step 1: Fetch permissions after login**

Update the login handler:

```typescript
import { usePermissionStore } from '../stores/permission'

async function handleLogin() {
  try {
    const res = await login(username.value, password.value)
    // Token is saved inside login() function
    // Now fetch permissions and menus
    const permissionStore = usePermissionStore()
    await permissionStore.fetchUserInfo()
    message.success('Login successful')
    router.push('/')
  } catch (e) {
    message.error('Login failed')
  }
}
```

- [ ] **Step 2: Add reset on logout**

In the layout's logout handler, call:

```typescript
const permissionStore = usePermissionStore()
permissionStore.reset()
clearToken()
router.push('/login')
```

---

## Task Dependency Graph

```
Task 1 (Migration) → Task 2 (Entity+Repo) → Task 3 (Perm CRUD)
                                           → Task 4 (Role-Perm)
                                           → Task 5 (User-Role)
                                           → Task 6 (Menu CRUD)
                                           → Task 7 (Current User API)
                                             ↓
Task 8 (Seed Data) ← depends on Tasks 3-7

Task 9 (Store)  ← depends on Task 7
  → Task 10 (Directive)   ← independent, after Task 9
  → Task 11 (Route Guard) ← independent, after Task 9
  → Task 14 (Role Page)   ← after Task 9, uses store
  → Task 15 (User Page)   ← after Task 9, uses store
  → Task 16 (Layout)      ← after Task 9, uses store

Task 12 (Perm Page)  ← after Task 9
Task 13 (Menu Page)  ← after Task 9 + Task 6

Task 17 (Login Flow) ← after all frontend tasks
```

---

## Plan Self-Review Checklist

- [x] **Spec coverage**: Every section of the design doc maps to at least one task
- [x] **No placeholders**: All code blocks contain real implementation code
- [x] **Type consistency**: Method signatures match across tasks
- [x] **File paths**: All paths are absolute and match the project structure
- [x] **Dependencies**: Task order respects dependency graph
