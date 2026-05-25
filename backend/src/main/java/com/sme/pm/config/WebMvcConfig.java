package com.sme.pm.config;

import com.sme.pm.interceptor.ProjectRoleInterceptor;
import com.sme.pm.service.IRolePermissionService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final CurrentUserArgumentResolver currentUserArgumentResolver;
    private final IRolePermissionService rolePermissionService;

    public WebMvcConfig(CurrentUserArgumentResolver currentUserArgumentResolver,
                       IRolePermissionService rolePermissionService) {
        this.currentUserArgumentResolver = currentUserArgumentResolver;
        this.rolePermissionService = rolePermissionService;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(currentUserArgumentResolver);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new ProjectRoleInterceptor(rolePermissionService))
                .addPathPatterns("/api/v1/tasks/**", "/api/v1/projects/**")
                .excludePathPatterns("/api/v1/tasks/bugs/statuses/**", "/api/v1/tasks/bugs/transitions/**");
    }
}
