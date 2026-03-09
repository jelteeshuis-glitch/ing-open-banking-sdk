package com.ing.developer.common;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OBSignerTest {

    private PrivateKey privateKey;

    @BeforeEach
    void setUp() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        privateKey = keyGen.generateKeyPair().getPrivate();
    }

    // --- Constructor tests ---

    @Test
    void shouldConstructWithKeyAndSignature() {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);
        assertNotNull(signer);
    }

    @Test
    void shouldConstructWithKeySignatureAndProvider() {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature, (java.security.Provider) null);
        assertNotNull(signer);
    }

    @Test
    void shouldConstructWithKeySignatureAndClock() {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
        OBSigner signer = new OBSigner(privateKey, signature, fixedClock);
        assertNotNull(signer);
    }

    @Test
    void shouldConstructWithAllParameters() {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));
        OBSigner signer = new OBSigner(privateKey, signature, null, fixedClock);
        assertNotNull(signer);
    }

    @Test
    void constructorShouldRejectNullKey() {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        assertThrows(NullPointerException.class, () -> new OBSigner(null, signature));
    }

    @Test
    void constructorShouldRejectNullSignature() {
        assertThrows(NullPointerException.class, () -> new OBSigner(privateKey, null));
    }

    // --- sign(method, uri, headers) tests ---

    @Test
    void signShouldProduceValidBase64Output() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = signer.sign("get", "/test", headers);
        assertNotNull(signed);
        assertNotNull(signed.getSignature());
        assertDoesNotThrow(() -> org.tomitribe.auth.signatures.Base64.decodeBase64(signed.getSignature().getBytes()));
    }

    @Test
    void signShouldPreserveKeyId() throws Exception {
        String keyId = "my-unique-key-id";
        Signature signature = new Signature(keyId, "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = signer.sign("get", "/test", headers);
        assertEquals(keyId, signed.getKeyId());
    }

    @Test
    void signShouldPreserveAlgorithm() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = signer.sign("get", "/test", headers);
        assertEquals("rsa-sha256", signed.getAlgorithm().getPortableName());
    }

    @Test
    void signShouldProduceDifferentSignaturesForDifferentInputs() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature sig1 = signer.sign("get", "/path1", headers);
        Signature sig2 = signer.sign("get", "/path2", headers);
        assertNotEquals(sig1.getSignature(), sig2.getSignature());
    }

    @Test
    void signShouldBeReusableAcrossMultipleCalls() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        // OBSigner is designed to be reusable across multiple messages
        Signature signed1 = signer.sign("get", "/test1", headers);
        Signature signed2 = signer.sign("post", "/test2", headers);
        Signature signed3 = signer.sign("get", "/test1", headers);

        assertNotNull(signed1);
        assertNotNull(signed2);
        assertNotNull(signed3);
        // Same inputs should produce consistent signatures (modulo timestamp)
        assertNotEquals(signed1.getSignature(), signed2.getSignature());
    }

    // --- sign(signingString) tests ---

    @Test
    void signStringShouldProduceSignature() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Signature signed = signer.sign("test signing string");
        assertNotNull(signed);
        assertNotNull(signed.getSignature());
        assertTrue(signed.getSignature().length() > 0);
    }

    @Test
    void signStringShouldProduceDifferentResultsForDifferentInputs() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Signature signed1 = signer.sign("string one");
        Signature signed2 = signer.sign("string two");

        assertNotEquals(signed1.getSignature(), signed2.getSignature(),
                "Different signing strings should produce different signatures");
    }

    @Test
    void signStringWithEmptyInputShouldStillProduce() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Signature signed = signer.sign("");
        assertNotNull(signed);
        assertNotNull(signed.getSignature());
    }

    // --- createSigningString() tests ---

    @Test
    void createSigningStringShouldIncludeRequestTarget() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        String signingString = signer.createSigningString("get", "/oauth2/token", headers);
        assertNotNull(signingString);
        assertTrue(signingString.contains("(request-target): get /oauth2/token"),
                "Signing string should contain request target");
    }

    @Test
    void createSigningStringShouldIncludeHeaders() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=abc123");

        String signingString = signer.createSigningString("post", "/test", headers);
        assertTrue(signingString.contains("date: Mon, 01 Jan 2024 00:00:00 GMT"),
                "Signing string should contain date header");
        assertTrue(signingString.contains("digest: SHA-256=abc123"),
                "Signing string should contain digest header");
    }

    @Test
    void createSigningStringWithCreatedAndExpiresShouldWork() throws Exception {
        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        String signingString = signer.createSigningString("get", "/test", headers, 1704067200000L, 1704070800000L);
        assertNotNull(signingString);
        assertTrue(signingString.length() > 0);
    }
}
