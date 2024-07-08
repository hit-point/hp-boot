package com.hpboot.interceptor;

import cn.dev33.satoken.stp.StpInterface;
import com.hpboot.domain.entity.SysRole;
import com.hpboot.service.ISysMenuService;
import com.hpboot.service.ISysRoleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class StpInterfaceImpl implements StpInterface {
    @Autowired
    private ISysMenuService menuService;
    @Autowired
    private ISysRoleService sysRoleService;

    /**
     * 返回一个账号所拥有的权限码集合
     */
    @Override
    public List<String> getPermissionList(Object userName, String loginType) {
        return menuService.selectMenuPermsByUserName(String.valueOf(userName));
    }

    /**
     * 返回一个账号所拥有的角色标识集合 (权限与角色可分开校验)
     */
    @Override
    public List<String> getRoleList(Object userName, String loginType) {
        List<SysRole> sysRoles = sysRoleService.selectRolesByUserName(String.valueOf(userName));
        //角色列表
        return sysRoles.stream().map(SysRole::getRoleKey).collect(Collectors.toList());
    }
}
