/**
  * Copyright (c) 2019 BusyMachines
  *
  * See company homepage at: https://www.busymachines.com/
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */

//#############################################################################
//################################## README ##################################
//#############################################################################
//
// The reason all modules gather their dependencies up top, is so that
// downstream modules declare ALL their transitive dependencies explicitly
// because otherwise fetching source code for all is kinda bugged :(
// plus, this way it should be always clear that a module only puts its
// UNIQUE dependencies out in the clear. Everything else gets brought on
// transitively anyway. So whatever change you make, please respect the
// pattern that you see here. Maybe even borrow it for other projects.
//
//#############################################################################
//#############################################################################
//#############################################################################

// format: off
addCommandAlias("it",             "IntegrationTest / test")
addCommandAlias("recompile",      ";clean;update;compile")
addCommandAlias("build",          ";compile;Test/compile")
addCommandAlias("rebuild",        ";clean;compile;Test/compile")
addCommandAlias("rebuild-update", ";clean;update;compile;Test/compile")
addCommandAlias("ci",             ";scalafmtCheck;rebuild-update;test;it")
addCommandAlias("ci-quick",       ";scalafmtCheck;build;test;it")
addCommandAlias("doLocal",        ";clean;update;compile;publishLocal")

addCommandAlias("cleanPublishSigned", ";recompile;publishSigned")
addCommandAlias("do212Release",       s";++${CompilerSettings.scala2_12};cleanPublishSigned;sonatypeBundleRelease")
addCommandAlias("do213Release",       s";++${CompilerSettings.scala2_13};cleanPublishSigned;sonatypeBundleRelease")
addCommandAlias("doRelease",          ";do212Release;do213Release")

addCommandAlias("lint", ";scalafixEnable;rebuild;scalafix;scalafmtAll")
// format: on

//*****************************************************************************
//*****************************************************************************
//********************************* PROJECTS **********************************
//*****************************************************************************
//*****************************************************************************

lazy val root = Project(id = "pureharm-aws", base = file("."))
  .settings(PublishingSettings.noPublishSettings)
  .settings(Settings.commonSettings)
  .aggregate(
    `aws-core`,
    `aws-s3`,
    `aws-cloudfront`,
    `aws-logger`,
    `aws-sns`,
  )

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++ AWS ++++++++++++++++++++++++++++++++++++
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
lazy val `aws-core-deps` = Seq(
  catsCore,
  catsEffect,
  pureharmCorePhantom,
  pureharmCoreAnomaly,
  pureharmEffectsCats,
  pureharmConfig,
  amazonRegionsV2,
)

lazy val `aws-core` = project
  .settings(PublishingSettings.sonatypeSettings)
  .settings(Settings.commonSettings)
  .settings(
    name := "pureharm-aws-core",
    libraryDependencies ++= `aws-core-deps`.distinct,
  )
  .dependsOn(
    )
  .aggregate(
    )

//#############################################################################
lazy val `aws-s3-deps` =
  `aws-core-deps` ++ Seq(
    catsCore,
    catsEffect,
    monixCatnap,
    pureharmCoreAnomaly,
    pureharmCorePhantom,
    pureharmEffectsCats,
    pureharmConfig,
    amazonS3V2,
    scalaTest      % ITT,
    log4cats       % ITT,
    logbackClassic % ITT,
  )

lazy val `aws-s3` = project
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(Settings.commonSettings)
  .settings(
    name := "pureharm-aws-s3",
    libraryDependencies ++= `aws-s3-deps`.distinct,
  )
  .dependsOn(
    `aws-core`,
  )
  .aggregate(
    `aws-core`,
  )

//#############################################################################

lazy val `aws-cloudfront-deps` =
  `aws-core-deps` ++ `aws-s3-deps` ++ Seq(
    catsCore,
    catsEffect,
    pureharmCoreAnomaly,
    pureharmCorePhantom,
    pureharmEffectsCats,
    pureharmConfig,
    amazonCloudFront,
    scalaTest      % ITT,
    log4cats       % ITT,
    http4sClient   % ITT,
    logbackClassic % ITT,
  )

