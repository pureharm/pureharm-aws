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

addCommandAlias("build", ";compile;Test/compile")
addCommandAlias("rebuild", ";clean;compile;Test/compile")
addCommandAlias("rebuild-update", ";clean;update;compile;Test/compile")
addCommandAlias("ci", ";scalafmtCheck;rebuild-update;test")
addCommandAlias("ci-quick", ";scalafmtCheck;build;test")
addCommandAlias("doLocal", ";clean;update;compile;publishLocal")
addCommandAlias("doRelease", ";rebuild-update;publishSigned;sonatypeRelease")

addCommandAlias("lint", ";scalafixEnable;rebuild;scalafix;scalafmtAll")

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
    `aws-logs`,
  )

//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
//++++++++++++++++++++++++++++++++++++ AWS ++++++++++++++++++++++++++++++++++++
//+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
lazy val `aws-core-deps` = Seq(
  pureharmCoreAnomaly,
  pureharmEffectsCats,
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
  `aws-core-deps` ++ Seq()

lazy val `aws-s3` = project
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
  `aws-core-deps` ++ `aws-s3-deps` ++ Seq()

lazy val `aws-cloudfront` = project
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

lazy val `aws-logs-deps` =
  `aws-core-deps` ++ Seq()

lazy val `aws-logs` = project
  .settings(PublishingSettings.sonatypeSettings)
  .settings(Settings.commonSettings)
  .settings(
    name := "pureharm-aws-logs",
    libraryDependencies ++= `aws-logs-deps`.distinct,
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

lazy val pureharmVersion:        String = "0.0.2-M19"    //https://github.com/busymachines/pureharm/releases
lazy val scalaCollCompatVersion: String = "2.1.2"        //https://github.com/scala/scala-collection-compat/releases
lazy val shapelessVersion:       String = "2.3.3"        //https://github.com/milessabin/shapeless/releases
lazy val catsVersion:            String = "2.0.0-RC2"    //https://github.com/typelevel/cats/releases
lazy val catsEffectVersion:      String = "2.0.0-RC2"    //https://github.com/typelevel/cats-effect/releases
lazy val log4catsVersion:        String = "1.0.0-RC3"    //https://github.com/ChristopherDavenport/log4cats/releases
lazy val logbackVersion:         String = "1.2.3"        //https://github.com/qos-ch/logback/releases
lazy val pureconfigVersion:      String = "0.11.1"       //https://github.com/pureconfig/pureconfig/releases
lazy val scalaTestVersion:       String = "3.1.0-SNAP13" //https://github.com/scalatest/scalatest/releases

lazy val awsJavaSdkVersion:   String = "1.11.590" //java — https://github.com/aws/aws-sdk-java/releases
lazy val awsJavaSdkV2Version: String = "2.7.3"    //java — https://github.com/aws/aws-sdk-java-v2/releases

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
lazy val catsCore:    ModuleID = "org.typelevel" %% "cats-core"    % catsVersion withSources ()
lazy val catsMacros:  ModuleID = "org.typelevel" %% "cats-macros"  % catsVersion withSources ()
lazy val catsKernel:  ModuleID = "org.typelevel" %% "cats-kernel"  % catsVersion withSources ()
lazy val catsLaws:    ModuleID = "org.typelevel" %% "cats-laws"    % catsVersion withSources ()
lazy val catsTestkit: ModuleID = "org.typelevel" %% "cats-testkit" % catsVersion withSources ()

lazy val cats: Seq[ModuleID] = Seq(
  catsCore,
  catsMacros,
  catsKernel,
  catsLaws,
  catsTestkit % Test,
)

//https://github.com/typelevel/cats-effect/releases
lazy val catsEffect: ModuleID = "org.typelevel" %% "cats-effect" % catsEffectVersion withSources ()

//https://github.com/milessabin/shapeless/releases
lazy val shapeless: ModuleID = "com.chuusai" %% "shapeless" % shapelessVersion withSources ()

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
lazy val amazonS3V2 = "software.amazon.awssdk" % "s3" % awsJavaSdkV2Version withSources ()

//#############################################################################
//################################## TESTING ##################################
//#############################################################################

//https://github.com/scalatest/scalatest/releases
lazy val scalaTest: ModuleID = "org.scalatest" %% "scalatest" % scalaTestVersion withSources ()

//#############################################################################
//################################## HELPERS ##################################
//#############################################################################

lazy val pureConfig: ModuleID = "com.github.pureconfig" %% "pureconfig" % pureconfigVersion withSources ()

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
