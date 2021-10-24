val akkaVersion = "2.6.15"
val akkaHttpVersion = "10.1.13"
val slickVersion = "3.3.3"
val connectorVersion = "8.0.19"
val scalaLoggingVersion = "3.9.3"
val logbackVersion = "1.2.3"
val janinoVersion = "3.1.4"
val encoderVersion = "6.6"
val akkaHttpCirceVersion = "1.36.0"
val circeVersion = "0.14.1"
val freemarkerVersion = "2.3.31"
val scalacticVersion = "3.2.10"

val akka = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
)

val database = Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "mysql" % "mysql-connector-java" % connectorVersion
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
  "io.circe" %% "circe-parser" % circeVersion
)

val other = Seq(
  "org.freemarker" % "freemarker" % freemarkerVersion
)

val testDep = Seq(
  "org.scalactic" %% "scalactic" % scalacticVersion,
  "org.scalatest" %% "scalatest" % scalacticVersion % "test"
)

lazy val root = (project in file("."))
  .settings(
    organization := "it.ldsoftware",
    name := "starling-migrate",
    scalaVersion := "2.13.6",
    Compile / mainClass := Some("it.ldsoftware.starling.StarlingApp"),
    libraryDependencies ++= akka ++ database ++ logging ++ json ++ other ++ testDep
  )
