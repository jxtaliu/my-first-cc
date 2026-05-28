package com.sme.pm.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link ResultCode} enum.
 *
 * @author SME PM Team
 * @since 1.0
 */
class ResultCodeTest {

    /**
     * Tests that ResultCode enum contains the expected number of entries.
     */
    @Test
    void resultCode_shouldHaveCorrectNumberOfEntries() {
        ResultCode[] codes = ResultCode.values();
        assertEquals(6, codes.length, "ResultCode should have 6 entries");
    }

    /**
     * Tests that SUCCESS code has correct HTTP status code and message.
     */
    @Test
    void success_shouldHaveCorrectCodeAndMessage() {
        assertEquals(200, ResultCode.SUCCESS.getCode());
        assertEquals("success", ResultCode.SUCCESS.getMessage());
    }

    /**
     * Tests that PARAM_ERROR code has correct HTTP status code and message.
     */
    @Test
    void paramError_shouldHaveCorrectCodeAndMessage() {
        assertEquals(400, ResultCode.PARAM_ERROR.getCode());
        assertEquals("Parameter error", ResultCode.PARAM_ERROR.getMessage());
    }

    /**
     * Tests that UNAUTHORIZED code has correct HTTP status code and message.
     */
    @Test
    void unauthorized_shouldHaveCorrectCodeAndMessage() {
        assertEquals(401, ResultCode.UNAUTHORIZED.getCode());
        assertEquals("Unauthorized", ResultCode.UNAUTHORIZED.getMessage());
    }

    /**
     * Tests that FORBIDDEN code has correct HTTP status code and message.
     */
    @Test
    void forbidden_shouldHaveCorrectCodeAndMessage() {
        assertEquals(403, ResultCode.FORBIDDEN.getCode());
        assertEquals("Forbidden", ResultCode.FORBIDDEN.getMessage());
    }

    /**
     * Tests that NOT_FOUND code has correct HTTP status code and message.
     */
    @Test
    void notFound_shouldHaveCorrectCodeAndMessage() {
        assertEquals(404, ResultCode.NOT_FOUND.getCode());
        assertEquals("Not found", ResultCode.NOT_FOUND.getMessage());
    }

    /**
     * Tests that INTERNAL_ERROR code has correct HTTP status code and message.
     */
    @Test
    void internalError_shouldHaveCorrectCodeAndMessage() {
        assertEquals(500, ResultCode.INTERNAL_ERROR.getCode());
        assertEquals("Internal server error", ResultCode.INTERNAL_ERROR.getMessage());
    }

    /**
     * Tests that getCode() returns the correct integer value for each enum entry.
     */
    @Test
    void getCode_shouldReturnCorrectValue() {
        for (ResultCode code : ResultCode.values()) {
            assertTrue(code.getCode() > 0, "Code should be positive for " + code.name());
            assertTrue(code.getCode() < 600, "Code should be valid HTTP status for " + code.name());
        }
    }

    /**
     * Tests that getMessage() returns a non-null, non-empty message for each enum entry.
     */
    @Test
    void getMessage_shouldReturnNonEmptyMessage() {
        for (ResultCode code : ResultCode.values()) {
            assertNotNull(code.getMessage(), "Message should not be null for " + code.name());
            assertFalse(code.getMessage().isEmpty(), "Message should not be empty for " + code.name());
        }
    }

    /**
     * Tests that valueOf() correctly retrieves enum entries by name.
     */
    @Test
    void valueOf_shouldReturnCorrectEnumEntry() {
        assertEquals(ResultCode.SUCCESS, ResultCode.valueOf("SUCCESS"));
        assertEquals(ResultCode.PARAM_ERROR, ResultCode.valueOf("PARAM_ERROR"));
        assertEquals(ResultCode.UNAUTHORIZED, ResultCode.valueOf("UNAUTHORIZED"));
        assertEquals(ResultCode.FORBIDDEN, ResultCode.valueOf("FORBIDDEN"));
        assertEquals(ResultCode.NOT_FOUND, ResultCode.valueOf("NOT_FOUND"));
        assertEquals(ResultCode.INTERNAL_ERROR, ResultCode.valueOf("INTERNAL_ERROR"));
    }

    /**
     * Tests that invalid enum name throws IllegalArgumentException.
     */
    @Test
    void valueOf_shouldThrowExceptionForInvalidName() {
        assertThrows(IllegalArgumentException.class, () -> ResultCode.valueOf("INVALID_CODE"));
    }

    /**
     * Tests that each ResultCode entry has a unique code value.
     */
    @Test
    void allCodeValues_shouldBeUnique() {
        long uniqueCount = java.util.Arrays.stream(ResultCode.values())
                .mapToLong(ResultCode::getCode)
                .distinct()
                .count();
        assertEquals(ResultCode.values().length, uniqueCount, "All ResultCode codes should be unique");
    }
}
