# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

# unreleased

# 0.2.0

## deprecations

- deprecate all configuration companion objects. Use `pureharm-config-ciris` to read the configs in userland, instead of relying on the library w/ pureconfig to do it.

## dependency upgrades

- [AWS SDK v1](https://github.com/aws/aws-sdk-java/releases) `1.11.991`
- [AWS SDK v2](https://github.com/aws/aws-sdk-java-v2/releases) `2.16.34`
- [pureharm-core-sprout](https://github.com/busymachines/pureharm-core/releases) `0.2.0`
- [pureharm-core-anomaly](https://github.com/busymachines/pureharm-core/releases) `0.2.0`
- [pureharm-effects-cats](https://github.com/busymachines/pureharm-effects-cats/releases) `0.2.0`
- [pureharm-config](https://github.com/busymachines/pureharm-config/releases) `0.2.0`

## aws-logs

- now depends only on `cats-core` instead of `cats-slf4j`. There was no need to depend directly on an implementation.

### deprecations

- deprecate `AWSLoggerFactory` in favor of `AWSLoggin`. It is a straightforward rename, the semantics are the same.

# 0.1.0

- create build with github actions

Dependency upgrades:

- pureharm series `0.1.x`
- AWS SDK `1.11.979` and `2.16.23`
