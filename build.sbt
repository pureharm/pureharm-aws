/*
 * Copyright 2019 BusyMachines
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

//=============================================================================
//============================== build details ================================
//=============================================================================

addCommandAlias("github-gen", "githubWorkflowGenerate")
addCommandAlias("github-check", "githubWorkflowCheck")
addCommandAlias("run-it", "IntegrationTest/test")
Global / onChangedBuildSource := ReloadOnSourceChanges

val Scala213  = "2.13.5"
val Scala3RC1 = "3.0.0-RC1"

//=============================================================================
//============================ publishing details =============================
//=============================================================================

//see: https://github.com/xerial/sbt-sonatype#buildsbt
ThisBuild / sonatypeCredentialHost := "s01.oss.sonatype.org"

ThisBuild / baseVersion  := "0.1.0"
ThisBuild / organization := "com.busymachines"
ThisBuild / organizationName := "BusyMachines"
ThisBuild / homepage     := Option(url("https://github.com/busymachines/pureharm-aws"))

ThisBuild / scmInfo := Option(
  ScmInfo(
    browseUrl  = url("https://github.com/busymachines/pureharm-aws"),
    connection = "git@github.com:busymachines/pureharm-aws.git",
  )
)

/** I want my email. So I put this here. To reduce a few lines of code,
  * the sbt-spiewak plugin generates this (except email) from these two settings:
  * {{{
  * ThisBuild / publishFullName   := "Loránd Szakács"
  * ThisBuild / publishGithubUser := "lorandszakacs"
  * }}}
  */
ThisBuild / developers := List(
  Developer(
    id    = "lorandszakacs",
    name  = "Loránd Szakács",
    email = "lorand.szakacs@protonmail.com",
    url   = new java.net.URL("https://github.com/lorandszakacs"),
  )
)

ThisBuild / startYear := Some(2019)
ThisBuild / licenses   := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

//until we get to 1.0.0, we keep strictSemVer false
ThisBuild / strictSemVer              := false
ThisBuild / spiewakCiReleaseSnapshots := false
ThisBuild / spiewakMainBranches       := List("main")
ThisBuild / Test / publishArtifact    := false

ThisBuild / scalaVersion       := Scala213
ThisBuild / crossScalaVersions := List(Scala213) //List(Scala213, Scala3RC1)

//required for binary compat checks
ThisBuild / versionIntroduced := Map(
  Scala213  -> "0.1.0",
  Scala3RC1 -> "0.1.0",
)
//=============================================================================
//================================ Dependencies ===============================
//=============================================================================
ThisBuild / resolvers += Resolver.sonatypeRepo("releases")
ThisBuild / resolvers += Resolver.sonatypeRepo("snapshots")

val pureharmCoreV:       String = "0.1.0"    //https://github.com/busymachines/pureharm-core/releases
val pureharmEffectsV:    String = "0.1.0"    //https://github.com/busymachines/pureharm-effects/releases
val pureharmJsonV:       String = "0.1.1"    //https://github.com/busymachines/pureharm-json/releases
val pureharmConfigV:     String = "0.1.0"    //https://github.com/busymachines/pureharm-config/releases
val pureharmTestkitV:    String = "0.1.0"    //https://github.com/busymachines/pureharm-testkit/releases
val fs2V:                String = "2.5.3"    //https://github.com/typelevel/fs2/releases
val monixV:              String = "3.3.0"    //https://github.com/monix/monix/releases
val log4catsV:           String = "1.2.0"    //https://github.com/ChristopherDavenport/log4cats/releases
val awsJavaSdkV:         String = "1.11.974" //java — https://github.com/aws/aws-sdk-java/releases
val awsJavaSdkV2V:       String = "2.16.18"  //java — https://github.com/aws/aws-sdk-java-v2/releases

//these are used only for testing
val logbackVersion:      String = "1.2.3"    //https://github.com/qos-ch/logback/releases
val http4sVersion:       String = "0.21.20"  //https://github.com/http4s/http4s/releases

val pureharmCoreAnomaly      = "com.busymachines" %% "pureharm-core-anomaly" % pureharmCoreV
val pureharmCoreSprout       = "com.busymachines" %% "pureharm-core-sprout" % pureharmCoreV
val pureharmCoreIdentifiable = "com.busymachines" %% "pureharm-core-identifiable" % pureharmCoreV
val pureharmEffectsCats      = "com.busymachines" %% "pureharm-effects-cats" % pureharmEffectsV
val pureharmJsonCirce        = "com.busymachines" %% "pureharm-json-circe" % pureharmJsonV
val pureharmConfig           = "com.busymachines" %% "pureharm-config" % pureharmConfigV
val pureharmTestkit          = "com.busymachines" %% "pureharm-testkit" % pureharmTestkitV


//https://github.com/monix/monix/releases
//we use this to interop with Java Futures from AWS stuff
val monixCatnap: ModuleID = "io.monix" %% "monix-catnap" % monixV withSources ()

