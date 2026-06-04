package com.zhangyuan.common.security;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * StpInterface 实现 — 从 Redis 共享会话中读取权限和角色。
 * <p>
 * auth-service 登录时将权限/角色写入 SaSession（通过 sa-token-redis-jackson 存入 Redis），
 * 本服务通过 {@link StpUtil#getSessionByLoginId(Object)} 读取同一会话。
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        if (session == null) {
            return new ArrayList<>();
        }
        @SuppressWarnings("unchecked")
        List<String> permissionList = (List<String>) session.get(SaSession.PERMISSION_LIST);
        return permissionList != null ? permissionList : new ArrayList<>();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        SaSession session = StpUtil.getSessionByLoginId(loginId);
        if (session == null) {
            return new ArrayList<>();
        }
        @SuppressWarnings("unchecked")
        List<String> roleList = (List<String>) session.get(SaSession.ROLE_LIST);
        return roleList != null ? roleList : new ArrayList<>();
    }
}
