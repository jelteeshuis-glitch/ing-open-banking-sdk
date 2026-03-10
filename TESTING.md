# Testing Guide

This document describes how to run tests, view coverage reports, and add new tests to the ING Open Banking SDK.

## Running Tests

### Unit Tests

Run all unit tests across the SDK:

```bash
mvn test
```

Run unit tests for a specific module:

```bash
mvn test -pl java/open-banking-common
```

### Integration Tests

Integration tests require sandbox connectivity and valid certificates. See the main [README.md](README.md) for certificate setup instructions.

```bash
mvn verify -pl java/open-banking-demo-app
```

## Code Coverage

### Generating Coverage Reports

Coverage reports are generated automatically when running tests via the JaCoCo Maven plugin:

```bash
mvn test
```

### Viewing Coverage Reports

After running tests, open the HTML coverage report for a specific module:

```bash
open java/open-banking-common/target/site/jacoco/index.html
```

Coverage reports are also uploaded as artifacts in CI (GitHub Actions).

### Coverage Thresholds

The `open-banking-common` module has advisory coverage thresholds configured:

| Metric          | Minimum |
|-----------------|---------|
| Line coverage   | 60%     |
| Branch coverage | 40%     |

These thresholds are currently advisory (`haltOnFailure=false`) and will not fail the build.

## Test Conventions

### Frameworks

- **JUnit 5** (`org.junit.jupiter`) for test execution
- **Mockito 4.x** (`org.mockito`) for mocking and stubbing

### Naming Conventions

- Unit test classes: `*Test.java` (e.g., `UtilsTest.java`, `SigningTest.java`)
- Integration test classes: `*TestIntegration.java`
- Test methods: descriptive camelCase names (e.g., `digestOfKnownStringReturnsExpectedValue`)

### Directory Structure

```
java/open-banking-common/
  src/test/
    java/com/ing/developer/common/
      UtilsTest.java
      SigningTest.java
      OBSignerTest.java
      clients/
        CompanionTest.java
        OpenBankingOAuthApiTest.java
      exceptions/
        OpenBankingExceptionTest.java
    resources/
      test-keystore.jks
```

## Test Keystore

Several tests require a JKS keystore file located at `java/open-banking-common/src/test/resources/test-keystore.jks`.

### Regenerating the Test Keystore

If you need to regenerate the test keystore:

```bash
cd java/open-banking-common/src/test/resources

# Generate signing key
keytool -genkeypair -keyalg RSA -keysize 2048 -alias sign \
  -keystore test-keystore.jks -storepass testpass -keypass testpass \
  -dname "CN=Test Sign, O=Test, L=Test, ST=Test, C=NL"

# Generate TLS key
keytool -genkeypair -keyalg RSA -keysize 2048 -alias tls \
  -keystore test-keystore.jks -storepass testpass -keypass testpass \
  -dname "CN=Test TLS, O=Test, L=Test, ST=Test, C=NL"
```

The keystore contains two RSA 2048-bit key pairs:
- **sign** - Used for HTTP signature testing
- **tls** - Used for mTLS client testing

Password: `testpass` (for both store and key passwords)

## Adding Tests for New Features

1. Create a test class in the appropriate `src/test/java` directory following the naming convention `*Test.java`
2. Use JUnit 5 annotations (`@Test`, `@BeforeEach`, `@DisplayName`, etc.)
3. Use Mockito for mocking external dependencies
4. Run `mvn test` to verify all tests pass
5. Check coverage with `mvn test` and review the JaCoCo report
