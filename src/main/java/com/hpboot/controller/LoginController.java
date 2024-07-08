package com.hpboot.controller;

import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.util.ObjectUtil;
import com.hpboot.common.core.constant.UserConstants;
import com.hpboot.common.core.domain.R;
import com.hpboot.common.core.utils.JSONConvertUtil;
import com.hpboot.common.core.web.domain.AjaxResult;
import com.hpboot.domain.vo.LoginBody;
import com.hpboot.domain.vo.LoginUser;
import com.hpboot.domain.vo.RegisterBody;
import com.hpboot.service.impl.SysLoginService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * token 控制
 */
@Api("登录控制器")
@Slf4j
@RestController
public class LoginController {
    @Autowired
    private SysLoginService sysLoginService;

    @ApiOperation("获取验证码")
    @GetMapping("/code")
    public AjaxResult code() {
        return sysLoginService.getImageCode();
    }

    @ApiOperation("登录")
    @PostMapping("/login")
    public R<?> login(@RequestBody LoginBody loginBody) {
        if (log.isInfoEnabled()) {
            log.info("用户登录信息：{}", JSONConvertUtil.toJson(loginBody));
        }
        LoginUser userInfo = sysLoginService.login(loginBody);
        Map<String,Object> outMap = new HashMap<>(8);
        outMap.put("access_token",userInfo.getAccessToken());
        outMap.put("expires_in",720);
        return R.ok(outMap);
    }

    @ApiOperation("登出")
    @DeleteMapping("/logout")
    public R<String> logout() {
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        if (!ObjectUtil.isEmpty(loginUser)) {
            log.info("用户：{},退出成功", loginUser.getUsername());
        }
        StpUtil.logout();
        return R.ok("用户退出成功");
    }

    @ApiOperation("注册")
    @PostMapping("/register")
    public R<?> register(@RequestBody RegisterBody registerBody) {
        if (log.isInfoEnabled()) {
            log.info("用户注册信息：{}", JSONConvertUtil.toJson(registerBody));
        }
        // 用户注册
        //todo 后续需要加验证码注册功能
        int isRegister = sysLoginService.register(registerBody.getUsername(), registerBody.getPassword());
        if (isRegister > 0) {
            return R.ok("用户注册成功");
        }
        return R.fail("用户注册失败");
    }

    @GetMapping("/isLogin")
    public R<?> register() {
        // 判断用户是否登录
        boolean isLogin = StpUtil.isLogin();
        if (isLogin) {
            return R.ok("用户已登录");
        }
        return R.fail("用户未登录");
    }
}
