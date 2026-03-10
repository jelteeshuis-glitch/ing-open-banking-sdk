# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/).

## [Unreleased]

### Added
- Comprehensive unit test suite for `open-banking-common` module (61 tests covering Utils, Signing, OBSigner, Companion, OpenBankingOAuthApi, and OpenBankingException)
- Unit tests for demo-app configuration classes (GreetingsConfiguration, PaymentRequestConfiguration, AccountInformationConfiguration)
- Enhanced ApiClient tests in demo-app module
- JaCoCo code coverage reporting with HTML reports and CI artifact upload
- Advisory coverage thresholds for `open-banking-common` (60% line, 40% branch)
- `TESTING.md` with testing guidelines, conventions, and keystore instructions
- `CHANGELOG.md` to track project changes

### Changed
- Aligned OpenAPI Generator Maven plugin version to 7.4.0 across all modules (was 5.2.1 in root POM, 7.4.0 in driver-generator)
- Updated custom `ApiClient.mustache` templates in oauth-driver and driver-generator to support OpenAPI Generator 7.4.0 method signatures (single-string overloads for `selectHeaderAccept` and `selectHeaderContentType`)
