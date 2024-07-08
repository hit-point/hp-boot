package com.hpboot.controller;

import cn.dev33.satoken.stp.StpUtil;
import com.hpboot.common.core.Log;
import com.hpboot.common.core.annotation.RequiresPermissions;
import com.hpboot.common.core.constant.UserConstants;
import com.hpboot.common.core.enums.BusinessType;
import com.hpboot.common.core.utils.SecurityUtils;
import com.hpboot.common.core.utils.StringUtils;
import com.hpboot.common.core.utils.file.FileUploadUtils;
import com.hpboot.common.core.utils.file.MimeTypeUtils;
import com.hpboot.common.core.web.domain.AjaxResult;
import com.hpboot.common.core.web.page.TableDataInfo;
import com.hpboot.config.RuoYiConfig;
import com.hpboot.domain.entity.SysDept;
import com.hpboot.domain.entity.SysRole;
import com.hpboot.domain.entity.SysUser;
import com.hpboot.domain.vo.LoginUser;
import com.hpboot.service.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.hpboot.common.core.web.domain.AjaxResult.success;

@Slf4j
@RestController
@RequestMapping("/system/user")
public class SysUserController extends BaseController {
    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysRoleService roleService;

    @Autowired
    private ISysDeptService deptService;

    @Autowired
    private ISysPostService postService;

    @Autowired
    private ISysPermissionService permissionService;

    /**
     * 获取用户列表
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/list")
    public TableDataInfo list(SysUser user) {
        startPage();
        List<SysUser> sysUsers = userService.selectUserList(user);
//        Page<SysUser> list = userService.selectUserList(user);
        return getDataTable(sysUsers);
    }
//
//    @Log(title = "用户管理", businessType = BusinessType.EXPORT)
//    @RequiresPermissions("system:user:export")
//    @PostMapping("/export")
//    public void export(HttpServletResponse response, SysUser user) {
//        List<SysUser> list = userService.selectUserList(user);
//        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
//        util.exportExcel(response, list, "用户数据");
//    }
//
//    @Log(title = "用户管理", businessType = BusinessType.IMPORT)
//    @RequiresPermissions("system:user:import")
//    @PostMapping("/importData")
//    public AjaxResult importData(MultipartFile file, boolean updateSupport) throws Exception {
//        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
//        List<SysUser> userList = util.importExcel(file.getInputStream());
//        String operName = SecurityUtils.getUsername();
//        String message = userService.importUser(userList, updateSupport, operName);
//        return success(message);
//    }
//
//    @PostMapping("/importTemplate")
//    public void importTemplate(HttpServletResponse response) throws IOException {
//        ExcelUtil<SysUser> util = new ExcelUtil<SysUser>(SysUser.class);
//        util.importTemplateExcel(response, "用户数据");
//    }
//
//    /**
//     * 获取当前用户信息
//     */
//    @InnerAuth
//    @GetMapping("/info/{username}")
//    public R<LoginUser> info(@PathVariable("username") String username) {
//        SysUser sysUser = userService.selectUserByUserName(username);
//        if (StringUtils.isNull(sysUser)) {
//            return R.fail("用户名或密码错误");
//        }
//        // 角色集合
//        Set<String> roles = permissionService.getRolePermission(sysUser);
//        // 权限集合
//        Set<String> permissions = permissionService.getMenuPermission(sysUser);
//        LoginUser sysUserVo = new LoginUser();
//        sysUserVo.setSysUser(sysUser);
//        sysUserVo.setRoles(roles);
//        sysUserVo.setPermissions(permissions);
//        return R.ok(sysUserVo);
//    }
//
//    /**
//     * 注册用户信息
//     */
//
//    @PostMapping("/register")
//    public R<Boolean> register(@RequestBody SysUser sysUser) {
//        String username = sysUser.getUserName();
//        if (!userService.checkUserNameUnique(sysUser)) {
//            return R.fail("保存用户'" + username + "'失败，注册账号已存在");
//        }
//        return R.ok(userService.registerUser(sysUser));
//    }
//
    /**
     * 获取用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/getInfo")
    public AjaxResult getInfo() {
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        SysUser user = userService.selectUserById(loginUser.getSysUser().getUserId());
        user.setAdmin(true);
        // 角色集合
        Set<String> roles = permissionService.getRolePermission(user);
        // 权限集合
        Set<String> permissions = permissionService.getMenuPermission(user);
        AjaxResult ajax = success();
        ajax.put("user", user);
        ajax.put("roles", roles);
        ajax.put("permissions", permissions);
        return ajax;
    }

    /**
     * 根据用户编号获取详细信息
     */
    @RequiresPermissions("system:user:query")
    @GetMapping(value = {"/", "/{userId}"})
    public AjaxResult getInfo(@PathVariable(value = "userId", required = false) Long userId) {
        userService.checkUserDataScope(userId);
        AjaxResult ajax = AjaxResult.success();
        List<SysRole> roles = roleService.selectRoleAll();
//        ajax.put("roles", SysUser.isAdmin(userId) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("posts", postService.selectPostAll());
        if (StringUtils.isNotNull(userId)) {
            SysUser sysUser = userService.selectUserById(userId);
            ajax.put(AjaxResult.DATA_TAG, sysUser);
            ajax.put("postIds", postService.selectPostListByUserId(userId));
            ajax.put("roleIds", sysUser.getRoles().stream().map(SysRole::getRoleId).collect(Collectors.toList()));

            ajax.put("roles", SysUser.isAdmin(sysUser.getUserName()) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));

        }
        return ajax;
    }

