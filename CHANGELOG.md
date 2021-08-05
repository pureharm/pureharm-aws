# Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/),
and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

# unreleased

# 0.4.0-M1

This is the first release available for Scala 3 and cats-effect 3!

### :warning: breaking changes :warning:
- removed dependency on pureharm-config and config-readers from companion objects of config case classes. This means that you have to read them yourselves in client code depending on the method you choose. Recommended way is using [`pureharm-config-ciris`](https://github.com/busymachines/pureharm-config-ciris), or for a smooth transition depend on [`pureharm-config`](https://github.com/busymachines/pureharm-config) directly.
- for cats-effect to compatibility use the modules suffixed w/ `-ce2`, modules without this suffix depend on cats-effect 3.

### New Scala versions:
- `2.13.6`
- `3.0.1` for JVM + JS platforms
- drop `3.0.0-RC2`, `3.0.0-RC3`

### internals
- temporarily disable tests, will be revived in a future milestone release
- bump scalafmt to `3.0.0-RC6` — from `2.7.5`
- bump sbt to `1.5.5`
- bump sbt-spiewak to `0.21.0`
- bump sbt-scalafmt to `2.4.3`

# 0.3.0

- Reverse deprecation on configuration companion objects. But this is the last release that depends on pureharm-config.
- Remove support for reading cloudfront signing keys as `.DER` format

## dependency upgrades

- [AWS SDK v1](https://github.com/aws/aws-sdk-java/releases) `1.11.996`
- [AWS SDK v2](https://github.com/aws/aws-sdk-java-v2/releases) `2.16.39`
- [pureharm-effects-cats](https://github.com/busymachines/pureharm-effects-cats/releases) `0.4.0`
- [pureharm-config](https://github.com/busymachines/pureharm-config/releases) `0.4.0`

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
