package com.sme.pm.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Result} generic wrapper class.
 *
 * @author SME PM Team
 * @since 1.0
 */
class ResultTest {

    // ========== Success Method Tests ==========

    /**
     * Tests that success() with no argument returns a Result with code 200 and null data.
     */
    @Test
    void success_withNoArg_shouldReturnSuccessWithNullData() {
        Result<String> result = Result.success();

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertNull(result.getData());
    }

    /**
     * Tests that success(T data) returns a Result with the provided data.
     */
    @Test
    void success_withData_shouldReturnResultWithData() {
        String data = "test data";
        Result<String> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals("success", result.getMessage());
        assertEquals(data, result.getData());
    }

    /**
     * Tests that success() works correctly with Integer data type.
     */
    @Test
    void success_withIntegerData_shouldWorkCorrectly() {
        Integer data = 42;
        Result<Integer> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals(data, result.getData());
    }

    /**
     * Tests that success() works correctly with custom object data type.
     */
    @Test
    void success_withObjectData_shouldWorkCorrectly() {
        TestData data = new TestData("test", 123);
        Result<TestData> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals(data, result.getData());
        assertEquals("test", result.getData().getName());
        assertEquals(123, result.getData().getValue());
    }

    /**
     * Tests that success() works correctly with collections.
     */
    @Test
    void success_withListData_shouldWorkCorrectly() {
        java.util.List<String> data = java.util.Arrays.asList("a", "b", "c");
        Result<java.util.List<String>> result = Result.success(data);

        assertEquals(200, result.getCode());
        assertEquals(3, result.getData().size());
        assertEquals("a", result.getData().get(0));
    }

    /**
     * Tests that success() works correctly with empty string data.
     */
    @Test
    void success_withEmptyStringData_shouldWorkCorrectly() {
        Result<String> result = Result.success("");

        assertEquals(200, result.getCode());
        assertEquals("", result.getData());
    }

    /**
     * Tests that success() works correctly with zero numeric data.
     */
    @Test
    void success_withZeroData_shouldWorkCorrectly() {
        Result<Integer> result = Result.success(0);

        assertEquals(200, result.getCode());
        assertEquals(0, result.getData());
    }

    // ========== Error Method Tests ==========

    /**
     * Tests that error(String message) returns a Result with code 500 and the provided message.
     */
    @Test
    void error_withMessage_shouldReturnErrorWith500Code() {
        String errorMessage = "Something went wrong";
        Result<Void> result = Result.error(errorMessage);

        assertEquals(500, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
    }

    /**
     * Tests that error(String message) works with empty string.
     */
    @Test
    void error_withEmptyMessage_shouldWorkCorrectly() {
        Result<Void> result = Result.error("");

        assertEquals(500, result.getCode());
        assertEquals("", result.getMessage());
    }

    /**
     * Tests that error(int code, String message) returns a Result with the provided code and message.
     */
    @Test
    void error_withCodeAndMessage_shouldReturnErrorWithProvidedCode() {
        int errorCode = 400;
        String errorMessage = "Bad request";
        Result<Void> result = Result.error(errorCode, errorMessage);

        assertEquals(errorCode, result.getCode());
        assertEquals(errorMessage, result.getMessage());
        assertNull(result.getData());
    }

    /**
     * Tests that error(int code, String message) works with various HTTP error codes.
     */
    @Test
    void error_withVariousErrorCodes_shouldWorkCorrectly() {
        // Test 400 Bad Request
        Result<Void> badRequest = Result.error(400, "Bad Request");
        assertEquals(400, badRequest.getCode());

        // Test 401 Unauthorized
        Result<Void> unauthorized = Result.error(401, "Unauthorized");
        assertEquals(401, unauthorized.getCode());

        // Test 403 Forbidden
        Result<Void> forbidden = Result.error(403, "Forbidden");
        assertEquals(403, forbidden.getCode());

        // Test 404 Not Found
        Result<Void> notFound = Result.error(404, "Not Found");
        assertEquals(404, notFound.getCode());

        // Test 500 Internal Server Error
        Result<Void> internalError = Result.error(500, "Internal Server Error");
        assertEquals(500, internalError.getCode());
    }

    /**
     * Tests that error(int code, String message) works with negative error code.
     */
    @Test
    void error_withNegativeCode_shouldWorkCorrectly() {
        Result<Void> result = Result.error(-1, "Custom error");
        assertEquals(-1, result.getCode());
    }

    // ========== Generic Type Tests ==========

    /**
     * Tests that Result works with different generic types.
     */
    @Test
    void result_withDifferentGenericTypes_shouldWorkCorrectly() {
        // String type
        Result<String> stringResult = Result.success("test");
        assertEquals(String.class, stringResult.getData().getClass());

        // Integer type
        Result<Integer> intResult = Result.success(123);
        assertEquals(Integer.class, intResult.getData().getClass());

        // Boolean type
        Result<Boolean> boolResult = Result.success(true);
        assertEquals(Boolean.class, boolResult.getData().getClass());

        // Long type
        Result<Long> longResult = Result.success(999L);
        assertEquals(Long.class, longResult.getData().getClass());
    }

    /**
     * Tests that Result works with null data in success case.
     */
    @Test
    void success_withNullData_shouldWorkCorrectly() {
        Result<Object> result = Result.success((Object) null);
        assertNull(result.getData());
    }

    // ========== Setter/Getter Tests (via Lombok @Data) ==========

    /**
     * Tests that setters and getters work correctly.
     */
    @Test
    void settersAndGetters_shouldWorkCorrectly() {
        Result<String> result = new Result<>();
        result.setCode(404);
        result.setMessage("Not found");
        result.setData("custom data");

        assertEquals(404, result.getCode());
        assertEquals("Not found", result.getMessage());
        assertEquals("custom data", result.getData());
    }

    // ========== Edge Cases ==========

    /**
     * Tests that success with very long string data works.
     */
    @Test
    void success_withLongStringData_shouldWorkCorrectly() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("a");
        }
        String longData = sb.toString();
        Result<String> result = Result.success(longData);

        assertEquals(200, result.getCode());
        assertEquals(longData, result.getData());
        assertEquals(10000, result.getData().length());
    }

    /**
     * Tests that error with very long message works.
     */
    @Test
    void error_withLongMessage_shouldWorkCorrectly() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10000; i++) {
            sb.append("e");
        }
        String longMessage = sb.toString();
        Result<Void> result = Result.error(longMessage);

        assertEquals(500, result.getCode());
        assertEquals(longMessage, result.getMessage());
    }

    /**
     * Tests that Result can be instantiated directly and modified.
     */
    @Test
    void directInstantiation_shouldAllowModification() {
        Result<Integer> result = new Result<>();
        result.setCode(100);
        result.setMessage("Custom");
        result.setData(555);

        assertEquals(100, result.getCode());
        assertEquals("Custom", result.getMessage());
        assertEquals(555, result.getData());
    }

    // ========== Helper Class for Object Tests ==========

    /**
     * Helper class for testing Result with complex object types.
     */
    @lombok.Data
    private static class TestData {
        private String name;
        private int value;

        public TestData(String name, int value) {
            this.name = name;
            this.value = value;
        }
    }
}
