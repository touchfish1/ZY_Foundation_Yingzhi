package com.zhangyuan.auth.application.service;

import com.zhangyuan.auth.adapter.out.persistence.AdminPermission;
import com.zhangyuan.auth.adapter.out.persistence.AdminRole;
import com.zhangyuan.common.response.PageResponse;
import com.zhangyuan.auth.domain.model.Permission;
import com.zhangyuan.auth.domain.repository.PermissionRepository;
import com.zhangyuan.auth.dto.PermissionRequest;
import com.zhangyuan.auth.dto.PermissionResponse;
import com.zhangyuan.auth.repository.AdminPermissionRepository;
import com.zhangyuan.auth.repository.AdminRoleRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;

@Service
public class PermissionApplicationService {

    private final PermissionRepository permissionRepository;
    private final AdminRoleRepository adminRoleRepository;
    private final AdminPermissionRepository adminPermissionRepository;

    public PermissionApplicationService(PermissionRepository permissionRepository,
                                        AdminRoleRepository adminRoleRepository,
                                        AdminPermissionRepository adminPermissionRepository) {
        this.permissionRepository = permissionRepository;
        this.adminRoleRepository = adminRoleRepository;
        this.adminPermissionRepository = adminPermissionRepository;
    }

    @Transactional(readOnly = true)
    public List<PermissionResponse> listPermissions(String module) {
        return permissionRepository.findAll().stream()
                .filter(p -> module == null || module.isBlank() || module.equals(p.getModule()))
                .sorted(Comparator.comparing(Permission::getModule).thenComparing(Permission::getCode))
                .map(p -> new PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getModule()))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<String> listModules() {
        return permissionRepository.findAll().stream()
                .map(Permission::getModule)
                .distinct()
                .sorted()
                .toList();
    }

    @Transactional
    public PermissionResponse createPermission(PermissionRequest request) {
        if (permissionRepository.findByCode(request.code()).isPresent()) {
            throw new IllegalArgumentException("Permission code already exists: " + request.code());
        }
        Permission domain = permissionRepository.save(new Permission(request.code(), request.name(), request.module()));
        return new PermissionResponse(domain.getId(), domain.getCode(), domain.getName(), domain.getModule());
    }

    @Transactional
    public PermissionResponse updatePermission(Long id, PermissionRequest request) {
        Permission existing = permissionRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Permission not found: " + id));
        existing.setName(request.name());
        existing.setModule(request.module());
        Permission saved = permissionRepository.save(existing);
        return new PermissionResponse(saved.getId(), saved.getCode(), saved.getName(), saved.getModule());
    }

    @Transactional
    public void deletePermission(Long id) {
        var rolesWithPerm = adminRoleRepository.findAll().stream()
                .filter(r -> r.getPermissions().stream().anyMatch(p -> p.getId().equals(id)))
                .toList();
        for (var role : rolesWithPerm) {
            role.getPermissions().removeIf(p -> p.getId().equals(id));
            adminRoleRepository.save(role);
        }
        permissionRepository.deleteById(id);
    }

    // Role-Permission assignment (needs JPA entity access for the join table)
    @Transactional(readOnly = true)
    public List<Long> getRolePermissionIds(Long roleId) {
        AdminRole role = adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        return role.getPermissions().stream()
                .map(AdminPermission::getId)
                .toList();
    }

    @Transactional
    public void setRolePermissions(Long roleId, List<Long> permissionIds) {
        AdminRole role = adminRoleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Role not found: " + roleId));
        List<AdminPermission> permissions = adminPermissionRepository.findAllById(permissionIds);
        role.getPermissions().clear();
        role.getPermissions().addAll(permissions);
        adminRoleRepository.save(role);
    }

    @Transactional(readOnly = true)
    public PageResponse<PermissionResponse> listPermissionsPaginated(
            List<String> modules, String keyword, int page, int pageSize) {

        PageRequest pageable = PageRequest.of(page - 1, pageSize, Sort.by("module", "code"));

        Specification<AdminPermission> spec = Specification.where(null);
        if (modules != null && !modules.isEmpty()) {
            spec = spec.and((root, query, cb) -> root.get("module").in(modules));
        }
        if (keyword != null && !keyword.isBlank()) {
            String pattern = "%" + keyword.toLowerCase() + "%";
            spec = spec.and((root, query, cb) ->
                cb.or(
                    cb.like(cb.lower(root.get("code")), pattern),
                    cb.like(cb.lower(root.get("name")), pattern)
                )
            );
        }

        Page<AdminPermission> pageResult = adminPermissionRepository.findAll(spec, pageable);
        List<PermissionResponse> items = pageResult.getContent().stream()
                .map(p -> new PermissionResponse(p.getId(), p.getCode(), p.getName(), p.getModule()))
                .toList();

        return PageResponse.from(pageResult, items);
    }
}
