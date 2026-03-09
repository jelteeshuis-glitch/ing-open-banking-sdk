package com.ing.developer.common;

import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.Signature;

import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class SigningTest {

    private static PrivateKey generateRSAPrivateKey() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair().getPrivate();
    }

    // --- digest() tests ---

    @Test
    void digestShouldReturnSha256PrefixedHash() {
        String result = Signing.digest("test");
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
    }

    @Test
    void digestShouldReturnConsistentResults() {
        String result1 = Signing.digest("hello");
        String result2 = Signing.digest("hello");
        assertEquals(result1, result2);
    }

    @Test
    void digestShouldReturnDifferentHashesForDifferentInputs() {
        String result1 = Signing.digest("hello");
        String result2 = Signing.digest("world");
        assertNotEquals(result1, result2);
    }

    @Test
    void digestOfEmptyStringShouldReturnKnownHash() {
        String result = Signing.digest("");
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
        assertEquals("SHA-256=47DEQpj8HBSa+/TImW+5JCeuQeRkm5NMpJWZG3hSuFU=", result);
    }

    @Test
    void digestOfOAuthBodyShouldProduceValidBase64() {
        String result = Signing.digest("grant_type=client_credentials");
        assertNotNull(result);
        String base64Part = result.substring("SHA-256=".length());
        assertTrue(base64Part.length() > 0);
        assertDoesNotThrow(() -> java.util.Base64.getDecoder().decode(base64Part));
    }

    @Test
    void digestWithSpecialCharactersShouldWork() {
        String result = Signing.digest("key=value&other=123&special=!@#$%");
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
    }

    @Test
    void digestWithUnicodeCharactersShouldWork() {
        String result = Signing.digest("unicode=\u00e9\u00e8\u00ea");
        assertNotNull(result);
        assertTrue(result.startsWith("SHA-256="));
    }

    // --- getDate() tests ---

    @Test
    void getDateShouldReturnValidRfcDateString() {
        String date = Signing.getDate();
        assertNotNull(date);
        assertTrue(date.contains("GMT"), "Date should contain GMT timezone");
        assertTrue(date.length() > 20, "Date string should be of reasonable length");
    }

    @Test
    void getDateShouldMatchRfcFormat() {
        String date = Signing.getDate();
        assertTrue(date.matches("\\w{3}, \\d{2} \\w{3} \\d{4} \\d{2}:\\d{2}:\\d{2} GMT"),
                "Date should match RFC format but was: " + date);
    }

    @Test
    void getDateCalledTwiceShouldBothBeValid() {
        String date1 = Signing.getDate();
        String date2 = Signing.getDate();
        assertNotNull(date1);
        assertNotNull(date2);
        assertTrue(date1.contains("GMT"));
        assertTrue(date2.contains("GMT"));
    }

    // --- getNewSigner() tests ---

    @Test
    void getNewSignerShouldReturnNonNullOBSigner() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());
        assertNotNull(signer);
    }

    @Test
    void getNewSignerWithDifferentClientIdsShouldReturnDifferentInstances() throws Exception {
        PrivateKey privateKey = generateRSAPrivateKey();
        OBSigner signer1 = Signing.getNewSigner("client-id-1", privateKey);
        OBSigner signer2 = Signing.getNewSigner("client-id-2", privateKey);
        assertNotNull(signer1);
        assertNotNull(signer2);
        assertNotSame(signer1, signer2);
    }

    // --- sign() tests ---

    @Test
    void signShouldReturnValidSignature() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = Signing.sign(signer, "get", "/test", headers);
        assertNotNull(signed);
        assertNotNull(signed.getSignature());
        assertTrue(signed.getSignature().length() > 0);
    }

    @Test
    void signShouldProduceDifferentSignaturesForDifferentMethods() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature getSignature = Signing.sign(signer, "get", "/test", headers);
        Signature postSignature = Signing.sign(signer, "post", "/test", headers);

        assertNotEquals(getSignature.getSignature(), postSignature.getSignature(),
                "Different HTTP methods should produce different signatures");
    }

    @Test
    void signShouldProduceDifferentSignaturesForDifferentPaths() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature sig1 = Signing.sign(signer, "get", "/path1", headers);
        Signature sig2 = Signing.sign(signer, "get", "/path2", headers);

        assertNotEquals(sig1.getSignature(), sig2.getSignature(),
                "Different paths should produce different signatures");
    }

    @Test
    void signShouldIncludeCorrectKeyId() throws Exception {
        String clientId = "my-special-client-id";
        OBSigner signer = Signing.getNewSigner(clientId, generateRSAPrivateKey());

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = Signing.sign(signer, "get", "/test", headers);
        assertEquals(clientId, signed.getKeyId());
    }

    @Test
    void signShouldUseRsaSha256Algorithm() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = Signing.sign(signer, "get", "/test", headers);
        assertEquals("rsa-sha256", signed.getAlgorithm().getPortableName());
    }

    @Test
    void signShouldProduceDifferentSignaturesForDifferentDigests() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());

        Map<String, String> headers1 = new HashMap<>();
        headers1.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers1.put("digest", Signing.digest("body1"));

        Map<String, String> headers2 = new HashMap<>();
        headers2.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers2.put("digest", Signing.digest("body2"));

        Signature sig1 = Signing.sign(signer, "post", "/oauth2/token", headers1);
        Signature sig2 = Signing.sign(signer, "post", "/oauth2/token", headers2);

        assertNotEquals(sig1.getSignature(), sig2.getSignature(),
                "Different digests should produce different signatures");
    }

    @Test
    void signedSignatureToStringShouldContainExpectedFields() throws Exception {
        OBSigner signer = Signing.getNewSigner("test-client-id", generateRSAPrivateKey());

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = Signing.sign(signer, "post", "/oauth2/token", headers);
        String signatureString = signed.toString();
        assertTrue(signatureString.contains("Signature "),
                "Signature.toString() should contain 'Signature ' prefix");
        assertTrue(signatureString.contains("keyId="),
                "Signature string should contain keyId");
        assertTrue(signatureString.contains("algorithm="),
                "Signature string should contain algorithm");
    }
}
