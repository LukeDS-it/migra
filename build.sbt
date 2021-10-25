val akkaVersion = "2.6.17"
val akkaHttpVersion = "10.2.6"
val slickVersion = "3.3.3"
val mysqlVersion = "8.0.25"
val h2Version = "1.4.200"
val scalaLoggingVersion = "3.9.4"
val logbackVersion = "1.2.6"
val janinoVersion = "3.1.6"
val encoderVersion = "6.6"
val akkaHttpCirceVersion = "1.38.2"
val circeVersion = "0.14.1"
val freemarkerVersion = "2.3.31"
val scalacticVersion = "3.2.10"
val lang3Version = "3.12.0"

val akka = Seq(
  "com.typesafe.akka" %% "akka-stream" % akkaVersion,
  "com.typesafe.akka" %% "akka-slf4j" % akkaVersion,
  "com.typesafe.akka" %% "akka-http" % akkaHttpVersion
)

val database = Seq(
  "com.typesafe.slick" %% "slick" % slickVersion,
  "com.typesafe.slick" %% "slick-hikaricp" % slickVersion,
  "mysql" % "mysql-connector-java" % mysqlVersion,
  "com.h2database" % "h2" % h2Version
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
  "org.freemarker" % "freemarker" % freemarkerVersion,
  "org.apache.commons" % "commons-lang3" % lang3Version
)

val testDep = Seq(
  "org.scalactic" %% "scalactic" % scalacticVersion % "test,it",
  "org.scalatest" %% "scalatest" % scalacticVersion % "test,it"
)

lazy val root = (project in file("."))
  .configs(IntegrationTest)
  .settings(Defaults.itSettings)
  .settings(
    organization := "it.ldsoftware",
    name := "starling-migrate",
    scalaVersion := "2.13.6",
    Compile / mainClass := Some("it.ldsoftware.starling.StarlingApp"),
    libraryDependencies ++= akka ++ database ++ logging ++ json ++ other ++ testDep
  )
