package org.openapitools.client;

import org.junit.jupiter.api.Test;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for the generated {@link ApiClient}.
 * Covers base path configuration, debugging toggle, parameter serialization,
 * header selection, and string escaping.
 */
class ApiClientTest {

    @Test
    void testApiClient() {
        ApiClient apiClient = new ApiClient();
        assertTrue(apiClient.getBasePath().equals("https://api.ing.com") ||
                apiClient.getBasePath().equals("https://api.sandbox.ing.com")
        );
    }

    @Test
    void testSetAndGetBasePath() {
        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath("https://custom.api.example.com");
        assertEquals("https://custom.api.example.com", apiClient.getBasePath());
    }

    @Test
    void testSetDebugging() {
        ApiClient apiClient = new ApiClient();
        // Should not throw
        apiClient.setDebugging(true);
        apiClient.setDebugging(false);
    }

    @Test
    void testParameterToStringWithNull() {
        ApiClient apiClient = new ApiClient();
        assertEquals("", apiClient.parameterToString(null));
    }

    @Test
    void testParameterToStringWithString() {
        ApiClient apiClient = new ApiClient();
        assertEquals("hello", apiClient.parameterToString("hello"));
    }

    @Test
    void testParameterToStringWithList() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.parameterToString(Arrays.asList("a", "b", "c"));
        assertTrue(result.contains("a"));
        assertTrue(result.contains("b"));
        assertTrue(result.contains("c"));
    }

    @Test
    void testSelectHeaderAcceptWithJsonAvailable() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.selectHeaderAccept(new String[]{"application/json", "text/plain"});
        assertNotNull(result);
        assertTrue(result.contains("application/json"));
    }

    @Test
    void testSelectHeaderAcceptWithEmptyArray() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.selectHeaderAccept(new String[]{});
        assertNull(result);
    }

    @Test
    void testSelectHeaderContentTypeWithJsonAvailable() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.selectHeaderContentType(new String[]{"application/json", "text/plain"});
        assertNotNull(result);
        assertTrue(result.contains("application/json"));
    }

    @Test
    void testSelectHeaderContentTypeWithEmptyArray() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.selectHeaderContentType(new String[]{});
        assertEquals("application/json", result);
    }

    @Test
    void testEscapeString() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.escapeString("hello world");
        assertNotNull(result);
        // URL-encoded space
        assertFalse(result.contains(" "));
    }

    @Test
    void testEscapeStringWithSpecialChars() {
        ApiClient apiClient = new ApiClient();
        String result = apiClient.escapeString("test/path?query=value&other=123");
        assertNotNull(result);
    }
}
