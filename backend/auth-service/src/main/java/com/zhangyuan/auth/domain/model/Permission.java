package com.zhangyuan.auth.domain.model;

public class Permission {

    private Long id;
    private String code;
    private String name;
    private String module;

    public Permission(String code, String name, String module) {
        this.code = code;
        this.name = name;
        this.module = module;
    }

    public Permission(Long id, String code, String name, String module) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.module = module;
    }

    public Long getId() { return id; }
    public String getCode() { return code; }
    public String getName() { return name; }
    public String getModule() { return module; }
    public void setName(String name) { this.name = name; }
    public void setModule(String module) { this.module = module; }
}
