package com.ing.developer.common;

import com.ing.developer.common.exceptions.OpenBankingException;
import com.ing.developer.common.exceptions.http.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

    // --- Pair tests ---

    @Test
    void pairShouldReturnFirstAndSecond() {
        Utils.Pair<String, Integer> pair = new Utils.Pair<>("hello", 42);
        assertEquals("hello", pair.getFirst());
        assertEquals(42, pair.getSecond());
    }

    @Test
    void pairShouldHandleNullValues() {
        Utils.Pair<String, String> pair = new Utils.Pair<>(null, null);
        assertNull(pair.getFirst());
        assertNull(pair.getSecond());
    }

    @Test
    void pairShouldHandleMixedNullValues() {
        Utils.Pair<String, Integer> pair1 = new Utils.Pair<>("hello", null);
        assertEquals("hello", pair1.getFirst());
        assertNull(pair1.getSecond());

        Utils.Pair<String, Integer> pair2 = new Utils.Pair<>(null, 42);
        assertNull(pair2.getFirst());
        assertEquals(42, pair2.getSecond());
    }

    @Test
    void pairShouldWorkWithDifferentTypes() {
        Utils.Pair<Long, Boolean> pair = new Utils.Pair<>(123L, true);
        assertEquals(123L, pair.getFirst());
        assertTrue(pair.getSecond());
    }

    @Test
    void pairShouldWorkWithNestedPairs() {
        Utils.Pair<String, Utils.Pair<Integer, Integer>> nested =
                new Utils.Pair<>("outer", new Utils.Pair<>(1, 2));
        assertEquals("outer", nested.getFirst());
        assertEquals(1, nested.getSecond().getFirst());
        assertEquals(2, nested.getSecond().getSecond());
    }

    // --- withCentsDelimiter() tests ---

    @Test
    void withCentsDelimiterShouldFormatCorrectly() {
        assertEquals("100.00", Utils.withCentsDelimiter(10000L));
        assertEquals("1.50", Utils.withCentsDelimiter(150L));
        assertEquals(".99", Utils.withCentsDelimiter(99L));
        assertEquals("12345.67", Utils.withCentsDelimiter(1234567L));
    }

    @Test
    void withCentsDelimiterShouldThrowForZero() {
        // The method uses substring(0, length-2) which fails for inputs with fewer than 2 digits
        assertThrows(StringIndexOutOfBoundsException.class, () -> Utils.withCentsDelimiter(0L));
    }

    @Test
    void withCentsDelimiterShouldThrowForSingleDigitInput() {
        // Single digit inputs (0-9) have length 1, causing substring(0, -1) to throw
        assertThrows(StringIndexOutOfBoundsException.class, () -> Utils.withCentsDelimiter(1L));
        assertThrows(StringIndexOutOfBoundsException.class, () -> Utils.withCentsDelimiter(9L));
    }

    @Test
    void withCentsDelimiterShouldHandleTwoDigitInput() {
        // Two digit inputs work: substring(0,0) + "." + substring(0) = ".10"
        assertEquals(".10", Utils.withCentsDelimiter(10L));
        assertEquals(".99", Utils.withCentsDelimiter(99L));
    }

    @Test
    void withCentsDelimiterShouldHandleLargeAmounts() {
        assertEquals("999999.99", Utils.withCentsDelimiter(99999999L));
        assertEquals("1000000.00", Utils.withCentsDelimiter(100000000L));
    }

    @Test
    void withCentsDelimiterShouldHandleExactDollars() {
        assertEquals("1.00", Utils.withCentsDelimiter(100L));
        assertEquals("10.00", Utils.withCentsDelimiter(1000L));
        assertEquals("500.00", Utils.withCentsDelimiter(50000L));
    }

    // --- getTimeStamp() tests ---

    @Test
    void getTimeStampShouldReturnCurrentEpochSeconds() {
        long before = System.currentTimeMillis() / 1000;
        long timestamp = Utils.getTimeStamp();
        long after = System.currentTimeMillis() / 1000;
        assertTrue(timestamp >= before && timestamp <= after);
    }

    @Test
    void getTimeStampShouldReturnPositiveValue() {
        long timestamp = Utils.getTimeStamp();
        assertTrue(timestamp > 0, "Timestamp should be positive");
    }

    @Test
    void getTimeStampShouldBeReasonablyClose() {
        long timestamp = Utils.getTimeStamp();
        long now = System.currentTimeMillis() / 1000;
        assertTrue(Math.abs(now - timestamp) <= 1, "Timestamp should be within 1 second of now");
    }

    // --- throwHttpExceptionBasedOnStatusCode() tests ---

    @Test
    void throwHttpExceptionShouldThrowBadRequestFor400() {
        assertThrows(OpenBankingHttpBadRequestException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(400, "TestResource", "Bad request"));
    }

    @Test
    void throwHttpExceptionShouldThrowUnauthorizedFor401() {
        assertThrows(OpenBankingHttpUnauthorizedException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(401, "TestResource", "Unauthorized"));
    }

    @Test
    void throwHttpExceptionShouldThrowForbiddenFor403() {
        assertThrows(OpenBankingHttpForbiddenException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(403, "TestResource", "Forbidden"));
    }

    @Test
    void throwHttpExceptionShouldThrowNotFoundFor404() {
        assertThrows(OpenBankingHttpNotFoundException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(404, "TestResource", "Not found"));
    }

    @Test
    void throwHttpExceptionShouldThrowGenericForOtherCodes() {
        assertThrows(OpenBankingHttpException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(500, "TestResource", "Server error"));
    }

    @Test
    void throwHttpExceptionShouldThrowGenericFor502() {
        assertThrows(OpenBankingHttpException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(502, "TestResource", "Bad gateway"));
    }

    @Test
    void throwHttpExceptionShouldThrowGenericFor503() {
        assertThrows(OpenBankingHttpException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(503, "TestResource", "Service unavailable"));
    }

    @Test
    void throwHttpExceptionShouldThrowGenericFor429() {
        assertThrows(OpenBankingHttpException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(429, "TestResource", "Too many requests"));
    }

    // --- Exception message content tests ---

    @Test
    void badRequestExceptionShouldContainResourceName() {
        OpenBankingHttpBadRequestException ex = assertThrows(OpenBankingHttpBadRequestException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(400, "PaymentAPI", "Bad request"));
        assertTrue(ex.getMessage().contains("PaymentAPI"), "Exception message should contain resource name");
        assertTrue(ex.getMessage().contains("Bad request"), "Exception message should indicate bad request");
    }

    @Test
    void unauthorizedExceptionShouldContainResourceName() {
        OpenBankingHttpUnauthorizedException ex = assertThrows(OpenBankingHttpUnauthorizedException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(401, "AccountAPI", "Unauthorized"));
        assertTrue(ex.getMessage().contains("AccountAPI"), "Exception message should contain resource name");
        assertTrue(ex.getMessage().contains("Unauthorized"), "Exception message should indicate unauthorized");
    }

    @Test
    void forbiddenExceptionShouldContainResourceName() {
        OpenBankingHttpForbiddenException ex = assertThrows(OpenBankingHttpForbiddenException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(403, "ShowcaseAPI", "Forbidden"));
        assertTrue(ex.getMessage().contains("ShowcaseAPI"), "Exception message should contain resource name");
        assertTrue(ex.getMessage().contains("Forbidden"), "Exception message should indicate forbidden");
    }

    @Test
    void notFoundExceptionShouldContainResourceName() {
        OpenBankingHttpNotFoundException ex = assertThrows(OpenBankingHttpNotFoundException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(404, "TokenEndpoint", "Not found"));
        assertTrue(ex.getMessage().contains("TokenEndpoint"), "Exception message should contain resource name");
        assertTrue(ex.getMessage().contains("not found"), "Exception message should indicate not found");
    }

    @Test
    void genericExceptionShouldContainErrorMessage() {
        OpenBankingHttpException ex = assertThrows(OpenBankingHttpException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(500, "Server", "Internal Server Error"));
        assertEquals("Internal Server Error", ex.getMessage());
    }

    // --- Exception hierarchy tests ---

    @Test
    void allHttpExceptionsShouldBeOpenBankingExceptions() {
        // 400
        assertThrows(OpenBankingException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(400, "Test", "msg"));
        // 401
        assertThrows(OpenBankingException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(401, "Test", "msg"));
        // 403
        assertThrows(OpenBankingException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(403, "Test", "msg"));
        // 404
        assertThrows(OpenBankingException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(404, "Test", "msg"));
        // 500
        assertThrows(OpenBankingException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(500, "Test", "msg"));
    }

    @Test
    void allHttpExceptionsShouldBeRuntimeExceptions() {
        assertThrows(RuntimeException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(400, "Test", "msg"));
        assertThrows(RuntimeException.class, () ->
                Utils.throwHttpExceptionBasedOnStatusCode(500, "Test", "msg"));
    }
}
