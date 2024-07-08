package com.hpboot.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hpboot.common.core.annotation.Log;
import com.hpboot.common.core.annotation.RequiresPermissions;
import com.hpboot.common.core.constant.UserConstants;
import com.hpboot.common.core.domain.R;
import com.hpboot.common.core.enums.BusinessType;
import com.hpboot.common.core.web.domain.AjaxResult;
import com.hpboot.common.core.web.page.TableDataInfo;
import com.hpboot.domain.entity.SysDept;
import com.hpboot.domain.entity.SysRole;
import com.hpboot.domain.entity.SysUser;
import com.hpboot.domain.entity.SysUserRole;
import com.hpboot.domain.vo.LoginUser;
import com.hpboot.service.ISysDeptService;
import com.hpboot.service.ISysRoleService;
import com.hpboot.service.ISysUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

/**
 * 角色信息
 *
 * 
 */
@Slf4j
@RestController
@RequestMapping("/system/role")
public class SysRoleController extends BaseController {
    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;
    @Autowired
    private ISysUserService userService;

    /**
     * 获取角色
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/list")
    public TableDataInfo getSysRole( SysRole role){
        startPage();
        List<SysRole> list = roleService.selectRoleList(role);
        return getDataTable(list);
    }
    /**
     * 根据角色id获取角色
     */
    @RequiresPermissions("system:role:query")
    @GetMapping(value = {"/", "/{roleId}"})
    public R<?> getSysRoleById(@PathVariable(value = "roleId", required = false) Long roleId){
        SysRole list = roleService.selectRoleById(roleId);
        return R.ok(list);
    }

    /**
     * 新增角色
     */
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping("/add")
    public AjaxResult addSave(@RequestBody SysRole role) {
        if (!roleService.checkRoleNameUnique(role)) {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        } else if (!roleService.checkRoleKeyUnique(role)) {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        role.setCreateBy(loginUser.getUsername());
        return toAjax(roleService.insertRole(role));
    }

    /**
     * 新增角色
     */
    @RequiresPermissions("system:role:add")
    @Log(title = "角色管理", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysRole role)
    {
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("新增角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        role.setCreateBy(loginUser.getSysUser().getUserName());
        return toAjax(roleService.insertRole(role));

    }

//    /**
//     * 修改保存角色
//     */
//    @RequiresPermissions("system:role:edit")
//    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
//    @PostMapping()
//    public AjaxResult editSave(@RequestBody SysRole role)
//    {
//        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
//        role.setUpdateBy(loginUser.getSysUser().getUserName());
//        //只能admin用户修改角色
//        if ("admin".equals(loginUser.getSysUser().getUserName())) {
//            if (!roleService.checkRoleNameUnique(role))
//            {
//                return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
//            }
//            else if (!roleService.checkRoleKeyUnique(role))
//            {
//                return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
//            }
//            return toAjax(roleService.updateRole(role));
//        }
//
//        return success();
//    }
    /**
     * 修改保存角色
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@Validated @RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        if (!roleService.checkRoleNameUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色名称已存在");
        }
        else if (!roleService.checkRoleKeyUnique(role))
        {
            return error("修改角色'" + role.getRoleName() + "'失败，角色权限已存在");
        }
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        role.setUpdateBy(loginUser.getSysUser().getUserName());
        return toAjax(roleService.updateRole(role));
    }


    /**
     * 根据角色id删除角色
     * @param roleId
     * @return
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @DeleteMapping("/{roleId}")
    public AjaxResult removeByRoleId(@PathVariable(value = "roleId", required = false) Long roleId) {
        int i = roleService.deleteRoleById(roleId);
        if (i > 0) {
          return success("删除成功");
        }
        return error("删除失败");
    }

    /**
     * 获取角色选择框列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping("/optionselect")
    public AjaxResult optionselect()
    {
        return success(roleService.selectRoleAll());
    }
    /**
     * 查询已分配用户角色列表
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/authUser/allocatedList")
    public TableDataInfo allocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectAllocatedList(user);
        for (SysUser s: list) {
            s.setCreateTime(new Date());
        }
        return getDataTable(list);
    }

    /**
     * 查询未分配用户角色列表
     */
    @RequiresPermissions("system:role:list")
    @GetMapping("/authUser/unallocatedList")
    public TableDataInfo unallocatedList(SysUser user)
    {
        startPage();
        List<SysUser> list = userService.selectUnallocatedList(user);
        for (SysUser s: list) {
            s.setCreateTime(new Date());
        }
        return getDataTable(list);
    }

    /**
     * 取消授权用户
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancel")
    public AjaxResult cancelAuthUser(@RequestBody SysUserRole userRole)
    {
        return toAjax(roleService.deleteAuthUser(userRole));
    }

    /**
     * 批量取消授权用户
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/cancelAll")
    public AjaxResult cancelAuthUserAll(Long roleId, Long[] userIds)
    {
        return toAjax(roleService.deleteAuthUsers(roleId, userIds));
    }

    /**
     * 批量选择用户授权
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.GRANT)
    @PutMapping("/authUser/selectAll")
    public AjaxResult selectAuthUserAll(Long roleId, Long[] userIds)
    {
        roleService.checkRoleDataScope(roleId);
        return toAjax(roleService.insertAuthUsers(roleId, userIds));
    }

    /**
     * 获取对应角色部门树列表
     */
    @RequiresPermissions("system:role:query")
    @GetMapping(value = "/deptTree/{roleId}")
    public AjaxResult deptTree(@PathVariable("roleId") Long roleId)
    {
        AjaxResult ajax = AjaxResult.success();
        ajax.put("checkedKeys", deptService.selectDeptListByRoleId(roleId));
        ajax.put("depts", deptService.selectDeptTreeList(new SysDept()));
        return ajax;
    }

    /**
     * 状态修改
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        role.setUpdateBy(loginUser.getSysUser().getUserName());
        return toAjax(roleService.updateRoleStatus(role));
    }

    /**
     * 修改保存数据权限
     */
    @RequiresPermissions("system:role:edit")
    @Log(title = "角色管理", businessType = BusinessType.UPDATE)
    @PutMapping("/dataScope")
    public AjaxResult dataScope(@RequestBody SysRole role)
    {
        roleService.checkRoleAllowed(role);
        roleService.checkRoleDataScope(role.getRoleId());
        return toAjax(roleService.authDataScope(role));
    }


}
