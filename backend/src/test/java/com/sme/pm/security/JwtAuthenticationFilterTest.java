package com.sme.pm.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for {@link JwtAuthenticationFilter}.
 *
 * @author SME PM Team
 * @since 1.0
 */
@ExtendWith(MockitoExtension.class)
class JwtAuthenticationFilterTest {

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private UserDetailsServiceImpl userDetailsService;

    @Mock
    private FilterChain filterChain;

    private JwtAuthenticationFilter jwtAuthenticationFilter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        jwtAuthenticationFilter = new JwtAuthenticationFilter(tokenProvider, userDetailsService);
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
        SecurityContextHolder.clearContext();
    }

    /**
     * Tests that doFilterInternal proceeds without authentication when no token is provided.
     */
    @Test
    void doFilterInternal_shouldProceedWithoutAuth_whenNoTokenProvided() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Tests that doFilterInternal proceeds without authentication when token is empty.
     */
    @Test
    void doFilterInternal_shouldProceedWithoutAuth_whenTokenIsEmpty() throws ServletException, IOException {
        request.addHeader("Authorization", "");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Tests that doFilterInternal proceeds without authentication when Authorization header is null.
     */
    @Test
    void doFilterInternal_shouldProceedWithoutAuth_whenAuthorizationHeaderIsNull() throws ServletException, IOException {
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Tests that doFilterInternal proceeds without authentication when token does not start with "Bearer ".
     */
    @Test
    void doFilterInternal_shouldProceedWithoutAuth_whenTokenDoesNotStartWithBearer() throws ServletException, IOException {
        request.addHeader("Authorization", "Basic sometoken");

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Tests that doFilterInternal proceeds without authentication when token is invalid.
     */
    @Test
    void doFilterInternal_shouldProceedWithoutAuth_whenTokenIsInvalid() throws ServletException, IOException {
        String invalidToken = "invalid.token.here";
        request.addHeader("Authorization", "Bearer " + invalidToken);
        when(tokenProvider.validateToken(invalidToken)).thenReturn(false);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenProvider).validateToken(invalidToken);
        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    /**
     * Tests that doFilterInternal sets authentication when token is valid.
     */
    @Test
    void doFilterInternal_shouldSetAuthentication_whenTokenIsValid() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        Long userId = 123L;
        request.addHeader("Authorization", "Bearer " + validToken);

        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(tokenProvider).validateToken(validToken);
        verify(tokenProvider).getUserIdFromToken(validToken);
        verify(filterChain).doFilter(request, response);

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication);
        assertEquals(userId, authentication.getPrincipal());
        assertNull(authentication.getCredentials());
        assertEquals(List.of(), authentication.getAuthorities());
    }

    /**
     * Tests that authentication details are correctly set.
     */
    @Test
    void doFilterInternal_shouldSetAuthenticationDetails_whenTokenIsValid() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        Long userId = 1L;
        request.addHeader("Authorization", "Bearer " + validToken);

        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(validToken)).thenReturn(userId);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        UsernamePasswordAuthenticationToken authentication =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertNotNull(authentication.getDetails());
    }

    /**
     * Tests that doFilterInternal proceeds with filter chain even when exception occurs during authentication.
     */
    @Test
    void doFilterInternal_shouldProceedWithFilterChain_whenExceptionOccurs() throws ServletException, IOException {
        String validToken = "valid.jwt.token";
        request.addHeader("Authorization", "Bearer " + validToken);

        when(tokenProvider.validateToken(validToken)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(validToken)).thenThrow(new RuntimeException("Token parsing error"));

        // This should not throw, but should proceed with filter chain
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        verify(filterChain).doFilter(request, response);
        assertNull(SecurityContextHolder.getContext().getAuthentication());
    }

    // ========== getTokenFromRequest Method Tests ==========

    /**
     * Tests that getTokenFromRequest returns token when Authorization header is valid.
     */
    @Test
    void getTokenFromRequest_shouldReturnToken_whenHeaderIsValid() {
        String token = "my.jwt.token";
        request.addHeader("Authorization", "Bearer " + token);

        // Use reflection to test private method
        String result = invokePrivateMethod("getTokenFromRequest", request);

        assertEquals(token, result);
    }

    /**
     * Tests that getTokenFromRequest returns null when Authorization header is missing.
     */
    @Test
    void getTokenFromRequest_shouldReturnNull_whenHeaderIsMissing() {
        String result = invokePrivateMethod("getTokenFromRequest", request);
        assertNull(result);
    }

    /**
     * Tests that getTokenFromRequest returns null when Authorization header is empty.
     */
    @Test
    void getTokenFromRequest_shouldReturnNull_whenHeaderIsEmpty() {
        request.addHeader("Authorization", "");

        String result = invokePrivateMethod("getTokenFromRequest", request);
        assertNull(result);
    }

    /**
     * Tests that getTokenFromRequest returns null when Authorization header does not start with "Bearer ".
     */
    @Test
    void getTokenFromRequest_shouldReturnNull_whenHeaderDoesNotStartWithBearer() {
        request.addHeader("Authorization", "Basic sometoken");

        String result = invokePrivateMethod("getTokenFromRequest", request);
        assertNull(result);
    }

    /**
     * Tests that getTokenFromRequest correctly extracts token with "Bearer " prefix.
     */
    @Test
    void getTokenFromRequest_shouldExtractToken_correctly() {
        String token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjN9.signature";
        request.addHeader("Authorization", "Bearer " + token);

        String result = invokePrivateMethod("getTokenFromRequest", request);
        assertEquals(token, result);
    }

    /**
     * Tests that getTokenFromRequest handles token with no Bearer prefix (whitespace only).
     */
    @Test
    void getTokenFromRequest_shouldReturnNull_whenOnlyWhitespaceBeforeToken() {
        request.addHeader("Authorization", "   some.token.here");

        String result = invokePrivateMethod("getTokenFromRequest", request);
        assertNull(result);
    }

    /**
     * Tests that getTokenFromRequest handles Bearer with no token.
     */
    @Test
    void getTokenFromRequest_shouldReturnEmptyString_whenBearerWithNoToken() {
        request.addHeader("Authorization", "Bearer ");

        String result = invokePrivateMethod("getTokenFromRequest", request);
        assertEquals("", result);
    }

    // ========== SecurityContext Tests ==========

    /**
     * Tests that SecurityContext is preserved when no token is provided.
     */
    @Test
    void securityContext_shouldBePreserved_whenNoTokenProvided() throws ServletException, IOException {
        // Set some authentication in SecurityContext
        UsernamePasswordAuthenticationToken existingAuth =
                new UsernamePasswordAuthenticationToken(999L, null, List.of());
        SecurityContextHolder.getContext().setAuthentication(existingAuth);

        assertNotNull(SecurityContextHolder.getContext().getAuthentication());

        // Process request without token
        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        // SecurityContext should be preserved when no token is provided
        assertNotNull(SecurityContextHolder.getContext().getAuthentication());
        assertEquals(999L, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
    }

    /**
     * Tests that multiple valid requests correctly update SecurityContext.
     */
    @Test
    void multipleRequests_shouldUpdateSecurityContext_correctly() throws ServletException, IOException {
        String token1 = "token1";
        String token2 = "token2";
        Long userId1 = 1L;
        Long userId2 = 2L;

        request.addHeader("Authorization", "Bearer " + token1);
        when(tokenProvider.validateToken(token1)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(token1)).thenReturn(userId1);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        UsernamePasswordAuthenticationToken auth1 =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(userId1, auth1.getPrincipal());

        // Clear and prepare for second request
        SecurityContextHolder.clearContext();

        request = new MockHttpServletRequest();
        request.addHeader("Authorization", "Bearer " + token2);
        when(tokenProvider.validateToken(token2)).thenReturn(true);
        when(tokenProvider.getUserIdFromToken(token2)).thenReturn(userId2);

        jwtAuthenticationFilter.doFilterInternal(request, response, filterChain);

        UsernamePasswordAuthenticationToken auth2 =
                (UsernamePasswordAuthenticationToken) SecurityContextHolder.getContext().getAuthentication();
        assertEquals(userId2, auth2.getPrincipal());
    }

    // ========== Helper Methods ==========

    /**
     * Invokes a private method using reflection for testing purposes.
     */
    private String invokePrivateMethod(String methodName, HttpServletRequest request) {
        try {
            java.lang.reflect.Method method = JwtAuthenticationFilter.class.getDeclaredMethod(methodName, HttpServletRequest.class);
            method.setAccessible(true);
            return (String) method.invoke(jwtAuthenticationFilter, request);
        } catch (Exception e) {
            throw new RuntimeException("Failed to invoke private method: " + methodName, e);
        }
    }
}