val fs2IO:   ModuleID = "co.fs2" %% "fs2-io"   % fs2V withSources ()

//used only for testing
//https://github.com/http4s/http4s/releases
val http4sClient: ModuleID = "org.http4s" %% "http4s-blaze-client" % http4sVersion withSources ()


//https://github.com/aws/aws-sdk-java/releases
val amazonCloudFront = "com.amazonaws" % "aws-java-sdk-cloudfront" % awsJavaSdkV withSources ()
val amazonLogs       = "com.amazonaws" % "aws-java-sdk-logs"       % awsJavaSdkV withSources ()

//https://github.com/aws/aws-sdk-java-v2/releases
val amazonRegionsV2 = "software.amazon.awssdk" % "regions" % awsJavaSdkV2V withSources ()
/** currently, pretty much only S3 is usable, cloudfront and logs lack some serious features:
  * - cloudfront: cannot sign
  * - logs: uses shitty interop w/ slf4j. Way too much magic...
  */
val amazonS3V2      = "software.amazon.awssdk" % "s3"      % awsJavaSdkV2V withSources ()
val amazonSNSV2     = "software.amazon.awssdk" % "sns"     % awsJavaSdkV2V withSources ()


//https://github.com/ChristopherDavenport/log4cats/releases
val log4cats = "org.typelevel" %% "log4cats-slf4j" % log4catsV withSources ()

//https://github.com/qos-ch/logback/releases — it is the backend implementation used by log4cats-slf4j
val logbackClassic = "ch.qos.logback" % "logback-classic" % logbackVersion withSources ()


//=============================================================================
//============================== Project details ==============================
//=============================================================================

lazy val root = Project(id = "pureharm-aws", base = file("."))
  .enablePlugins(NoPublishPlugin)
  .enablePlugins(SonatypeCiReleasePlugin)
  .settings(commonSettings)
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

lazy val `aws-core` = project
  .settings(commonSettings)
  .settings(
    name := "pureharm-aws-core",
    libraryDependencies ++= Seq(
      pureharmCoreSprout,
      pureharmCoreAnomaly,
      pureharmEffectsCats,
      pureharmConfig,
      amazonRegionsV2,
    ),
  )

//#############################################################################

lazy val `aws-s3` = project
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "pureharm-aws-s3",
    libraryDependencies ++= Seq(
      monixCatnap,
      fs2IO,
      pureharmCoreAnomaly,
      pureharmCoreSprout,
      pureharmEffectsCats,
      pureharmConfig,
      amazonS3V2,
      pureharmTestkit % ITT,
      log4cats        % ITT,
      logbackClassic  % ITT,
    ),
  )
  .dependsOn(
    `aws-core`
  )

//#############################################################################

lazy val `aws-cloudfront` = project
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "pureharm-aws-cloudfront",
    libraryDependencies ++= Seq(
      pureharmCoreAnomaly,
      pureharmCoreSprout,
      pureharmEffectsCats,
      pureharmConfig,
      amazonCloudFront,
      pureharmTestkit % ITT,
      log4cats        % ITT,
      http4sClient    % ITT,
      logbackClassic  % ITT,
    ),
  )
  .dependsOn(
    `aws-core`,
    `aws-s3`,
  )

//#############################################################################

lazy val `aws-logger` = project
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "pureharm-aws-logger",
    libraryDependencies ++= Seq(
      pureharmCoreAnomaly,
      pureharmCoreSprout,
      pureharmEffectsCats,
      pureharmConfig,
      amazonLogs,
      log4cats,
      pureharmTestkit % ITT,
      logbackClassic  % ITT,
    ),
  )
  .dependsOn(
    `aws-core`
  )

//#############################################################################

lazy val `aws-sns` = project
  .settings(commonSettings)
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    name := "pureharm-aws-sns",
    libraryDependencies ++= Seq(
      pureharmCoreAnomaly,
      pureharmCoreSprout,
      pureharmEffectsCats,
      pureharmConfig,
      pureharmJsonCirce,
      amazonSNSV2,
      pureharmTestkit % ITT,
      log4cats        % ITT,
      http4sClient    % ITT,
      logbackClassic  % ITT,
    ),
  )
  .dependsOn(
    `aws-core`
  )

//=============================================================================
//================================= Settings ==================================
//=============================================================================

lazy val commonSettings = Seq(
  Compile / unmanagedSourceDirectories ++= {
    val major = if (isDotty.value) "-3" else "-2"
    List(CrossType.Pure, CrossType.Full).flatMap(
      _.sharedSrcDir(baseDirectory.value, "main").toList.map(f => file(f.getPath + major))
    )
  },
  Test / unmanagedSourceDirectories ++= {
    val major = if (isDotty.value) "-3" else "-2"
    List(CrossType.Pure, CrossType.Full).flatMap(
      _.sharedSrcDir(baseDirectory.value, "test").toList.map(f => file(f.getPath + major))
    )
  },
)

def ITT: String = "it,test"
