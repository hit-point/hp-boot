package com.hpboot.domain.vo;

import lombok.Data;

/**
 * 用户登录对象
 */
@Data
public class LoginBody {
    /**
     * 用户名
     */
    private String username;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 验证码
     */
    private String code;
    /**
     * 验证码uuid
     */
    private String uuid;
}
