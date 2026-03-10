package com.ing.developer.common;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.*;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OBSigner}.
 * Covers construction, signing operations, determinism, and key differentiation.
 */
class OBSignerTest {

    private static KeyPair keyPair;

    @BeforeAll
    static void generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        keyPair = keyGen.generateKeyPair();
    }

    // --- Constructor tests ---

    @Test
    void constructorWithValidKeyAndSignature() {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(keyPair.getPrivate(), sig);
        assertNotNull(signer);
    }

    @Test
    void constructorThrowsOnNullKey() {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        assertThrows(NullPointerException.class, () -> new OBSigner(null, sig));
    }

    @Test
    void constructorThrowsOnNullSignature() {
        assertThrows(NullPointerException.class,
                () -> new OBSigner(keyPair.getPrivate(), null));
    }

    // --- sign(method, uri, headers) tests ---

    @Test
    void signWithMethodUriHeadersReturnsValidSignature() throws IOException {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(keyPair.getPrivate(), sig);

        Map<String, String> headers = createTestHeaders();

        Signature result = signer.sign("post", "/oauth2/token", headers);
        assertNotNull(result);
        assertNotNull(result.getSignature());
        assertFalse(result.getSignature().isEmpty());
        assertEquals("test-key-id", result.getKeyId());
    }

    @Test
    void signatureContainsBase64Content() throws IOException {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(keyPair.getPrivate(), sig);

        Map<String, String> headers = createTestHeaders();
        Signature result = signer.sign("post", "/oauth2/token", headers);

        // The signature field should be valid base64
        String sigString = result.getSignature();
        assertNotNull(sigString);
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(sigString),
                "Signature should be valid base64");
    }

    // --- sign(signingString) tests ---

    @Test
    void signStringReturnsValidSignature() throws IOException {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(keyPair.getPrivate(), sig);

        Signature result = signer.sign("test signing string content");
        assertNotNull(result);
        assertNotNull(result.getSignature());
        assertFalse(result.getSignature().isEmpty());
    }

    // --- Determinism tests ---

    @Test
    void signingSameHeadersTwiceProducesSameSignature() throws IOException {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(keyPair.getPrivate(), sig);

        // Use same signing string to guarantee determinism (sign(method, uri, headers)
        // uses clock.millis() which may differ between calls)
        String signingString = "test deterministic signing";
        Signature result1 = signer.sign(signingString);
        Signature result2 = signer.sign(signingString);

        assertEquals(result1.getSignature(), result2.getSignature());
    }

    // --- Different keys produce different signatures ---

    @Test
    void differentKeysProduceDifferentSignatures() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair2 = keyGen.generateKeyPair();

        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");

        OBSigner signer1 = new OBSigner(keyPair.getPrivate(), sig);
        OBSigner signer2 = new OBSigner(keyPair2.getPrivate(), sig);

        String signingString = "test content for different keys";
        Signature result1 = signer1.sign(signingString);
        Signature result2 = signer2.sign(signingString);

        assertNotEquals(result1.getSignature(), result2.getSignature(),
                "Different keys should produce different signatures");
    }

    // --- createSigningString tests ---

    @Test
    void createSigningStringReturnsNonEmpty() throws IOException {
        Signature sig = new Signature("test-key-id", "rsa-sha256", null,
                "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(keyPair.getPrivate(), sig);

        Map<String, String> headers = createTestHeaders();
        String signingString = signer.createSigningString("post", "/test", headers);
        assertNotNull(signingString);
        assertFalse(signingString.isEmpty());
        assertTrue(signingString.contains("(request-target): post /test"));
    }

    private Map<String, String> createTestHeaders() {
        Map<String, String> headers = new LinkedHashMap<>();
        headers.put("digest", "SHA-256=testdigest");
        headers.put("date", "Mon, 10 Mar 2026 12:00:00 GMT");
        return headers;
    }
}