lazy val `aws-cloudfront` = project
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(Settings.commonSettings)
  .settings(
    name := "pureharm-aws-cloudfront",
    libraryDependencies ++= `aws-cloudfront-deps`.distinct,
  )
  .dependsOn(
    `aws-core`,
    `aws-s3`,
  )
  .aggregate(
    `aws-core`,
    `aws-s3`,
  )

//#############################################################################

lazy val `aws-logger-deps` =
  `aws-core-deps` ++ Seq(
    catsCore,
    catsEffect,
    pureharmCoreAnomaly,
    pureharmCorePhantom,
    pureharmEffectsCats,
    pureharmConfig,
    amazonLogs,
    log4cats,
    scalaTest      % ITT,
    logbackClassic % ITT,
  )

lazy val `aws-logger` = project
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(Settings.commonSettings)
  .settings(
    name := "pureharm-aws-logger",
    libraryDependencies ++= `aws-logger-deps`.distinct,
  )
  .dependsOn(
    `aws-core`,
  )
  .aggregate(
    `aws-core`,
  )

//#############################################################################

lazy val `aws-sns-deps` =
  `aws-core-deps` ++ Seq(
    catsCore,
    catsEffect,
    pureharmCoreAnomaly,
    pureharmCorePhantom,
    pureharmEffectsCats,
    pureharmConfig,
    pureharmJsonCirce,
    amazonSNSV2,
    scalaTest      % ITT,
    log4cats       % ITT,
    http4sClient   % ITT,
    logbackClassic % ITT,
  )

lazy val `aws-sns` = project
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(PublishingSettings.sonatypeSettings)
  .settings(Settings.commonSettings)
  .settings(
    name := "pureharm-aws-sns",
    libraryDependencies ++= `aws-sns-deps`.distinct,
  )
  .dependsOn(
    `aws-core`,
  )
  .aggregate(
    `aws-core`,
  )

//#############################################################################
//#############################################################################
//################################ DEPENDENCIES ###############################
//#############################################################################
//#############################################################################

lazy val pureharmVersion:        String = "0.0.4"    //https://github.com/busymachines/pureharm/releases
lazy val scalaCollCompatVersion: String = "2.1.4"    //https://github.com/scala/scala-collection-compat/releases
lazy val shapelessVersion:       String = "2.3.3"    //https://github.com/milessabin/shapeless/releases
lazy val catsVersion:            String = "2.1.0"    //https://github.com/typelevel/cats/releases
lazy val catsEffectVersion:      String = "2.1.1"    //https://github.com/typelevel/cats-effect/releases
lazy val fs2Version:             String = "2.2.2"    //https://github.com/functional-streams-for-scala/fs2/releases
lazy val monixVersion:           String = "3.1.0"    //https://github.com/monix/monix/releases
lazy val log4catsVersion:        String = "1.0.1"    //https://github.com/ChristopherDavenport/log4cats/releases
lazy val awsJavaSdkVersion:      String = "1.11.700" //java — https://github.com/aws/aws-sdk-java/releases
lazy val awsJavaSdkV2Version:    String = "2.10.42"  //java — https://github.com/aws/aws-sdk-java-v2/releases

//these are used only for testing
lazy val logbackVersion:   String = "1.2.3"     //https://github.com/qos-ch/logback/releases
lazy val http4sVersion:    String = "0.21.0-M6" //https://github.com/http4s/http4s/releases
lazy val scalaTestVersion: String = "3.1.0"     //https://github.com/scalatest/scalatest/releases

//#############################################################################
//################################### SCALA ###################################
//#############################################################################

//https://github.com/scala/scala-collection-compat/releases
lazy val scalaCollectionCompat: ModuleID =
  "org.scala-lang.modules" %% "scala-collection-compat" % scalaCollCompatVersion withSources ()

//#############################################################################
//################################# PUREHARM ##################################
//#############################################################################

