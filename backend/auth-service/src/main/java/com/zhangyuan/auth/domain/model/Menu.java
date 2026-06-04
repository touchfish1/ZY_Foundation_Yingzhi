package com.zhangyuan.auth.domain.model;

import java.util.ArrayList;
import java.util.List;

public class Menu {

    private Long id;
    private Long parentId;
    private String name;
    private String path;
    private String icon;
    private String menuType;   // group, page, button
    private Integer sortOrder;
    private String status;
    private List<String> permissionCodes = new ArrayList<>();

    public Menu() {}

    public Menu(String name, String menuType, Integer sortOrder) {
        this.name = name;
        this.menuType = menuType;
        this.sortOrder = sortOrder;
    }

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
    public List<String> getPermissionCodes() { return permissionCodes; }
    public void setPermissionCodes(List<String> permissionCodes) { this.permissionCodes = permissionCodes; }
}