//    /**
//     * 新增用户
//     */
//    @RequiresPermissions("system:user:add")
//    @Log(title = "用户管理", businessType = BusinessType.INSERT)
//    @PostMapping
//    public AjaxResult add(@Validated @RequestBody SysUser user) {
//        if (!userService.checkUserNameUnique(user)) {
//            return error("新增用户'" + user.getUserName() + "'失败，登录账号已存在");
//        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
//            return error("新增用户'" + user.getUserName() + "'失败，手机号码已存在");
//        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
//            return error("新增用户'" + user.getUserName() + "'失败，邮箱账号已存在");
//        }
//        user.setCreateBy(SecurityUtils.getUsername());
//        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
//        return toAjax(userService.insertUser(user));
//    }
//
    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PostMapping
    public AjaxResult add(@Validated @RequestBody SysUser user) {
        if (!userService.checkUserNameUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        } else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        } else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user)) {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        user.setUpdateBy(loginUser.getSysUser().getUserName());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        return toAjax(userService.insertUser(user));
    }

    /**
     * 修改用户
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult exit(@Validated @RequestBody SysUser user)
    {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        if (!userService.checkUserNameUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，登录账号已存在");
        }
        else if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        user.setUpdateBy(loginUser.getSysUser().getUserName());
        return toAjax(userService.updateUser(user));
    }

    /**
     * 删除用户
     */
    @RequiresPermissions("system:user:remove")
    @Log(title = "用户管理", businessType = BusinessType.DELETE)
    @DeleteMapping("/{userIds}")
    public AjaxResult remove(@PathVariable String userIds) {
        return toAjax(userService.deleteUserByIds(userIds));
    }

    /**
     * 重置密码
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/resetPwd")
    public AjaxResult resetPwd(@RequestBody SysUser user) {
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        user.setPassword(SecurityUtils.encryptPassword(user.getPassword()));
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        user.setUpdateBy(loginUser.getSysUser().getUserName());
        return toAjax(userService.resetPwd(user));
    }

    /**
     * 状态修改
     */
    @RequiresPermissions("system:user:edit")
    @Log(title = "用户管理", businessType = BusinessType.UPDATE)
    @PutMapping("/changeStatus")
    public AjaxResult changeStatus(@RequestBody SysUser user) {
        //只有admin用户才能修改用户
        userService.checkUserAllowed(user);
        userService.checkUserDataScope(user.getUserId());
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        user.setUpdateBy(loginUser.getSysUser().getUserName());
        return toAjax(userService.changeStatus(user));
    }

    /**
     * 根据用户编号获取授权角色
     */
    @RequiresPermissions("system:user:query")
    @GetMapping("/authRole/{userId}")
    public AjaxResult authRole(@PathVariable("userId") Long userId) {
        AjaxResult ajax = AjaxResult.success();
        SysUser user = userService.selectUserById(userId);
        List<SysRole> roles = roleService.selectRolesByUserId(userId);
        ajax.put("user", user);
//        ajax.put("roles", SysUser.isAdmin(user.getUserName()) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));
        ajax.put("roles", SysUser.isAdmin(user.getUserName()) ? roles : roles.stream().filter(r -> !r.isAdmin()).collect(Collectors.toList()));

        return ajax;
    }

