# Permission System Enhancement Design

**Date:** 2026-06-04
**Status:** Draft
**Version:** 1.0

## 1. Overview

Enhance the existing RBAC permission system to provide full-featured permission management, role-permission assignment, user-role assignment, configurable menu tree, and frontend permission directives.

### Goals

- Provide CRUD management for permissions, roles, and menus in the admin frontend
- Enable role-permission and user-role assignment through UI
- Support configurable tree-structured navigation menus with permission-based visibility
- Implement frontend `v-permission` directive for button-level access control
- Add route permission guards for page-level access control
- Design with future cross-system OAuth2 extensibility in mind

## 2. Data Model

### 2.1 Existing Tables (unchanged)

- `admin_user` — Admin users
- `admin_role` — Roles
- `admin_permission` — Permission codes
- `admin_user_role` — User-Role many-to-many
- `admin_role_permission` — Role-Permission many-to-many

### 2.2 New Tables

#### admin_menu

| Column | Type | Description |
|--------|------|-------------|
| id | BIGSERIAL PK | Auto-increment ID |
| parent_id | BIGINT FK → admin_menu.id | Parent node for tree structure |
| name | VARCHAR(64) NOT NULL | Display name |
| path | VARCHAR(255) | Vue route path (nullable for group/button) |
| icon | VARCHAR(64) | Naive UI icon name |
| menu_type | VARCHAR(16) NOT NULL DEFAULT 'page' | `group` / `page` / `button` |
| sort_order | INT NOT NULL DEFAULT 0 | Ordering within same parent |
| status | VARCHAR(16) NOT NULL DEFAULT 'enabled' | `enabled` / `disabled` |
| created_at | TIMESTAMPTZ DEFAULT NOW() | |
| updated_at | TIMESTAMPTZ DEFAULT NOW() | |

#### admin_menu_permission

| Column | Type | Description |
|--------|------|-------------|
| menu_id | BIGINT FK → admin_menu.id ON DELETE CASCADE | Menu item |
| permission_code | VARCHAR(128) FK → admin_permission.code | Required permission code |

PRIMARY KEY (menu_id, permission_code) — A menu item can require multiple permission codes (OR logic: any one grants visibility).

### 2.3 Menu Types

| Type | Meaning | Sidebar | Example |
|------|---------|---------|---------|
| `group` | Collapsible group header | Rendered as group label | "System Management" |
| `page` | Clickable navigation item | Rendered as link → route | "User Management" → `/system/users` |
| `button` | Permission identifier only | Not rendered | "Create User" button for v-permission |

### 2.4 Future Extensibility

The `admin_permission` table already has a `module` field for grouping permissions by business domain. For future cross-system scenarios (OAuth2), a `application` or `service` field can be added to isolate permission namespaces between different systems. The menu tree can also be scoped by application in the future.

## 3. Backend API Design

### 3.1 Permission Management (new controller)

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/admin/system/permissions` | List permissions (filterable by module) | `system:permission:list` |
| POST | `/admin/system/permissions` | Create permission | `system:permission:create` |
| PUT | `/admin/system/permissions/{id}` | Update permission | `system:permission:update` |
| DELETE | `/admin/system/permissions/{id}` | Delete permission | `system:permission:delete` |
| GET | `/admin/system/permissions/modules` | List distinct module names | `system:permission:list` |

**Request body (create/update):**
```json
{
  "code": "asset:upload",
  "name": "Upload Assets",
  "module": "asset"
}
```

### 3.2 Role-Permission Assignment (enhance existing)

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/admin/system/roles/{id}/permissions` | Get permission IDs for role | `system:role:list` |
| PUT | `/admin/system/roles/{id}/permissions` | Set permissions for role | `system:role:update` |

**Request body (set permissions):**
```json
{
  "permissionIds": [1, 2, 3, 5, 8]
}
```

