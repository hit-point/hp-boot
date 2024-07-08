package com.hpboot.service;

import com.hpboot.common.core.exception.CaptchaException;
import com.hpboot.common.core.web.domain.AjaxResult;

import java.io.IOException;

/**
 * 验证码获取
 */
public interface ValidateCodeService {
    /**
     * 生成验证码
     */
    AjaxResult createCaptcha() throws IOException, CaptchaException;

    /**
     * 校验验证码
     */
    void checkCaptcha(String key, String value) throws CaptchaException;
}
