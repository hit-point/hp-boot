package com.hpboot.service.impl;

import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ObjectUtil;
import com.hpboot.common.core.constant.CacheConstants;
import com.hpboot.common.core.constant.UserConstants;
import com.hpboot.common.core.enums.UserStatus;
import com.hpboot.common.core.exception.ServiceException;
import com.hpboot.common.core.utils.SecurityUtils;
import com.hpboot.common.core.utils.StringUtils;
import com.hpboot.common.core.utils.ip.IpUtils;
import com.hpboot.common.core.web.domain.AjaxResult;
import com.hpboot.domain.entity.SysUser;
import com.hpboot.domain.vo.LoginBody;
import com.hpboot.domain.vo.LoginUser;
import com.hpboot.service.ISysUserService;
import com.hpboot.service.ValidateCodeService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 登录校验方法
 */
@Slf4j
@Component
public class SysLoginService {
    @Autowired
    private ISysUserService ISysUserService;

    @Autowired
    private RedisService redisService;

    @Autowired
    private ValidateCodeService validateCodeService;

    public AjaxResult getImageCode() {
        AjaxResult ajaxResult = new AjaxResult();
        try {
            ajaxResult = validateCodeService.createCaptcha();
        } catch (IOException e) {
            log.error("生成验证码异常", e);
        }
        return ajaxResult;
    }

    /**
     * 登录
     */
    public LoginUser login(LoginBody loginBody) {
        String username = loginBody.getUsername();
        String password = loginBody.getPassword();

        String code = loginBody.getCode();
        String uuid = loginBody.getUuid();
        //校验验证码
        validateCodeService.checkCaptcha(code, uuid);

        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ServiceException("用户/密码必须填写");
        }
        // 密码如果不在指定范围内 错误
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new ServiceException("用户密码不在指定范围");
        }
        // 用户名不在指定范围内 错误
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new ServiceException("用户名不在指定范围");
        }
        // IP黑名单校验
        String blackStr = Convert.toStr(redisService.getCacheObject(CacheConstants.SYS_LOGIN_BLACKIPLIST));
        if (IpUtils.isMatchedIp(blackStr, IpUtils.getIpAddr())) {
            throw new ServiceException("很遗憾，访问IP已被列入系统黑名单");
        }
        // 查询用户信息
        LoginUser loginUser = ISysUserService.selectUserByUserName(username);

        if (StringUtils.isNull(loginUser) || StringUtils.isNull(loginUser.getSysUser())) {
            throw new ServiceException("登录用户：" + username + " 不存在");
        }

        SysUser user = loginUser.getSysUser();
        if (UserStatus.DELETED.getCode().equals(user.getDelFlag())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已被删除");
        }
        if (UserStatus.DISABLE.getCode().equals(user.getStatus())) {
            throw new ServiceException("对不起，您的账号：" + username + " 已停用");
        }
        //校验密码
        boolean isLogin = SecurityUtils.matchesPassword(password, user.getPassword());
        if (!isLogin) {
            throw new ServiceException("用户名或密码错误");
        }

        loginUser = getUserToken(loginUser);
        return loginUser;
    }

    //封装用户token
    private LoginUser getUserToken(LoginUser loginUser) {
        //此处校验通过,登录信息存入redis
        //默认三天过期
        long timeOut = 24 * 60 * 60 * 3;
        StpUtil.login(loginUser.getSysUser().getUserName(), timeOut);
        //用户信息存入redis
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        loginUser.setAccessToken(tokenInfo.tokenValue);

        StpUtil.getSession().set(UserConstants.USER_DETAIL, loginUser);

        return loginUser;
    }

    /**
     * 注册
     */
    public int register(String username, String password) {
        // 用户名或密码为空 错误
        if (StringUtils.isEmpty(username) || StringUtils.isEmpty(password)) {
            throw new ServiceException("用户/密码必须填写");
        }
        if (username.length() < UserConstants.USERNAME_MIN_LENGTH
                || username.length() > UserConstants.USERNAME_MAX_LENGTH) {
            throw new ServiceException("账户长度必须在2到20个字符之间");
        }
        if (password.length() < UserConstants.PASSWORD_MIN_LENGTH
                || password.length() > UserConstants.PASSWORD_MAX_LENGTH) {
            throw new ServiceException("密码长度必须在5到20个字符之间");
        }

        LoginUser loginUser = ISysUserService.selectUserByUserName(username);
        if (ObjectUtil.isNotEmpty(loginUser) || ObjectUtil.isNotEmpty(loginUser.getSysUser())) {
            throw new ServiceException("用户已经注册过，无需再次注册");
        }

        // 注册用户信息
        SysUser sysUser = new SysUser();
        sysUser.setUserName(username);
        sysUser.setPassword(SecurityUtils.encryptPassword(password));
        return ISysUserService.insertUser(sysUser);
    }
}
