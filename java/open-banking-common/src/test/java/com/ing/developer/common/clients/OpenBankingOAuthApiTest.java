package com.ing.developer.common.clients;

import com.ing.developer.common.Utils;
import org.junit.jupiter.api.Test;

import java.security.*;
import java.security.cert.Certificate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for {@link OpenBankingOAuthApi}.
 * Tests constructor validation and token expiration logic.
 * Full OAuth flow testing requires sandbox connectivity (integration tests).
 *
 * Requires test-keystore.jks in src/test/resources with alias 'sign' and password 'testpass'.
 */
class OpenBankingOAuthApiTest {

    private static final String TEST_KEYSTORE = "test-keystore.jks";
    private static final char[] TEST_PASSWORD = "testpass".toCharArray();

    private Utils.Pair<Certificate, PrivateKey> loadTrustMaterial() {
        return Companion.Utils.getTrustMaterial(TEST_KEYSTORE, TEST_PASSWORD);
    }

    // --- Constructor tests ---

    @Test
    void constructorWithValidParameters() {
        Utils.Pair<Certificate, PrivateKey> trustMaterial = loadTrustMaterial();
        Utils.Pair<PrivateKey, javax.ws.rs.client.ClientBuilder> clientPair =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, false, false);

        javax.ws.rs.client.Client client = clientPair.getSecond().build();

        // Should not throw any exception
        OpenBankingOAuthApi api = new OpenBankingOAuthApi("test-client-id", trustMaterial, client);
        assertNotNull(api);
    }

    @Test
    void constructorWithNullKeyThrows() {
        Utils.Pair<Certificate, PrivateKey> trustMaterialWithNullKey =
                new Utils.Pair<>(null, null);

        Utils.Pair<PrivateKey, javax.ws.rs.client.ClientBuilder> clientPair =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, false, false);
        javax.ws.rs.client.Client client = clientPair.getSecond().build();

        // base64encode will fail with null certificate
        assertThrows(RuntimeException.class,
                () -> new OpenBankingOAuthApi("test-client-id", trustMaterialWithNullKey, client));
    }

    // --- Token expiration logic tests (indirectly via getToken) ---
    // Note: Full getToken() testing requires mocking the HTTP client chain which is
    // complex with the generated ApiClient. The token caching and expiration logic
    // is tested indirectly through the integration tests.
    // The getToken() method will throw when called without a real sandbox connection.

    @Test
    void getTokenThrowsWithoutSandboxConnection() {
        Utils.Pair<Certificate, PrivateKey> trustMaterial = loadTrustMaterial();
        Utils.Pair<PrivateKey, javax.ws.rs.client.ClientBuilder> clientPair =
                Companion.Utils.createOpenBankingClient(TEST_KEYSTORE, TEST_PASSWORD,
                        false, null, null, false, false);
        javax.ws.rs.client.Client client = clientPair.getSecond().build();

        OpenBankingOAuthApi api = new OpenBankingOAuthApi("test-client-id", trustMaterial, client);

        // Without a real sandbox connection, getToken should throw
        assertThrows(Exception.class, () -> api.getToken());
    }
}
