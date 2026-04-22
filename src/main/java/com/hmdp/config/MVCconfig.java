package com.hmdp.config;

import com.hmdp.utils.LoginInterceptor;
import com.hmdp.utils.ReflashInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class MVCconfig implements WebMvcConfigurer {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new LoginInterceptor()).excludePathPatterns(
                "/user/code",
                "/user/login",
                "/shop/**",
                "/blog/hot",
                "/shop-type/**",
                "/voucher/**",
                "/upload/**"
        ).order(1);
        registry.addInterceptor(new ReflashInterceptor(stringRedisTemplate)).addPathPatterns("/**").order(0);
    }
}
