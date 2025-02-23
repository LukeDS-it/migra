val projectScalaVersion = "2.13.16"
ThisBuild / scalaVersion := projectScalaVersion

val pekkoVersion = "1.1.3"
val pekkoJdbcVersion = "1.1.0"
val pekkoHttpVersion = "1.1.0"
val slickVersion = "3.5.2"
val mysqlVersion = "8.0.33"
val h2Version = "2.3.232"
val scalaLoggingVersion = "3.9.5"
val logbackVersion = "1.5.16"
val janinoVersion = "3.1.12"
val encoderVersion = "8.0"
val pekkoHttpJsonVersion = "2.5.0"
val circeVersion = "0.14.10"
val circeYamlVersion = "0.15.1"
val freemarkerVersion = "2.3.34"
val scalacticVersion = "3.2.19"
val lang3Version = "3.17.0"
val jsonPathVersion = "2.9.0"
val jacksonVersion = "2.18.2"
val wiremockVersion = "3.0.1"
val scalamockVersion = "5.2.0"
val postgresqlVersion = "42.7.5"
val flywayVersion = "11.3.3"
val mockitoScalaVersion = "1.17.37"
val graalVmVersion = "24.1.2"

val pekko = Seq(
  "org.apache.pekko" %% "pekko-stream" % pekkoVersion,
  "org.apache.pekko" %% "pekko-actor-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-persistence-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-cluster-sharding-typed" % pekkoVersion,
  "org.apache.pekko" %% "pekko-persistence-query" % pekkoVersion,
  "org.apache.pekko" %% "pekko-serialization-jackson" % pekkoVersion,
  "org.apache.pekko" %% "pekko-slf4j" % pekkoVersion,
  "org.apache.pekko" %% "pekko-persistence-jdbc" % pekkoJdbcVersion,
  "org.apache.pekko" %% "pekko-http" % pekkoHttpVersion
)

val database = Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "org.flywaydb" % "flyway-core" % flywayVersion,
  "mysql" % "mysql-connector-java" % mysqlVersion,
  "com.h2database" % "h2" % h2Version,
  "org.postgresql" % "postgresql" % postgresqlVersion
)
val logging = Seq(
  "com.typesafe.scala-logging" %% "scala-logging" % scalaLoggingVersion,
  "ch.qos.logback" % "logback-classic" % logbackVersion,
  "org.codehaus.janino" % "janino" % janinoVersion,
  "net.logstash.logback" % "logstash-logback-encoder" % encoderVersion
)

val json = Seq(
  "com.github.pjfanning" %% "pekko-http-circe" % pekkoHttpJsonVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.jayway.jsonpath" % "json-path" % jsonPathVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
)

val yaml = Seq(
  "io.circe" %% "circe-yaml-v12" % circeYamlVersion
)

val other = Seq(
  "org.scala-lang" % "scala-compiler" % projectScalaVersion,
  "org.freemarker" % "freemarker" % freemarkerVersion,
  "org.apache.commons" % "commons-lang3" % lang3Version,
  "org.graalvm.js" % "js" % graalVmVersion,
  "org.graalvm.js" % "js-scriptengine" % graalVmVersion
)

val testDep = Seq(
  "org.scalactic" %% "scalactic" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "com.github.tomakehurst" % "wiremock-jre8" % wiremockVersion % Test,
  "org.apache.pekko" %% "pekko-testkit" % pekkoVersion % Test,
  "org.apache.pekko" %% "pekko-actor-testkit-typed" % pekkoVersion % Test,
  "org.apache.pekko" %% "pekko-http-testkit" % pekkoHttpVersion % Test,
  "org.apache.pekko" %% "pekko-stream-testkit" % pekkoVersion % Test,
  "org.apache.pekko" %% "pekko-persistence-testkit" % pekkoVersion % Test,
  "org.mockito" %% "mockito-scala" % mockitoScalaVersion % Test
)

val itTestDep = Seq(
  "org.scalactic" %% "scalactic" % scalacticVersion % Test,
  "org.scalatest" %% "scalatest" % scalacticVersion % Test,
  "org.mockito" %% "mockito-scala" % mockitoScalaVersion % Test
)

lazy val root = (project in file("."))
  .settings(CompilerSettings.settings)
  .settings(
    organization := "it.ldsoftware",
    name := "migra",
    Compile / mainClass := Some("it.ldsoftware.migra.MigraApp"),
    libraryDependencies ++= pekko ++ database ++ logging ++ json ++ yaml ++ other ++ testDep,
    Test / fork := true,
    Test / envVars := Map(
      "UNIT_DB_USER" -> "user",
      "UNIT_DB_PASS" -> "pass"
    )
  )

lazy val integration = (project in file("integration"))
  .settings(CompilerSettings.settings)
  .dependsOn(root)
  .settings(
    publish / skip := true,
    libraryDependencies ++= testDep ++ itTestDep,
    Test / fork := true,
    Test / envVars := Map(
      "UNIT_DB_USER" -> "user",
      "UNIT_DB_PASS" -> "pass"
    )
  )
