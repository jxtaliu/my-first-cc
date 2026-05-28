package com.sme.pm.interceptor;

import com.sme.pm.annotation.RequireProjectRole;
import com.sme.pm.service.IRolePermissionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.method.HandlerMethod;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProjectRoleInterceptorTest {

    @Mock
    private IRolePermissionService rolePermissionService;

    @Mock
    private HttpServletRequest request;

    private HttpServletResponse response;
    private StringWriter responseWriter;

    private ProjectRoleInterceptor interceptor;

    @BeforeEach
    void setUp() {
        interceptor = new ProjectRoleInterceptor(rolePermissionService);
        response = mock(HttpServletResponse.class);
        SecurityContextHolder.clearContext();
    }

    private void setUpSecurityContext(Long userId) {
        UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(userId, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    private HttpServletResponse createResponseWithWriter() throws Exception {
        responseWriter = new StringWriter();
        when(response.getWriter()).thenReturn(new PrintWriter(responseWriter));
        return response;
    }

    @Test
    void preHandle_shouldReturnTrue_whenNoAnnotation() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethod(null);

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verify(response, never()).setStatus(anyInt());
    }

    @Test
    void preHandle_shouldReturnFalse_whenUserIdNotInRequest() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethod("memberOnly");
        // SecurityContext is cleared in setUp, so userId will be null
        createResponseWithWriter();

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.UNAUTHORIZED.value());
    }

    @Test
    void preHandle_shouldReturnFalse_whenProjectIdNotFound() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethod("memberOnly");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/tasks");
        createResponseWithWriter();

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.BAD_REQUEST.value());
    }

    @Test
    void preHandle_shouldReturnFalse_whenUserNotMember() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethod("memberOnly");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn("PRJ_001");
        when(rolePermissionService.getProjectRole(1L, "PRJ_001")).thenReturn("NONE");
        createResponseWithWriter();

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void preHandle_shouldReturnTrue_whenMemberOnlyAndUserIsMember() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethod("memberOnly");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn("PRJ_001");
        when(rolePermissionService.getProjectRole(1L, "PRJ_001")).thenReturn("DEVELOPER");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
    }

    @Test
    void preHandle_shouldReturnTrue_whenUserHasRequiredRole() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethodWithRoles("PROJECT_OWNER");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn("PRJ_001");
        when(rolePermissionService.getProjectRole(1L, "PRJ_001")).thenReturn("PROJECT_OWNER");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
    }

    @Test
    void preHandle_shouldReturnFalse_whenUserLacksRequiredRole() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethodWithRoles("PROJECT_OWNER");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn("PRJ_001");
        when(rolePermissionService.getProjectRole(1L, "PRJ_001")).thenReturn("DEVELOPER");
        createResponseWithWriter();

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertFalse(result);
        verify(response).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void preHandle_shouldReturnTrue_whenUserHasAnyOfRequiredRoles() throws Exception {
        HttpServletResponse respWithWriter = mock(HttpServletResponse.class);
        StringWriter sw = new StringWriter();
        lenient().when(respWithWriter.getWriter()).thenReturn(new PrintWriter(sw));

        HandlerMethod handlerMethod = mockHandlerMethodWithRoles("PROJECT_OWNER", "PROJECT_MANAGER");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn("PRJ_001");
        when(rolePermissionService.getProjectRole(1L, "PRJ_001")).thenReturn("PROJECT_MANAGER");

        boolean result = interceptor.preHandle(request, respWithWriter, handlerMethod);

        assertTrue(result);
    }

    @Test
    void preHandle_shouldExtractProjectIdFromPath() throws Exception {
        HandlerMethod handlerMethod = mockHandlerMethod("memberOnly");
        setUpSecurityContext(1L);
        when(request.getParameter("projectId")).thenReturn(null);
        when(request.getRequestURI()).thenReturn("/api/v1/projects/PRJ_001/tasks");
        when(rolePermissionService.getProjectRole(1L, "PRJ_001")).thenReturn("DEVELOPER");

        boolean result = interceptor.preHandle(request, response, handlerMethod);

        assertTrue(result);
        verify(rolePermissionService).getProjectRole(1L, "PRJ_001");
    }

    private HandlerMethod mockHandlerMethod(String memberOnly) throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method;
        if (memberOnly != null) {
            method = TestController.class.getMethod("memberOnlyEndpoint");
        } else {
            method = TestController.class.getMethod("noAnnotationEndpoint");
        }
        return new HandlerMethod(controller, method);
    }

    private HandlerMethod mockHandlerMethodWithRoles(String... roles) throws NoSuchMethodException {
        TestController controller = new TestController();
        Method method;
        if (roles.length > 1) {
            method = TestController.class.getMethod("requireMultipleRolesEndpoint");
        } else {
            method = TestController.class.getMethod("requireRoleEndpoint");
        }
        return new HandlerMethod(controller, method);
    }

    static class TestController {
        public void noAnnotationEndpoint() {}

        @RequireProjectRole(memberOnly = true)
        public void memberOnlyEndpoint() {}

        @RequireProjectRole(value = {"PROJECT_OWNER"})
        public void requireRoleEndpoint() {}

        @RequireProjectRole(value = {"PROJECT_OWNER", "PROJECT_MANAGER"})
        public void requireMultipleRolesEndpoint() {}
    }
}
