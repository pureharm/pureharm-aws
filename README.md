# pureharm-aws

Pure functional wrappers in the typelevel ecosystem over the AWS Java SDK.

See [changelog](./CHANGELOG.md).

## modules

The available modules are (for Scala 2.13):

- `"com.busymachines" %% s"pureharm-aws-core" % "0.3.0"`
- `"com.busymachines" %% s"pureharm-aws-s3" % "0.3.0"`
- `"com.busymachines" %% s"pureharm-aws-sns" % "0.3.0"`
  - [pureharm-json-circe](https://github.com/busymachines/pureharm-json-circe/releases) `0.2.0`
- `"com.busymachines" %% s"pureharm-aws-cloudfront" % "0.3.0"`
- `"com.busymachines" %% s"pureharm-aws-logger" % "0.3.0"`
  - [log4cats](https://github.com/typelevel/log4cats/releases) `1.2.2`

Common downstream dependencies:

- [AWS SDK v1](https://github.com/aws/aws-sdk-java/releases) `1.11.996`
- [AWS SDK v2](https://github.com/aws/aws-sdk-java-v2/releases) `2.16.39`
- [pureharm-core-sprout](https://github.com/busymachines/pureharm-core/releases) `0.2.0`
- [pureharm-core-anomaly](https://github.com/busymachines/pureharm-core/releases) `0.2.0`
- [pureharm-effects-cats](https://github.com/busymachines/pureharm-effects-cats/releases) `0.4.0`
- [pureharm-config](https://github.com/busymachines/pureharm-config/releases) `0.4.0`

## usage

Under construction. See [release notes](https://github.com/busymachines/pureharm-aws/releases) and tests for examples.

## Copyright and License

All code is available to you under the Apache 2.0 license, available
at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0) and also in
the [LICENSE](./LICENSE) file.
