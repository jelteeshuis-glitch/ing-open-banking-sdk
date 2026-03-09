# Migration Guide: ING Open Banking SDK 1.0.0

This document describes the breaking changes introduced in version 1.0.0 of the ING Open Banking SDK and provides guidance for migrating from previous versions (0.0.3-SNAPSHOT).

## Breaking Changes

### 1. Java 17 Minimum Requirement

The SDK now requires **Java 17** or later. Java 8 and 11 are no longer supported.

**Action required:** Update your project to use JDK 17 or later, and set `<java.version>17</java.version>` in your POM.

### 2. Jakarta EE Namespace Migration (javax.ws.rs → jakarta.ws.rs)

All Jakarta EE imports have been migrated from `javax.ws.rs.*` to `jakarta.ws.rs.*`. This is required by Jersey 3.x.

**Action required:** Update all imports in your code that reference `javax.ws.rs`:

```java
// Before
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.MediaType;

// After
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.core.MediaType;
```

> **Note:** JDK classes such as `javax.net.ssl.*`, `javax.crypto.*`, `javax.security.*`, and `java.security.*` are **NOT** affected by this change. These remain unchanged.

### 3. Jersey 3.1.x

The HTTP client library has been upgraded from Jersey 2.36 to Jersey 3.1.9. This is the primary driver for the Jakarta namespace change.

### 4. Spring Boot 3.4.x

The demo and simple application modules now use Spring Boot 3.4.3 (upgraded from 2.7.2 / 2.3.3.RELEASE). If you use these as reference implementations, note that Spring Boot 3.x requires Java 17+ and uses the Jakarta namespace.

### 5. Swagger Annotations 2.x

Swagger annotations have been upgraded from `io.swagger:swagger-annotations` 1.6.2 to `io.swagger.core.v3:swagger-annotations` 2.2.25. The Maven coordinates have changed:

```xml
<!-- Before -->
<dependency>
    <groupId>io.swagger</groupId>
    <artifactId>swagger-annotations</artifactId>
</dependency>

<!-- After -->
<dependency>
    <groupId>io.swagger.core.v3</groupId>
    <artifactId>swagger-annotations</artifactId>
</dependency>
```

### 6. OpenAPI Generator 7.x

The OpenAPI Generator plugin has been upgraded from 5.2.1 to 7.4.0, and the library configuration has changed from `jersey2` to `jersey3`.

## Dependency Version Changes

| Dependency | Old Version | New Version |
|---|---|---|
| Java | 1.8 / 11 | 17 |
| Jersey | 2.36 | 3.1.9 |
| Jackson | 2.13.3 | 2.17.2 |
| Spring Boot | 2.7.2 / 2.3.3 | 3.4.3 |
| Swagger Annotations | 1.6.2 | 2.2.25 |
| OpenAPI Generator | 5.2.1 | 7.4.0 |
| Apache HttpClient | 4.5.13 | 4.5.14 |
| JUnit | 5.7.1 / 5.8.1 | 5.11.4 |
| Commons Codec | 1.9 | 1.17.1 |
| ScribeJava | 8.3.1 | 8.3.3 |
| Jackson Databind Nullable | 0.2.2 | 0.2.6 |
| Maven Surefire Plugin | 3.0.0-M4 / 2.12 | 3.5.2 |
| Maven Compiler Plugin | 3.8.1 / 3.6.1 | 3.13.0 |

## Step-by-Step Migration Guide

1. **Update your JDK** to version 17 or later
2. **Update your Maven** to version 3.9 or later (recommended)
3. **Update SDK dependency version** from `0.0.3-SNAPSHOT` to `1.0.0-SNAPSHOT`
4. **Find and replace imports** in your code:
   - `javax.ws.rs.` → `jakarta.ws.rs.`
   - Do **NOT** change `javax.net.ssl.*`, `javax.crypto.*`, or `javax.security.*`
5. **Update Swagger annotations** if used directly:
   - Change Maven coordinates from `io.swagger:swagger-annotations` to `io.swagger.core.v3:swagger-annotations`
6. **Rebuild** with `mvn clean install`
7. **Test** your application thoroughly

## Known Issues

- The `tomitribe-http-signatures` library (version 1.8) remains unchanged as there is no newer compatible version
- Apache HttpClient remains at 4.5.x; migration to HttpClient 5.x would require significant API changes and is planned as a separate effort