### 3.3 User-Role Assignment (enhance existing)

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/admin/system/users/{id}/roles` | Get role IDs for user | `system:user:list` |
| PUT | `/admin/system/users/{id}/roles` | Set roles for user | `system:user:update` |

**Request body (set roles):**
```json
{
  "roleIds": [1, 2]
}
```

### 3.4 Menu Management (new controller)

| Method | Path | Description | Permission |
|--------|------|-------------|------------|
| GET | `/admin/system/menus` | Get full menu tree (unfiltered) | `system:menu:list` |
| POST | `/admin/system/menus` | Create menu item | `system:menu:create` |
| PUT | `/admin/system/menus/{id}` | Update menu item | `system:menu:update` |
| DELETE | `/admin/system/menus/{id}` | Delete menu node (with children) | `system:menu:delete` |
| PUT | `/admin/system/menus/sort` | Batch update sort orders (drag & drop) | `system:menu:update` |

**Request body (create/update):**
```json
{
  "parentId": null,
  "name": "User Management",
  "path": "/system/users",
  "icon": "PersonOutline",
  "menuType": "page",
  "sortOrder": 1,
  "status": "enabled",
  "permissionCodes": ["system:user:list", "system:user:manage"]
}
```

**Response (tree structure):**
```json
[
  {
    "id": 1,
    "parentId": null,
    "name": "Dashboard",
    "path": "/",
    "icon": "Dashboard",
    "menuType": "page",
    "sortOrder": 1,
    "status": "enabled",
    "permissionCodes": [],
    "children": []
  },
  {
    "id": 2,
    "parentId": null,
    "name": "System",
    "path": null,
    "icon": "Settings",
    "menuType": "group",
    "sortOrder": 10,
    "status": "enabled",
    "permissionCodes": [],
    "children": [
      {
        "id": 5,
        "parentId": 2,
        "name": "User Management",
        "path": "/system/users",
        "icon": "People",
        "menuType": "page",
        "sortOrder": 1,
        "status": "enabled",
        "permissionCodes": ["system:user:list"],
        "children": []
      }
    ]
  }
]
```

### 3.5 Current User APIs

| Method | Path | Description |
|--------|------|-------------|
| GET | `/admin/auth/me` | Get current user info (roles, permissions) — **enhanced: include permission codes** |
| GET | `/admin/auth/menus` | Get filtered menu tree (only items user has permission for) |

**`/admin/auth/menus` response** — same tree structure as section 3.4, but filtered:
- Items with `type: 'group'` are included if any child is accessible
- Items with `type: 'page'` or `type: 'button'` are included if user has any of the required permission codes (or if no permission codes are required)
- Items with `status: 'disabled'` are excluded

### 3.6 Permission Check API

| Method | Path | Description |
|--------|------|-------------|
| POST | `/api/auth/check` | Batch check permissions (for external systems) |

**Request:**
```json
{
  "codes": ["asset:list", "asset:upload", "order:read"]
}
```

**Response:**
```json
{
  "results": {
    "asset:list": true,
    "asset:upload": false,
    "order:read": true
  }
}
```

### 3.7 New Permission Codes to Seed

Add to `AuthBootstrap.java`:

```
system:permission:list
system:permission:create
system:permission:update
system:permission:delete
system:menu:list
system:menu:create
system:menu:update
system:menu:delete
```

## 4. Frontend Architecture

### 4.1 Permission Store (`stores/permission.ts`)

```typescript
export const usePermissionStore = defineStore('permission', () => {
  const permissions = ref<string[]>([])
  const menus = ref<MenuItem[]>([])
  const roles = ref<string[]>([])

  async function fetchUserInfo() {
    const [userInfo, userMenus] = await Promise.all([
      me(),             // GET /admin/auth/me
      fetchMenus()      // GET /admin/auth/menus
    ])
    permissions.value = userInfo.permissions
    roles.value = userInfo.user.roles
    menus.value = userMenus
  }

  function hasPermission(code: string): boolean {
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
})
```

### 4.2 `v-permission` Directive

```typescript
// directives/permission.ts
app.directive('permission', {
  mounted(el, binding) {
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
```

Usage:
```html
<n-button v-permission="'asset:upload'">Upload</n-button>
<n-button v-permission="['asset:upload', 'asset:delete']">Manage</n-button>
```

### 4.3 Route Guard Enhancement

```typescript
// router/index.ts
router.beforeEach((to) => {
  if (to.path === '/login') return true
  if (!getToken()) return '/login'

  const store = usePermissionStore()
  const permissions = to.meta?.permissions as string[] | undefined
  if (permissions?.length && !store.hasAnyPermission(permissions)) {
    return '/403'
  }
})
```

**Route metadata for permission protection:**
```typescript
{
  path: '/system/users',
  component: Users,
  meta: { permissions: ['system:user:list'] }
}
```

### 4.4 Dynamic Menu Rendering

In `AdminLayout.vue`, replace the hardcoded `menuOptions` with a computed property derived from `permissionStore.menus`:

```typescript
const menuItems = computed(() => {
  return buildMenuTree(permissionStore.menus)
})
```

The menu tree from `/admin/auth/menus` is already filtered by permissions on the backend, so no additional client-side filtering is needed.

### 4.5 Login Flow Update

In `Login.vue`, after successful login, call `permissionStore.fetchUserInfo()` to populate permissions and menus before navigating to the dashboard.

## 5. UI Design

### 5.1 Permission Management Page (`/system/permissions`)

- **Layout**: Table with module-grouped rows
- **Columns**: Permission Code, Name, Module, Created At, Actions
- **Filtering**: Search by code/name, filter by module dropdown
- **CRUD**: Create/edit via modal dialog, delete with confirmation
- **Form fields**: code (with `xxx:yyy` format validation), name, module

### 5.2 Menu Management Page (`/system/menus`)

- **Layout**: Split panel — left tree view, right edit form
- **Tree**: `n-tree` with draggable nodes, context menu (add child, delete, move up/down)
- **Edit form**: name, path, icon (picker), type (select: group/page/button), sort order, status
- **Permission binding**: Checkbox list of all permissions, grouped by module
- **Drag & drop**: Reorder and change parent via drag

### 5.3 Enhanced Role Edit Dialog

- **Existing fields**: code (readonly), name
- **New field**: Permission transfer (`n-transfer`) — left side shows unassigned permissions, right side shows assigned permissions, grouped by module with search

### 5.4 Enhanced User Edit Dialog

- **Existing fields**: nickname, email, status
- **New field**: Role selection — multi-select checkbox list of all roles

## 6. Seed Data Updates

Add to `AuthBootstrap.defaultPermissions()`:

```java
// Permission management
new PermissionSeed("system:permission:list", "List permissions", "system"),
new PermissionSeed("system:permission:create", "Create permissions", "system"),
new PermissionSeed("system:permission:update", "Update permissions", "system"),
new PermissionSeed("system:permission:delete", "Delete permissions", "system"),
// Menu management
new PermissionSeed("system:menu:list", "List menus", "system"),
new PermissionSeed("system:menu:create", "Create menus", "system"),
new PermissionSeed("system:menu:update", "Update menus", "system"),
new PermissionSeed("system:menu:delete", "Delete menus", "system"),
```

Add default menu tree seed data in a new `MenuBootstrap.java`:
- Dashboard
- Content Management (group) → Page Management, Media Assets
- Business Management (group) → Plan Groups, Plans, Orders, Payments
- System Management (group) → User Management, Role Management, Permission Management, Menu Management, System Settings
- Top-level button items for all existing permissions (for v-permission use)

## 7. Non-Goals (Out of Scope)

- OAuth2 / SSO integration (future)
- Fine-grained field-level data permissions
- Operation audit log (future)
- Multi-tenant permission isolation

## 8. Technical Considerations

- **Flyway migration**: Add `V002__create_admin_menu.sql` to auth-service (enable Flyway for auth-service)
- **Data consistency**: Deleting a permission should warn if it's bound to any menu or role
- **Caching**: Permission changes require users to re-login (or implement session refresh)
- **Nacos service registration**: No changes needed — new endpoints are within existing auth-service
- **Backward compatibility**: All existing APIs remain unchanged; old token format works