//    /**
//     * 用户授权角色
//     */
//    @RequiresPermissions("system:user:edit")
//    @Log(title = "用户管理", businessType = BusinessType.GRANT)
//    @PutMapping("/authRole")
//    public AjaxResult insertAuthRole(Long userId, Long[] roleIds) {
//        userService.checkUserDataScope(userId);
//        userService.insertUserAuth(userId, roleIds);
//        return success();
//    }
//
    /**
     * 获取部门树列表
     */
    @RequiresPermissions("system:user:list")
    @GetMapping("/deptTree")
    public AjaxResult deptTree(SysDept dept) {
        return success(deptService.selectDeptTreeList(dept));
    }

    /**
     * 修改用户
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/profile")
    public AjaxResult updateProfile(@RequestBody SysUser user)
    {
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        SysUser sysUser = loginUser.getSysUser();
        user.setUserName(sysUser.getUserName());
        if (StringUtils.isNotEmpty(user.getPhonenumber()) && !userService.checkPhoneUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，手机号码已存在");
        }
        else if (StringUtils.isNotEmpty(user.getEmail()) && !userService.checkEmailUnique(user))
        {
            return error("修改用户'" + user.getUserName() + "'失败，邮箱账号已存在");
        }
        user.setUserId(sysUser.getUserId());
        user.setPassword(null);
        user.setAvatar(null);
        user.setDeptId(null);
        if (userService.updateUserProfile(user) > 0)
        {
            // 更新缓存用户信息
            loginUser.getSysUser().setNickName(user.getNickName());
            loginUser.getSysUser().setPhonenumber(user.getPhonenumber());
            loginUser.getSysUser().setEmail(user.getEmail());
            loginUser.getSysUser().setSex(user.getSex());
//            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改个人信息异常，请联系管理员");
    }
    /**
     * 重置密码
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PutMapping("/profile/updatePwd")
    public AjaxResult updatePwd(String oldPassword, String newPassword)
    {
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        String username = loginUser.getSysUser().getUserName();
        SysUser user = userService.selectUserByUserName2(username);
        String password = user.getPassword();
        if (!SecurityUtils.matchesPassword(oldPassword, password))
        {
            return error("修改密码失败，旧密码错误");
        }
        if (SecurityUtils.matchesPassword(newPassword, password))
        {
            return error("新密码不能与旧密码相同");
        }
        if (userService.resetUserPwd(username, SecurityUtils.encryptPassword(newPassword)) > 0)
        {
            // 更新缓存用户密码
            LoginUser loginUser2 = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
            loginUser2.getSysUser().setPassword(SecurityUtils.encryptPassword(newPassword));
//            tokenService.setLoginUser(loginUser);
            return success();
        }
        return error("修改密码异常，请联系管理员");
    }


    /**
     * 保存头像
     */
    @Log(title = "个人信息", businessType = BusinessType.UPDATE)
    @PostMapping("/profile/avatar")
    public AjaxResult updateAvatar(@RequestParam("avatarfile") MultipartFile file)
    {
        SysUser currentUser = getSysUser();
        try
        {
            if (!file.isEmpty())
            {
                String avatar = FileUploadUtils.upload(RuoYiConfig.getAvatarPath(), file, MimeTypeUtils.IMAGE_EXTENSION);
                currentUser.setAvatar(avatar);
                if (userService.updateUserInfo(currentUser) > 0)
                {
//                    setSysUser(userService.selectUserById(currentUser.getUserId()));
                    return success();
                }
            }
            return error();
        }
        catch (Exception e)
        {
            log.error("修改头像失败！", e);
            return error(e.getMessage());
        }
    }

//    /**
//     * 头像上传
//     */
//    @Log(title = "用户头像", businessType = BusinessType.UPDATE)
//    @PostMapping("/avatar")
//    public AjaxResult avatar(@RequestParam("avatarfile") MultipartFile file)
//    {
//        if (!file.isEmpty())
//        {
//            LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
//            String extension = FileTypeUtils.getExtension(file);
//            if (!StringUtils.equalsAnyIgnoreCase(extension, MimeTypeUtils.IMAGE_EXTENSION))
//            {
//                return error("文件格式不正确，请上传" + Arrays.toString(MimeTypeUtils.IMAGE_EXTENSION) + "格式");
//            }
//            R<SysFile> fileResult = remoteFileService.upload(file);
//            if (StringUtils.isNull(fileResult) || StringUtils.isNull(fileResult.getData()))
//            {
//                return error("文件服务异常，请联系管理员");
//            }
//            String url = fileResult.getData().getUrl();
//            if (userService.updateUserAvatar(loginUser.getUsername(), url))
//            {
//                AjaxResult ajax = AjaxResult.success();
//                ajax.put("imgUrl", url);
//                // 更新缓存用户头像
//                loginUser.getSysUser().setAvatar(url);
////                tokenService.setLoginUser(loginUser);
//                return ajax;
//            }
//        }
//        return error("上传图片异常，请联系管理员");
//    }

}
