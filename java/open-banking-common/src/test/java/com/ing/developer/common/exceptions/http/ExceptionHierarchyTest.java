package com.ing.developer.common.exceptions.http;

import com.ing.developer.common.exceptions.OpenBankingException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class ExceptionHierarchyTest {

    // --- OpenBankingException tests ---

    @Test
    void openBankingExceptionShouldExtendRuntimeException() {
        OpenBankingException ex = new OpenBankingException("test message");
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void openBankingExceptionShouldStoreMessage() {
        OpenBankingException ex = new OpenBankingException("test message");
        assertEquals("test message", ex.getMessage());
    }

    @Test
    void openBankingExceptionShouldStoreCause() {
        RuntimeException cause = new RuntimeException("root cause");
        OpenBankingException ex = new OpenBankingException(cause);
        assertEquals(cause, ex.getCause());
    }

    @Test
    void openBankingExceptionShouldStoreMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");
        OpenBankingException ex = new OpenBankingException("wrapper message", cause);
        assertEquals("wrapper message", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    // --- OpenBankingHttpException tests ---

    @Test
    void httpExceptionShouldExtendOpenBankingException() {
        OpenBankingHttpException ex = new OpenBankingHttpException("http error");
        assertInstanceOf(OpenBankingException.class, ex);
        assertInstanceOf(RuntimeException.class, ex);
    }

    @Test
    void httpExceptionShouldStoreMessage() {
        OpenBankingHttpException ex = new OpenBankingHttpException("http error message");
        assertEquals("http error message", ex.getMessage());
    }

    @Test
    void httpExceptionShouldStoreMessageAndCause() {
        Exception cause = new Exception("root");
        OpenBankingHttpException ex = new OpenBankingHttpException("http error", cause);
        assertEquals("http error", ex.getMessage());
        assertEquals(cause, ex.getCause());
    }

    // --- OpenBankingHttpBadRequestException tests ---

    @Test
    void badRequestExceptionShouldExtendHttpException() {
        OpenBankingHttpBadRequestException ex = new OpenBankingHttpBadRequestException("Resource");
        assertInstanceOf(OpenBankingHttpException.class, ex);
        assertInstanceOf(OpenBankingException.class, ex);
    }

    @Test
    void badRequestExceptionShouldFormatMessage() {
        OpenBankingHttpBadRequestException ex = new OpenBankingHttpBadRequestException("PaymentAPI");
        assertEquals("Bad request for resource: PaymentAPI", ex.getMessage());
    }

    // --- OpenBankingHttpUnauthorizedException tests ---

    @Test
    void unauthorizedExceptionShouldExtendHttpException() {
        OpenBankingHttpUnauthorizedException ex = new OpenBankingHttpUnauthorizedException("Resource");
        assertInstanceOf(OpenBankingHttpException.class, ex);
    }

    @Test
    void unauthorizedExceptionShouldFormatMessage() {
        OpenBankingHttpUnauthorizedException ex = new OpenBankingHttpUnauthorizedException("AccountAPI");
        assertEquals("Unauthorized for resource: AccountAPI", ex.getMessage());
    }

    // --- OpenBankingHttpForbiddenException tests ---

    @Test
    void forbiddenExceptionShouldExtendHttpException() {
        OpenBankingHttpForbiddenException ex = new OpenBankingHttpForbiddenException("Resource");
        assertInstanceOf(OpenBankingHttpException.class, ex);
    }

    @Test
    void forbiddenExceptionShouldFormatMessage() {
        OpenBankingHttpForbiddenException ex = new OpenBankingHttpForbiddenException("ShowcaseAPI");
        assertEquals("Forbidden for resource: ShowcaseAPI", ex.getMessage());
    }

    // --- OpenBankingHttpNotFoundException tests ---

    @Test
    void notFoundExceptionShouldExtendHttpException() {
        OpenBankingHttpNotFoundException ex = new OpenBankingHttpNotFoundException("Resource");
        assertInstanceOf(OpenBankingHttpException.class, ex);
    }

    @Test
    void notFoundExceptionShouldFormatMessage() {
        OpenBankingHttpNotFoundException ex = new OpenBankingHttpNotFoundException("TokenEndpoint");
        assertEquals("Resource: TokenEndpoint not found", ex.getMessage());
    }

    // --- Empty resource name edge cases ---

    @Test
    void badRequestWithEmptyResourceShouldStillFormat() {
        OpenBankingHttpBadRequestException ex = new OpenBankingHttpBadRequestException("");
        assertEquals("Bad request for resource: ", ex.getMessage());
    }

    @Test
    void notFoundWithEmptyResourceShouldStillFormat() {
        OpenBankingHttpNotFoundException ex = new OpenBankingHttpNotFoundException("");
        assertEquals("Resource:  not found", ex.getMessage());
    }
}
