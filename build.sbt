import sbt.Keys._
import sbt.ModuleID
import sbtassembly.AssemblyKeys.{assemblyMergeStrategy, assemblyShadeRules}
import sbtassembly.{MergeStrategy, PathList, ShadeRule}

// Common settings
val projectName = "kip-portal"
val ver = "1.0"
val scalaVer = "2.11.8"
val org = "com.knoldus"

name := projectName
version := ver
scalaVersion := scalaVer
organization := org

val akkaHttpVersion = "10.0.10"
val scalaTestVersion = "3.0.1"
val javaMailVersion = "1.4"
val cassandraVersion = "2.1.10.3"
val confServiceVersion = "1.2.1"
val loggerVersion = "1.7.25"

lazy val commonDependencies: Seq[ModuleID] = Seq(
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "org.scalatest" %% "scalatest" % scalaTestVersion % Test,
  "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
  "javax.mail" % "mail" % javaMailVersion,
  "com.typesafe" % "config" % confServiceVersion,
  "org.slf4j" % "slf4j-api" % loggerVersion,
  "org.slf4j" % "slf4j-simple" % loggerVersion
)

lazy val persistanceDependencies: Seq[ModuleID] = Seq(
  "com.outworkers" %% "phantom-dsl" % "2.7.6",
  "com.outworkers" %% "phantom-connectors" % "2.7.6"
)

lazy val commonSettings: Seq[Def.Setting[_]] = Seq(
  version := ver,

  organization := org,

  scalaVersion := scalaVer,

  fork in Test := true,

  parallelExecution in Test := false,

  assemblyShadeRules in assembly := Seq(
    ShadeRule
      .rename("io.netty.**" -> "shadeio.@1")
      .inLibrary("io.netty" % "netty-all" % "4.0.23.Final")
      .inLibrary("org.apache.cassandra" % "cassandra-all" % "2.1.9")
  ),

  assemblyMergeStrategy in assembly := {
    case PathList("META-INF", xs@_*) => MergeStrategy.discard
    case "application.conf" => MergeStrategy.last
    case "reference.conf" => MergeStrategy.concat
    case _ => MergeStrategy.first
  }
)

lazy val common = (project in file("common")).settings(
  commonSettings,
  libraryDependencies ++= commonDependencies
)

lazy val userApi = (project in file("user-api")).settings(
  commonSettings,
  libraryDependencies ++= commonDependencies
)

lazy val persistance = (project in file("persistance")).settings(
  commonSettings,
  libraryDependencies ++= (commonDependencies ++ persistanceDependencies)
)

lazy val notificationApi = (project in file("notification-api")).settings(
  commonSettings,
  libraryDependencies ++= commonDependencies
).dependsOn(userApi, persistance, userApi, common)

lazy val root = project.in(file(".")).aggregate(userApi, common, persistance, notificationApi)
