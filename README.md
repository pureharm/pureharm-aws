# pureharm-aws
Wrappers over the Java APIs for interacting with AWS. Depends on [pureharm](https://github.com/busymachines/pureharm)

Currently the project is under heavy development, and is mostly driven by company needs until a stable version can be put out. At the end of the day this is a principled utility library that provides all glue to make web server development a breeze.

## sbt build

The available modules are:

```scala
val pureharmAWSVersion: String = "0.0.1" //https://github.com/busymachines/pureharm-aws/releases

def pureharmAWS(m: String): ModuleID = "com.busymachines" %% s"pureharm-aws-$m" % pureharmAWSVersion

val pureharmAWSCore:       ModuleID = pureharmAWS("core")       withSources ()
val pureharmAWSS3:         ModuleID = pureharmAWS("s3")         withSources ()
val pureharmAWSCloudfront: ModuleID = pureharmAWS("cloudfront") withSources ()
val pureharmAWSLogger:     ModuleID = pureharmAWS("logger")     withSources ()

//------- OR --------

libraryDependencies ++= Seq(
  "com.busymachines" %% s"pureharm-aws-core"       % pureharmAWSVersion,
  "com.busymachines" %% s"pureharm-aws-s3"         % pureharmAWSVersion,
  "com.busymachines" %% s"pureharm-aws-cloudfront" % pureharmAWSVersion,
  "com.busymachines" %% s"pureharm-aws-logger"     % pureharmAWSVersion,
)
```

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

All code is available to you under the Apache 2.0 license, available at [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0) and also in the [LICENSE](./LICENSE) file.
