import sbt.Keys.crossScalaVersions

name := "scala-pact-app1"

lazy val commonSettings = Seq(
  organization := "com.github",
  scalaVersion := "2.12.1",
  version := "0.1.0",
  crossScalaVersions := Seq("2.12.1", "2.11.8"),
  libraryDependencies ++= {
    val akkaHttpVersion = "10.0.5"
    val slickVersion = "3.2.0"
    Seq(
      "com.typesafe.akka" %% "akka-http" % akkaHttpVersion,
      "com.typesafe.akka" %% "akka-http-spray-json" % akkaHttpVersion,
      "com.typesafe" % "config" % "1.3.1",
      "org.flywaydb" % "flyway-core" % "4.1.2",
      "mysql" % "mysql-connector-java" % "6.0.6",
      "com.typesafe.slick" %% "slick" % slickVersion,
      "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
      "org.slf4j" % "slf4j-nop" % "1.6.4",
//      "ch.qos.logback" % "logback-classic" % "1.2.2",
      "org.scalatest" %% "scalatest" % "3.0.1" % Test,
      "com.itv" %% "scalapact-scalatest" % "2.1.3" % Test,
      "org.mockito" % "mockito-core" % "2.7.21" % Test,
      "com.whisk" %% "docker-testkit-scalatest" % "0.9.0" % Test,
//      "com.whisk" %% "docker-testkit-impl-spotify" % "0.9.0" % Test,
      "com.whisk" %% "docker-testkit-impl-docker-java" % "0.9.0" % Test

    )
  }
)

lazy val root = (project in file("."))
  .settings(commonSettings: _*)