//https://github.com/busymachines/pureharm/releases/
def pureharm(m: String): ModuleID = "com.busymachines" %% s"pureharm-$m" % pureharmVersion

lazy val pureharmCore:             ModuleID = pureharm("core")              withSources ()
lazy val pureharmCoreAnomaly:      ModuleID = pureharm("core-anomaly")      withSources ()
lazy val pureharmCorePhantom:      ModuleID = pureharm("core-phantom")      withSources ()
lazy val pureharmCoreIdentifiable: ModuleID = pureharm("core-identifiable") withSources ()
lazy val pureharmEffectsCats:      ModuleID = pureharm("effects-cats")      withSources ()
lazy val pureharmJsonCirce:        ModuleID = pureharm("json-circe")        withSources ()
lazy val pureharmConfig:           ModuleID = pureharm("config")            withSources ()

//#############################################################################
//################################# TYPELEVEL #################################
//#############################################################################

//https://github.com/typelevel/cats/releases
lazy val catsCore: ModuleID = "org.typelevel" %% "cats-core" % catsVersion withSources ()

//https://github.com/typelevel/cats-effect/releases
lazy val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % catsEffectVersion withSources ()

//https://github.com/monix/monix/releases
//we use this to interop with Java Futures from AWS stuff
lazy val monixCatnap: ModuleID = "io.monix" %% "monix-catnap" % monixVersion withSources ()

//https://github.com/milessabin/shapeless/releases
lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % shapelessVersion withSources ()

//used only for testing
//https://github.com/http4s/http4s/releases
lazy val http4sClient: ModuleID = "org.http4s" %% "http4s-blaze-client" % http4sVersion withSources ()

//#############################################################################
//################################ AMAZON V1 — ################################
//#############################################################################

//https://github.com/aws/aws-sdk-java/releases
lazy val amazonCloudFront = "com.amazonaws" % "aws-java-sdk-cloudfront" % awsJavaSdkVersion withSources ()
lazy val amazonLogs       = "com.amazonaws" % "aws-java-sdk-logs"       % awsJavaSdkVersion withSources ()

//#############################################################################
//################################  AMAZON V2 ################################
//#############################################################################

//https://github.com/aws/aws-sdk-java-v2/releases
lazy val amazonRegionsV2 = "software.amazon.awssdk" % "regions" % awsJavaSdkV2Version withSources ()
/**
  * currently, pretty much only S3 is usable, cloudfront and logs lack some serious features:
  * - cloudfront: cannot sign
  * - logs: uses shitty interop w/ slf4j. Way too much magic...
  */
lazy val amazonS3V2  = "software.amazon.awssdk" % "s3"  % awsJavaSdkV2Version withSources ()
lazy val amazonSNSV2 = "software.amazon.awssdk" % "sns" % awsJavaSdkV2Version withSources ()

//#############################################################################
//################################## TESTING ##################################
//#############################################################################

//https://github.com/scalatest/scalatest/releases
lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % scalaTestVersion withSources ()

//#############################################################################
//#################################  LOGGING ##################################
//#############################################################################

//https://github.com/ChristopherDavenport/log4cats/releases
lazy val log4cats = "io.chrisdavenport" %% "log4cats-slf4j" % log4catsVersion withSources ()

//https://github.com/qos-ch/logback/releases — it is the backend implementation used by log4cats-slf4j
lazy val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion withSources ()

//#############################################################################
//################################  BUILD UTILS ###############################
//#############################################################################
/**
  * See SBT docs:
  * https://www.scala-sbt.org/release/docs/Multi-Project.html#Per-configuration+classpath+dependencies
  *
  * Ensures dependencies between the ``test`` parts of the modules
  */
def fullDependency(p: Project): ClasspathDependency = p % "compile->compile;test->test"

/**
  * Used only when one module is useful to test another module, but
  * in production build they don't require to be used together.
  */
def asTestingDependency(p: Project): ClasspathDependency = p % "test -> compile"

/**
  * Used to mark a dependency as needed in both integration tests, and tests
  */
def ITT: String = "it,test"
