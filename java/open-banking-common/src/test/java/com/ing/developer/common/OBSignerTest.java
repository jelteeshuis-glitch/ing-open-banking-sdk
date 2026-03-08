package com.ing.developer.common;

import org.junit.jupiter.api.Test;
import org.tomitribe.auth.signatures.Signature;

import java.io.IOException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

public class OBSignerTest {

    @Test
    void shouldConstructWithRSAKeyPairAndSign() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);
        assertNotNull(signer);
    }

    @Test
    void signShouldProduceValidBase64Output() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Map<String, String> headers = new HashMap<>();
        headers.put("date", "Mon, 01 Jan 2024 00:00:00 GMT");
        headers.put("digest", "SHA-256=test");

        Signature signed = signer.sign("get", "/test", headers);
        assertNotNull(signed);
        assertNotNull(signed.getSignature());

        // Verify the signature string is valid base64
        assertDoesNotThrow(() -> org.tomitribe.auth.signatures.Base64.decodeBase64(signed.getSignature().getBytes()));
    }

    @Test
    void signStringShouldProduceSignature() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        Signature signature = new Signature("test-key-id", "rsa-sha256", null, "(request-target)", "date", "digest");
        OBSigner signer = new OBSigner(privateKey, signature);

        Signature signed = signer.sign("test signing string");
        assertNotNull(signed);
        assertNotNull(signed.getSignature());
        assertTrue(signed.getSignature().length() > 0);
    }
}
