package com.sme.pm.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider tokenProvider;
    private static final String SECRET = "this-is-a-very-long-secret-key-for-testing-purposes-at-least-256-bits";
    private static final long EXPIRATION = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        tokenProvider = new JwtTokenProvider(SECRET, EXPIRATION);
    }

    @Test
    void generateToken_shouldCreateValidToken() {
        Long userId = 1L;
        String username = "testuser";

        String token = tokenProvider.generateToken(userId, username);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    void getUserIdFromToken_shouldReturnCorrectUserId() {
        Long userId = 123L;
        String username = "testuser";

        String token = tokenProvider.generateToken(userId, username);
        Long extractedUserId = tokenProvider.getUserIdFromToken(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void validateToken_shouldReturnTrue_forValidToken() {
        Long userId = 1L;
        String username = "testuser";

        String token = tokenProvider.generateToken(userId, username);
        boolean isValid = tokenProvider.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forInvalidToken() {
        String invalidToken = "invalid.token.here";

        boolean isValid = tokenProvider.validateToken(invalidToken);

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forEmptyToken() {
        boolean isValid = tokenProvider.validateToken("");

        assertFalse(isValid);
    }

    @Test
    void validateToken_shouldReturnFalse_forNullToken() {
        boolean isValid = tokenProvider.validateToken(null);

        assertFalse(isValid);
    }

    @Test
    void getUserIdFromToken_shouldThrow_forTamperedToken() {
        String tamperedToken = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiIxMjMifQ.invalidSignature";

        assertThrows(Exception.class, () -> tokenProvider.getUserIdFromToken(tamperedToken));
    }

    @Test
    void generateToken_shouldCreateDifferentTokens_forDifferentUsers() {
        String token1 = tokenProvider.generateToken(1L, "user1");
        String token2 = tokenProvider.generateToken(2L, "user2");

        assertNotEquals(token1, token2);
    }

    @Test
    void multipleGenerations_shouldCreateUniqueTokens() {
        String token1 = tokenProvider.generateToken(1L, "user");
        // Small delay to ensure different timestamp
        String token2 = tokenProvider.generateToken(1L, "user");

        // Tokens should be different due to issuedAt timestamp
        assertNotEquals(token1, token2);
    }
}
