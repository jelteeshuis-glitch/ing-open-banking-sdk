package com.ing.developer.common;

import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.*;
import java.security.cert.Certificate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Signing}.
 * Covers digest computation, date formatting, certificate base64 encoding,
 * signer creation, and the sign method.
 */
class SigningTest {

    // --- digest tests ---

    @Test
    void digestOfKnownString() {
        // SHA-256 of "hello world" is known
        String result = Signing.digest("hello world");
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
        // The base64-encoded SHA-256 of "hello world"
        String expected = "SHA-256=" + Base64.getEncoder().encodeToString(
                sha256("hello world".getBytes()));
        assertEquals(expected, result);
    }

    @Test
    void digestOfEmptyString() {
        String result = Signing.digest("");
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
        // SHA-256 of empty string is known: 47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=
        String expected = "SHA-256=" + Base64.getEncoder().encodeToString(
                sha256("".getBytes()));
        assertEquals(expected, result);
    }

    @Test
    void digestOfBodyWithSpecialChars() {
        String body = "grant_type=client_credentials&scope=payment-accounts:balances:view";
        String result = Signing.digest(body);
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
    }

    @Test
    void digestIsDeterministic() {
        String result1 = Signing.digest("test body");
        String result2 = Signing.digest("test body");
        assertEquals(result1, result2);
    }

    // --- getDate tests ---

    @Test
    void getDateReturnsRfc7231Format() {
        String date = Signing.getDate();
        assertNotNull(date);
        // Should match pattern like "Mon, 10 Mar 2026 15:30:00 GMT"
        assertTrue(date.endsWith("GMT"), "Date should end with GMT, got: " + date);
        // Verify it can be parsed back
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        ZonedDateTime parsed = ZonedDateTime.parse(date, formatter);
        assertNotNull(parsed);
    }

    @Test
    void getDateUsesGmtTimezone() {
        String date = Signing.getDate();
        assertTrue(date.contains("GMT"));
    }

    @Test
    void getDateIsCloseToCurrentTime() {
        ZonedDateTime before = ZonedDateTime.now(ZoneId.of("GMT"));
        String date = Signing.getDate();
        ZonedDateTime after = ZonedDateTime.now(ZoneId.of("GMT"));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(
                "EEE, dd MMM yyyy HH:mm:ss zzz", Locale.US);
        ZonedDateTime parsed = ZonedDateTime.parse(date, formatter);

        assertFalse(parsed.isBefore(before.minusSeconds(1)),
                "Date should not be before the test started");
        assertFalse(parsed.isAfter(after.plusSeconds(1)),
                "Date should not be after the test ended");
    }

    // --- base64encode tests ---

    @Test
    void base64encodeReturnNonEmptyString() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();

        // Create a self-signed certificate for testing
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(Thread.currentThread().getContextClassLoader()
                .getResourceAsStream("test-keystore.jks"), "testpass".toCharArray());
        Certificate cert = ks.getCertificate("sign");
        assertNotNull(cert, "Test keystore should contain a 'sign' alias");

        String encoded = Signing.base64encode(cert);
        assertNotNull(encoded);
        assertFalse(encoded.isEmpty());
        // Verify it's valid base64
        assertDoesNotThrow(() -> Base64.getDecoder().decode(encoded));
    }

    // --- getNewSigner tests ---

    @Test
    void getNewSignerReturnsNonNullSigner() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();

        OBSigner signer = Signing.getNewSigner("test-client-id", privateKey);
        assertNotNull(signer);
    }

    // --- sign tests ---

    @Test
    void signReturnsValidSignature() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();

        OBSigner signer = Signing.getNewSigner("test-client-id", privateKey);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("digest", Signing.digest("test body"));
        headers.put("date", Signing.getDate());

        Signature signature = Signing.sign(signer, "post", "/oauth2/token", headers);
        assertNotNull(signature);
        assertNotNull(signature.getSignature());
        assertFalse(signature.getSignature().isEmpty());
    }

    @Test
    void signWithDifferentMethodsProducesDifferentSignatures() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        PrivateKey privateKey = keyGen.generateKeyPair().getPrivate();

        OBSigner signer = Signing.getNewSigner("test-client-id", privateKey);

        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("digest", Signing.digest("test body"));
        headers.put("date", Signing.getDate());

        Signature sig1 = Signing.sign(signer, "get", "/api/test", headers);
        Signature sig2 = Signing.sign(signer, "post", "/api/test", headers);

        assertNotEquals(sig1.getSignature(), sig2.getSignature());
    }

    private static byte[] sha256(byte[] input) {
        try {
            return MessageDigest.getInstance("SHA-256").digest(input);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }
}
