package com.ing.developer.common;

import org.junit.jupiter.api.Test;

import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;

import static org.junit.jupiter.api.Assertions.*;

public class SigningTest {

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
    void getDateShouldReturnValidRfcDateString() {
        String date = Signing.getDate();
        assertNotNull(date);
        // RFC date format: "EEE, dd MMM yyyy HH:mm:ss zzz"
        assertTrue(date.contains("GMT"), "Date should contain GMT timezone");
        assertTrue(date.length() > 20, "Date string should be of reasonable length");
    }

    @Test
    void getNewSignerShouldReturnNonNullOBSigner() throws Exception {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        KeyPair keyPair = keyGen.generateKeyPair();
        PrivateKey privateKey = keyPair.getPrivate();

        OBSigner signer = Signing.getNewSigner("test-client-id", privateKey);
        assertNotNull(signer);
    }
}
