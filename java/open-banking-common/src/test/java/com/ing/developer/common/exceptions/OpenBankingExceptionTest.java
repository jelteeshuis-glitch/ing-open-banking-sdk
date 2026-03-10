package com.ing.developer.common.exceptions;

import com.ing.developer.common.exceptions.http.*;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OpenBankingException} and its HTTP exception subclasses.
 * Verifies constructors, messages, causes, and exception hierarchy.
 */
class OpenBankingExceptionTest {

    // --- OpenBankingException tests ---

    @Test
    void constructWithMessageOnly() {
        OpenBankingException ex = new OpenBankingException("test message");
        assertEquals("test message", ex.getMessage());
        assertNull(ex.getCause());
    }

    @Test
    void constructWithCauseOnly() {
        RuntimeException cause = new RuntimeException("root cause");
        OpenBankingException ex = new OpenBankingException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void constructWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        OpenBankingException ex = new OpenBankingException("test message", cause);
        assertEquals("test message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    @Test
    void isRuntimeException() {
        OpenBankingException ex = new OpenBankingException("test");
        assertTrue(ex instanceof RuntimeException);
    }

    // --- OpenBankingHttpException tests ---

    @Test
    void httpExceptionWithMessage() {
        OpenBankingHttpException ex = new OpenBankingHttpException("http error");
        assertEquals("http error", ex.getMessage());
        assertTrue(ex instanceof OpenBankingException);
    }

    @Test
    void httpExceptionWithMessageAndCause() {
        RuntimeException cause = new RuntimeException("root");
        OpenBankingHttpException ex = new OpenBankingHttpException("http error", cause);
        assertEquals("http error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    // --- OpenBankingHttpBadRequestException tests ---

    @Test
    void badRequestExceptionContainsResourceName() {
        OpenBankingHttpBadRequestException ex = new OpenBankingHttpBadRequestException("MyResource");
        assertTrue(ex.getMessage().contains("MyResource"));
        assertTrue(ex.getMessage().contains("Bad request"));
        assertTrue(ex instanceof OpenBankingHttpException);
    }

    // --- OpenBankingHttpUnauthorizedException tests ---

    @Test
    void unauthorizedExceptionContainsResourceName() {
        OpenBankingHttpUnauthorizedException ex = new OpenBankingHttpUnauthorizedException("MyResource");
        assertTrue(ex.getMessage().contains("MyResource"));
        assertTrue(ex.getMessage().contains("Unauthorized"));
        assertTrue(ex instanceof OpenBankingHttpException);
    }

    // --- OpenBankingHttpForbiddenException tests ---

    @Test
    void forbiddenExceptionContainsResourceName() {
        OpenBankingHttpForbiddenException ex = new OpenBankingHttpForbiddenException("MyResource");
        assertTrue(ex.getMessage().contains("MyResource"));
        assertTrue(ex.getMessage().contains("Forbidden"));
        assertTrue(ex instanceof OpenBankingHttpException);
    }

    // --- OpenBankingHttpNotFoundException tests ---

    @Test
    void notFoundExceptionContainsResourceName() {
        OpenBankingHttpNotFoundException ex = new OpenBankingHttpNotFoundException("MyResource");
        assertTrue(ex.getMessage().contains("MyResource"));
        assertTrue(ex.getMessage().contains("not found"));
        assertTrue(ex instanceof OpenBankingHttpException);
    }
}
