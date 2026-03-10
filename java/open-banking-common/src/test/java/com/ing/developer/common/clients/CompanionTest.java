package com.ing.developer.common.clients;

import com.ing.developer.common.Utils;
import com.ing.developer.common.exceptions.OpenBankingException;
import org.junit.jupiter.api.Test;

import javax.ws.rs.client.ClientBuilder;
import java.security.PrivateKey;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link Companion.Utils}.
 * Covers keystore loading, trust material extraction, SSL context creation,
 * and the createOpenBankingClient factory method.
 *
 * Requires test-keystore.jks in src/test/resources with alias 'sign' and password 'testpass'.
 */
class CompanionTest {

    private static final String TEST_KEYSTORE = "test-keystore.jks";
    private static final char[] TEST_PASSWORD = "testpass".toCharArray();

    // --- createOpenBankingClient tests ---

    @Test
    void createOpenBankingClientReturnsNonNullPair() {
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, false, false);
        assertNotNull(result);
        assertNotNull(result.getFirst(), "PrivateKey should not be null");
        assertNotNull(result.getSecond(), "ClientBuilder should not be null");
    }

    @Test
    void createOpenBankingClientWithPsd2Mode() {
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, true, false);
        assertNotNull(result);
        assertNotNull(result.getFirst());
        assertNotNull(result.getSecond());
    }

    @Test
    void createOpenBankingClientWithLogging() {
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, false, true);
        assertNotNull(result);
        assertNotNull(result.getFirst());
        assertNotNull(result.getSecond());
    }

    @Test
    void createOpenBankingClientWithProxyEnabled() {
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        true, "proxy.example.com", 8080, false, false);
        assertNotNull(result);
        assertNotNull(result.getFirst());
        assertNotNull(result.getSecond());
    }

    @Test
    void createOpenBankingClientWithProxyDefaultPort() {
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        true, "proxy.example.com", null, false, false);
        assertNotNull(result);
        assertNotNull(result.getFirst());
        assertNotNull(result.getSecond());
    }

    @Test
    void createOpenBankingClientWithProxyDisabled() {
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, false, false);
        assertNotNull(result);
    }

    // --- Invalid keystore tests ---

    @Test
    void createOpenBankingClientWithNonExistentKeystore() {
        // When keystore file doesn't exist, classloader returns null InputStream.
        // KeyStore.load(null, password) creates an empty keystore, but
        // getTrustMaterial will return null certificate/key for the 'sign' alias.
        // The SSL context creation still succeeds with an empty keystore.
        // This documents that no validation exception is thrown for missing files.
        Utils.Pair<PrivateKey, ClientBuilder> result =
                Companion.Utils.createOpenBankingClient(
                        "non-existent-keystore.jks", TEST_PASSWORD,
                        false, null, null, false, false);
        assertNotNull(result);
        // PrivateKey will be null since alias 'sign' doesn't exist in empty keystore
        assertNull(result.getFirst(), "PrivateKey should be null for non-existent keystore");
    }

    @Test
    void createOpenBankingClientWithWrongPassword() {
        assertThrows(OpenBankingException.class,
                () -> Companion.Utils.createOpenBankingClient(
                        TEST_KEYSTORE, "wrongpass".toCharArray(),
                        false, null, null, false, false));
    }

    // --- getTrustMaterial tests ---

    @Test
    void getTrustMaterialReturnsValidCertificateAndKey() {
        Utils.Pair<Certificate, PrivateKey> result =
                Companion.Utils.getTrustMaterial(TEST_KEYSTORE, TEST_PASSWORD);
        assertNotNull(result);
        assertNotNull(result.getFirst(), "Certificate should not be null");
        assertNotNull(result.getSecond(), "PrivateKey should not be null");
    }

    @Test
    void getTrustMaterialWithNonExistentKeystoreReturnsNulls() {
        // When keystore file doesn't exist, classloader returns null InputStream.
        // KeyStore.load(null, password) creates an empty keystore.
        // getTrustMaterial returns null cert and null key for missing alias.
        Utils.Pair<Certificate, PrivateKey> result =
                Companion.Utils.getTrustMaterial("non-existent-keystore.jks", TEST_PASSWORD);
        assertNotNull(result);
        assertNull(result.getFirst(), "Certificate should be null for non-existent keystore");
        assertNull(result.getSecond(), "PrivateKey should be null for non-existent keystore");
    }

    @Test
    void getTrustMaterialWithWrongPasswordThrows() {
        assertThrows(OpenBankingException.class,
                () -> Companion.Utils.getTrustMaterial(
                        TEST_KEYSTORE, "wrongpass".toCharArray()));
    }
}
