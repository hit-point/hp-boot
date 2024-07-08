package com.hpboot.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.router.SaRouter;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class InterceptorWebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //鉴权校验拦截
        registry.addInterceptor(new SaInterceptor(handle -> {
            SaRouter
                    // 拦截的 path 列表，可以写多个 */
                    .match("/**")
                    .notMatch("/login", "/register", "/code")
                    //knife4j接口文档使用 放行
                    .notMatch("/*.html")
                    .notMatch("/swagger-resources")
                    .notMatch("/webjars/**")
                    .notMatch("/**/api-docs")
                    //检查登录情况
                    .check(r -> StpUtil.checkLogin())
                    //检查token是否过期
                    .check(r -> StpUtil.checkActiveTimeout());

            // 根据路由划分模块，不同模块不同鉴权
//            SaRouter.match("/user/**", r -> StpUtil.checkPermission("user"));
//            SaRouter.match("/admin/**", r -> StpUtil.checkPermission("admin"));
        })).addPathPatterns("/**");
    }
}
