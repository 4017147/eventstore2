import sbt._
import Keys._
import com.typesafe.sbt.SbtMultiJvm
import com.typesafe.sbt.SbtMultiJvm.MultiJvmKeys.{ MultiJvm }

object ExampleBuild extends Build {

  lazy val buildSettings = Defaults.defaultSettings ++ multiJvmSettings ++ Seq(
    organization := "example",
    version := "1.0",
    scalaVersion := "2.10.2",
    // make sure that the artifacts don't have the scala version in the name
    crossPaths := false
  )

  lazy val example = Project(
    id = "example",
    base = file("."),
    settings = buildSettings ++
      Seq(libraryDependencies ++= Dependencies.example)
  ) configs(MultiJvm)

  lazy val multiJvmSettings = SbtMultiJvm.multiJvmSettings ++ Seq(
    // make sure that MultiJvm test are compiled by the default test compilation
    compile in MultiJvm <<= (compile in MultiJvm) triggeredBy (compile in Test),
    // disable parallel tests
    parallelExecution in Test := false,
    // make sure that MultiJvm tests are executed by the default test target
    executeTests in Test <<=
      ((executeTests in Test), (executeTests in MultiJvm)) map {
        case ((_, testResults), (_, multiJvmResults)) =>
          val results = testResults ++ multiJvmResults
          (Tests.overall(results.values), results)
      }
  )

  object Dependencies {
    val example = Seq(
      // ---- application dependencies ----
      "com.typesafe.akka" %% "akka-actor" % "2.2.1" ,
      "com.typesafe.akka" %% "akka-remote" % "2.2.1" ,

      // ---- test dependencies ----
      "com.typesafe.akka" %% "akka-testkit" % "2.2.1" %
        "test" ,
      "com.typesafe.akka" %% "akka-multi-node-testkit" % "2.2.1" %
        "test" ,
      "org.scalatest" %% "scalatest" % "1.9.1" % "test",
      "junit" % "junit" % "4.5" % "test"
    )
  }
}

