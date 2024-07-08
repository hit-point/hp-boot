package com.hpboot.common.core.utils;


import cn.dev33.satoken.secure.BCrypt;
import cn.dev33.satoken.stp.SaTokenInfo;
import cn.dev33.satoken.stp.StpUtil;
import com.hpboot.common.core.constant.UserConstants;
import com.hpboot.domain.vo.LoginUser;

import java.util.Optional;

/**
 * 权限获取工具类
 *
 * 
 */
public class SecurityUtils {
    /**
     * 获取用户ID
     */
    public static Long getUserId() {
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        return loginUser.getUserid();
    }

    /**
     * 获取用户名称
     */
    public static String getUsername() {
        LoginUser loginUser = (LoginUser) StpUtil.getSession().get(UserConstants.USER_DETAIL);
        Optional.of(loginUser).orElse(null);
        return loginUser.getUsername();
    }

    /**
     * 获取请求token
     */
    public static String getToken() {
        SaTokenInfo tokenInfo = StpUtil.getTokenInfo();
        return tokenInfo.tokenValue;
    }

    /**
     * 根据request获取请求token
     */
//    public static String getToken(HttpServletRequest request) {
//        // 从header获取token标识
//        String token = request.getHeader(TokenConstants.AUTHENTICATION);
//        return replaceTokenPrefix(token);
//    }

    /**
     * 裁剪token前缀
     */
//    public static String replaceTokenPrefix(String token) {
//        // 如果前端设置了令牌前缀，则裁剪掉前缀
//        if (StringUtils.isNotEmpty(token) && token.startsWith(TokenConstants.PREFIX)) {
//            token = token.replaceFirst(TokenConstants.PREFIX, "");
//        }
//        return token;
//    }

    /**
     * 是否为管理员
     *
     * @param roleKey 角色key
     * @return 结果
     */
    public static boolean isAdmin(String roleKey) {
        return StringUtils.isEmpty(roleKey) && "admin".equals(roleKey);
    }

    /**
     * 生成BCryptPasswordEncoder密码
     *
     * @param password 密码
     * @return 加密字符串
     */
    public static String encryptPassword(String password) {
        String hashpw = BCrypt.hashpw(password);
        return hashpw;
    }

    public static void main(String[] args) {
        String aa = "admin123";
        String s = encryptPassword(aa);
        System.out.println(s);
    }

    /**
     * 判断密码是否相同
     *
     * @param rawPassword     真实密码
     * @param encodedPassword 加密后字符
     * @return 结果
     */
    public static boolean matchesPassword(String rawPassword, String encodedPassword) {
        return BCrypt.checkpw(rawPassword, encodedPassword);
    }
}
