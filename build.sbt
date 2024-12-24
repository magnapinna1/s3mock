import sbt.librarymanagement.Developer
import sbt.librarymanagement.ivy.Credentials
import xerial.sbt.Sonatype.sonatypeCentralHost

ThisBuild / name := "s3mock"
ThisBuild / organization := "io.github.magnapinna1"
ThisBuild / scalaVersion := "2.12.12"
ThisBuild / licenses := Seq("MIT" -> url("https://opensource.org/licenses/MIT"))
ThisBuild / homepage := Some(url("https://github.com/magnapinna1/s3mock"))
scmInfo := Some(
  ScmInfo(
    url("https://github.com/magnapinna1/s3mock"),
    "scm:git@github.com:magnapinna1/s3mock.git"
  )
)

ThisBuild / developers := List(
  Developer(
    "magnapinna1",
    "Michael M",
    "mmoich123@gmail.com",
    url("https://github.com/magnapinna1")
  )
)

lazy val pekkoVersion = "1.0.2"


ThisBuild / sonatypeCredentialHost := sonatypeCentralHost
ThisBuild / publishTo := sonatypePublishToBundle.value
ThisBuild / version := "1.0.0"

ThisBuild / libraryDependencies ++= Seq(
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-http" % "1.0.0",
  "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test,
  "org.scala-lang.modules" %% "scala-xml" % "1.3.0",
  "org.scala-lang.modules" %% "scala-collection-compat" % "2.5.0",
  "com.github.pathikrit" %% "better-files" % "3.9.1",
  "com.typesafe.scala-logging" %% "scala-logging" % "3.9.4",
  "com.amazonaws" % "aws-java-sdk-s3" % "1.12.650",
  "org.scalatest" %% "scalatest" % "3.2.9" % Test,
  "ch.qos.logback" % "logback-classic" % "1.4.14" % Test,
  "org.iq80.leveldb" % "leveldb" % "0.12",
  "org.apache.pekko" %% "pekko-connectors-s3" % pekkoVersion % Test,
  "javax.xml.bind" % "jaxb-api" % "2.3.1",
  "com.sun.xml.bind" % "jaxb-core" % "3.0.1",
  "com.sun.xml.bind" % "jaxb-impl" % "3.0.1"
)

ThisBuild / parallelExecution := false
Global / concurrentRestrictions += Tags.limit(Tags.Test, 1)