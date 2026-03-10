package com.ing.developer.common;

import com.ing.developer.common.exceptions.http.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Utils}.
 * Covers the Pair inner class, withCentsDelimiter, getTimeStamp,
 * and throwHttpExceptionBasedOnStatusCode methods.
 */
class UtilsTest {

    // --- Pair tests ---

    @Test
    void pairGetFirstReturnsFirstValue() {
        Utils.Pair<String, Integer> pair = new Utils.Pair<>("hello", 42);
        assertEquals("hello", pair.getFirst());
    }

    @Test
    void pairGetSecondReturnsSecondValue() {
        Utils.Pair<String, Integer> pair = new Utils.Pair<>("hello", 42);
        assertEquals(42, pair.getSecond());
    }

    @Test
    void pairHandlesNullValues() {
        Utils.Pair<String, String> pair = new Utils.Pair<>(null, null);
        assertNull(pair.getFirst());
        assertNull(pair.getSecond());
    }

    @Test
    void pairHandlesMixedNullValues() {
        Utils.Pair<String, Integer> pair = new Utils.Pair<>("value", null);
        assertEquals("value", pair.getFirst());
        assertNull(pair.getSecond());
    }

    @Test
    void pairWithDifferentGenericTypes() {
        Utils.Pair<Object, Object> pair = new Utils.Pair<>(123L, 45.6);
        assertEquals(123L, pair.getFirst());
        assertEquals(45.6, pair.getSecond());
    }

    // --- withCentsDelimiter tests ---

    @Test
    void withCentsDelimiterStandardValue() {
        assertEquals("1.00", Utils.withCentsDelimiter(100L));
    }

    @Test
    void withCentsDelimiterSingleCent() {
        // Values < 100 cause StringIndexOutOfBoundsException due to implementation
        // which uses substring(0, length-2). This documents the current behavior.
        assertThrows(StringIndexOutOfBoundsException.class,
                () -> Utils.withCentsDelimiter(1L));
    }

    @Test
    void withCentsDelimiterLargeValue() {
        assertEquals("123456.78", Utils.withCentsDelimiter(12345678L));
    }

    @Test
    void withCentsDelimiterTenCents() {
        // Values with exactly 2 digits produce empty prefix: ".10"
        assertEquals(".10", Utils.withCentsDelimiter(10L));
    }

    @Test
    void withCentsDelimiterNinetyCents() {
        // Values with exactly 2 digits produce empty prefix: ".99"
        assertEquals(".99", Utils.withCentsDelimiter(99L));
    }

    // --- getTimeStamp tests ---

    @Test
    void getTimeStampReturnsCurrentEpochSeconds() {
        long before = System.currentTimeMillis() / 1000;
        long timestamp = Utils.getTimeStamp();
        long after = System.currentTimeMillis() / 1000;

        assertTrue(timestamp >= before && timestamp <= after,
                "Timestamp should be within 1 second of current time");
    }

    // --- throwHttpExceptionBasedOnStatusCode tests ---

    @Test
    void throwHttpExceptionFor400() {
        OpenBankingHttpBadRequestException ex = assertThrows(
                OpenBankingHttpBadRequestException.class,
                () -> Utils.throwHttpExceptionBasedOnStatusCode(400, "TestResource", "Bad request")
        );
        assertTrue(ex.getMessage().contains("TestResource"));
    }

    @Test
    void throwHttpExceptionFor401() {
        OpenBankingHttpUnauthorizedException ex = assertThrows(
                OpenBankingHttpUnauthorizedException.class,
                () -> Utils.throwHttpExceptionBasedOnStatusCode(401, "TestResource", "Unauthorized")
        );
        assertTrue(ex.getMessage().contains("TestResource"));
    }

    @Test
    void throwHttpExceptionFor403() {
        OpenBankingHttpForbiddenException ex = assertThrows(
                OpenBankingHttpForbiddenException.class,
                () -> Utils.throwHttpExceptionBasedOnStatusCode(403, "TestResource", "Forbidden")
        );
        assertTrue(ex.getMessage().contains("TestResource"));
    }

    @Test
    void throwHttpExceptionFor404() {
        OpenBankingHttpNotFoundException ex = assertThrows(
                OpenBankingHttpNotFoundException.class,
                () -> Utils.throwHttpExceptionBasedOnStatusCode(404, "TestResource", "Not found")
        );
        assertTrue(ex.getMessage().contains("TestResource"));
    }

    @Test
    void throwHttpExceptionForOtherStatusCode() {
        OpenBankingHttpException ex = assertThrows(
                OpenBankingHttpException.class,
                () -> Utils.throwHttpExceptionBasedOnStatusCode(500, "TestResource", "Internal server error")
        );
        assertEquals("Internal server error", ex.getMessage());
    }

    @Test
    void throwHttpExceptionFor502() {
        OpenBankingHttpException ex = assertThrows(
                OpenBankingHttpException.class,
                () -> Utils.throwHttpExceptionBasedOnStatusCode(502, "TestResource", "Bad gateway")
        );
        assertEquals("Bad gateway", ex.getMessage());
    }
}
