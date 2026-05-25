package com.sme.pm.interceptor;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.service.IRolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Interceptor to check project role permissions.
 * Validates that the current user has the required role in the project.
 */
public class ProjectRoleInterceptor implements HandlerInterceptor {

    private final IRolePermissionService rolePermissionService;

    public ProjectRoleInterceptor(IRolePermissionService rolePermissionService) {
        this.rolePermissionService = rolePermissionService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (!(handler instanceof HandlerMethod)) {
            return true;
        }

        HandlerMethod handlerMethod = (HandlerMethod) handler;

        // Check class-level annotation
        RequireProjectRole classAnnotation = AnnotationUtils.findAnnotation(
                handlerMethod.getBeanType(), RequireProjectRole.class);

        // Check method-level annotation
        RequireProjectRole methodAnnotation = AnnotationUtils.findAnnotation(
                handlerMethod.getMethod(), RequireProjectRole.class);

        RequireProjectRole annotation = methodAnnotation != null ? methodAnnotation : classAnnotation;

        if (annotation == null) {
            return true; // No role required
        }

        // Get current user ID from SecurityContext (set by JWT filter)
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        Long userId = null;
        if (authentication != null && authentication.getPrincipal() != null) {
            Object principal = authentication.getPrincipal();
            if (principal instanceof Long) {
                userId = (Long) principal;
            } else if (principal instanceof Integer) {
                userId = ((Integer) principal).longValue();
            } else if (principal instanceof String) {
                try {
                    userId = Long.parseLong((String) principal);
                } catch (NumberFormatException ignored) {}
            }
        }

        if (userId == null) {
            sendError(response, HttpStatus.UNAUTHORIZED, "Unauthorized");
            return false;
        }

        // Get project ID from request
        String projectId = extractProjectId(request);
        if (projectId == null) {
            sendError(response, HttpStatus.BAD_REQUEST, "Project ID is required");
            return false;
        }

        // Check if user is a project member
        String userRole = rolePermissionService.getProjectRole(userId, projectId);
        if ("NONE".equals(userRole)) {
            sendError(response, HttpStatus.FORBIDDEN, "You are not a member of this project");
            return false;
        }

        // If memberOnly is true, any project membership is sufficient
        if (annotation.memberOnly()) {
            return true;
        }

        // Check if user has any of the required roles
        String[] requiredRoles = annotation.value();
        if (requiredRoles == null || requiredRoles.length == 0) {
            return true; // No specific role required
        }

        boolean hasRole = false;
        for (String requiredRole : requiredRoles) {
            if (requiredRole.equals(userRole)) {
                hasRole = true;
                break;
            }
        }

        if (!hasRole) {
            sendError(response, HttpStatus.FORBIDDEN, "Insufficient permissions for this operation");
            return false;
        }

        return true;
    }

    private String extractProjectId(HttpServletRequest request) {
        // Try query parameter first: ?projectId=xxx
        String projectId = request.getParameter("projectId");
        if (projectId != null && !projectId.isEmpty()) {
            return projectId;
        }

        // Try path: /api/v1/projects/{projectId}/...
        String pathInfo = request.getRequestURI();
        String[] segments = pathInfo.split("/");

        for (int i = 0; i < segments.length; i++) {
            if ("projects".equals(segments[i]) && i + 1 < segments.length) {
                return segments[i + 1];
            }
        }

        return null;
    }

    private void sendError(HttpServletResponse response, HttpStatus status, String message) throws Exception {
        response.setStatus(status.value());
        response.setContentType("application/json");
        response.getWriter().write("{\"code\":" + status.value() + ",\"message\":\"" + message + "\"}");
    }
}
