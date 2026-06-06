package com.zhangyuan.payment.common;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
