package com.ing.developer.app.configuration;

import org.junit.jupiter.api.Test;
import org.springframework.core.env.Environment;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for the demo-app configuration classes.
 * Tests constructor behavior with mocked Spring Environment.
 * Note: buildClient() methods require a valid keystore on the classpath,
 * so we primarily test constructor validation behavior.
 */
class ConfigurationTest {

    private Environment createMockEnvironmentForPremium() {
        Environment env = mock(Environment.class);
        when(env.getRequiredProperty("premium.oauth.client-id")).thenReturn("test-client-id");
        when(env.getRequiredProperty("premium.keystore.name")).thenReturn("test-keystore.jks");
        when(env.getRequiredProperty("premium.keystore.password")).thenReturn("testpass");
        when(env.getRequiredProperty("logging", Boolean.class)).thenReturn(false);
        when(env.getRequiredProperty("proxy.use", Boolean.class)).thenReturn(false);
        when(env.getRequiredProperty("proxy.host")).thenReturn("localhost");
        when(env.getRequiredProperty("proxy.port", Integer.class)).thenReturn(3128);
        return env;
    }

    private Environment createMockEnvironmentForPsd2() {
        Environment env = mock(Environment.class);
        when(env.getRequiredProperty("psd2.key-id")).thenReturn("test-psd2-key");
        when(env.getRequiredProperty("psd2.keystore.name")).thenReturn("test-keystore.jks");
        when(env.getRequiredProperty("psd2.keystore.password")).thenReturn("testpass");
        when(env.getRequiredProperty("logging", Boolean.class)).thenReturn(false);
        when(env.getRequiredProperty("proxy.use", Boolean.class)).thenReturn(false);
        when(env.getRequiredProperty("proxy.host")).thenReturn("localhost");
        when(env.getRequiredProperty("proxy.port", Integer.class)).thenReturn(3128);
        return env;
    }

    // --- GreetingsConfiguration tests ---

    @Test
    void greetingsConfigurationCanBeConstructed() {
        Environment env = createMockEnvironmentForPremium();
        GreetingsConfiguration config = new GreetingsConfiguration(env);
        assertNotNull(config);
    }

    @Test
    void greetingsConfigurationThrowsOnMissingProperty() {
        Environment env = mock(Environment.class);
        when(env.getRequiredProperty("premium.oauth.client-id"))
                .thenThrow(new IllegalStateException("Required property not found"));
        assertThrows(IllegalStateException.class, () -> new GreetingsConfiguration(env));
    }

    // --- PaymentRequestConfiguration tests ---

    @Test
    void paymentRequestConfigurationCanBeConstructed() {
        Environment env = createMockEnvironmentForPremium();
        PaymentRequestConfiguration config = new PaymentRequestConfiguration(env);
        assertNotNull(config);
    }

    @Test
    void paymentRequestConfigurationThrowsOnMissingProperty() {
        Environment env = mock(Environment.class);
        when(env.getRequiredProperty("premium.oauth.client-id"))
                .thenThrow(new IllegalStateException("Required property not found"));
        assertThrows(IllegalStateException.class, () -> new PaymentRequestConfiguration(env));
    }

    // --- AccountInformationConfiguration tests ---

    @Test
    void accountInformationConfigurationCanBeConstructed() {
        Environment env = createMockEnvironmentForPsd2();
        AccountInformationConfiguration config = new AccountInformationConfiguration(env);
        assertNotNull(config);
    }

    @Test
    void accountInformationConfigurationThrowsOnMissingProperty() {
        Environment env = mock(Environment.class);
        when(env.getRequiredProperty("psd2.key-id"))
                .thenThrow(new IllegalStateException("Required property not found"));
        assertThrows(IllegalStateException.class, () -> new AccountInformationConfiguration(env));
    }
}
