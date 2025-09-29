package com.onlinebanking.uiservice.config;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new AuthenticationInterceptor())
                .addPathPatterns("/dashboard", "/accounts", "/transactions")
                .excludePathPatterns("/", "/login", "/register", "/css/**", "/js/**", "/images/**");
    }

    public static class AuthenticationInterceptor implements HandlerInterceptor {
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
            Boolean authenticated = (Boolean) request.getSession().getAttribute("authenticated");
            if (authenticated == null || !authenticated) {
                response.sendRedirect("/login");
                return false;
            }
            return true;
        }
    }
}