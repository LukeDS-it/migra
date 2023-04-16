val akkaVersion = "2.6.19"
val akkaHttpVersion = "10.2.9"
val akkaJdbcVersion = "3.5.3"
val slickVersion = "3.3.3"
val mysqlVersion = "8.0.30"
val h2Version = "2.1.210"
val scalaLoggingVersion = "3.9.4"
val logbackVersion = "1.2.11"
val janinoVersion = "3.1.6"
val encoderVersion = "7.0.1"
val akkaHttpCirceVersion = "1.39.2"
val circeVersion = "0.14.1"
val freemarkerVersion = "2.3.31"
val scalacticVersion = "3.2.11"
val lang3Version = "3.12.0"
val jsonPathVersion = "2.7.0"
val jacksonVersion = "2.13.2"
val wiremockVersion = "2.32.0"
val scalamockVersion = "5.2.0"
val postgresqlVersion = "42.3.3"
val flywayVersion = "8.5.4"
val projectScalaVersion = "2.13.8"
val mockitoScalaVersion = "1.17.12"

val akka = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-actor-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-cluster-sharding-typed" % akkaVersion,
  "com.typesafe.akka" %% "akka-persistence-query" % akkaVersion,
  "com.typesafe.akka" %% "akka-serialization-jackson" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.github.dnvriend" %% "akka-persistence-jdbc" % akkaJdbcVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
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
  "de.heikoseeberger" %% "akka-http-circe" % akkaHttpCirceVersion,
  "io.circe" %% "circe-core" % circeVersion,
  "io.circe" %% "circe-generic" % circeVersion,
  "io.circe" %% "circe-parser" % circeVersion,
  "com.jayway.jsonpath" % "json-path" % jsonPathVersion,
  "com.fasterxml.jackson.core" % "jackson-core" % jacksonVersion,
  "com.fasterxml.jackson.module" %% "jackson-module-scala" % jacksonVersion
)

val other = Seq(
  "org.scala-lang" % "scala-compiler" % projectScalaVersion,
  "org.freemarker" % "freemarker" % freemarkerVersion,
  "org.apache.commons" % "commons-lang3" % lang3Version
)

val testDep = Seq(
  "org.scalactic" %% "scalactic" % scalacticVersion % "test,it",
  "org.scalatest" %% "scalatest" % scalacticVersion % "test,it",
  "com.github.tomakehurst" % "wiremock-jre8" % wiremockVersion % Test,
  "com.typesafe.akka" %% "akka-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-actor-testkit-typed" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-http-testkit" % akkaHttpVersion % Test,
  "com.typesafe.akka" %% "akka-stream-testkit" % akkaVersion % Test,
  "com.typesafe.akka" %% "akka-persistence-testkit" % akkaVersion % Test,
  "org.mockito" %% "mockito-scala" % mockitoScalaVersion % "test,it"
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    organization := "it.ldsoftware",
    name := "migra",
    scalaVersion := projectScalaVersion,
    Compile / mainClass := Some("it.ldsoftware.migra.MigraApp"),
    libraryDependencies ++= akka ++ database ++ logging ++ json ++ other ++ testDep,
    Test / fork := true,
    Test / envVars := Map(
      "UNIT_DB_USER" -> "user",
      "UNIT_DB_PASS" -> "pass"
    )
  )
