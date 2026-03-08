package com.ing.developer.common;

import com.ing.developer.common.exceptions.http.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class UtilsTest {

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
    void withCentsDelimiterShouldFormatCorrectly() {
        assertEquals("100.00", Utils.withCentsDelimiter(10000L));
        assertEquals("1.50", Utils.withCentsDelimiter(150L));
        assertEquals(".99", Utils.withCentsDelimiter(99L));
        assertEquals("12345.67", Utils.withCentsDelimiter(1234567L));
    }

    @Test
    void getTimeStampShouldReturnCurrentEpochSeconds() {
        long before = System.currentTimeMillis() / 1000;
        long timestamp = Utils.getTimeStamp();
        long after = System.currentTimeMillis() / 1000;
        assertTrue(timestamp >= before && timestamp <= after);
    }

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
}
