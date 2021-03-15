# pureharm-aws

Wrappers over the Java APIs for interacting with AWS. Depends on [pureharm](https://github.com/busymachines/pureharm)

Currently, the project is under heavy development, and is mostly driven by company needs until a stable version can be
put out. At the end of the day this is a principled utility library that provides all glue to make web server
development a breeze.

## modules

Depend on [pureharm kernel, config, json](https://github.com/busymachines/pureharm/releases) `0.0.7`

The available modules are (for Scala 2.13):

- `"com.busymachines" %% s"pureharm-aws-core" % "0.0.7"`
- `"com.busymachines" %% s"pureharm-aws-s3" % "0.0.7"`
- `"com.busymachines" %% s"pureharm-aws-sns" % "0.0.7"`
- `"com.busymachines" %% s"pureharm-aws-cloudfront" % "0.0.7"`
- `"com.busymachines" %% s"pureharm-aws-logger" % "0.0.7"`

### For the impatient

Add these resolvers to your settings to quickly get freshly published versions:

```
resolvers ++= Seq(
  Resolver.sonatypeRepo("public"),
  Resolver.sonatypeRepo("snapshots"),
)
```

## Usage

Under construction. See [release notes](https://github.com/busymachines/pureharm-aws/releases) and tests for examples.

## Copyright and License

All code is available to you under the Apache 2.0 license, available
at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0) and also in
the [LICENSE](./LICENSE) file.
