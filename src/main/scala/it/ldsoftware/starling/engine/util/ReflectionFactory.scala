package it.ldsoftware.starling.engine.util

import com.typesafe.config.{Config, ConfigFactory}
import it.ldsoftware.starling.extensions.ConfigExtensions._
import it.ldsoftware.starling.extensions.UsableExtensions._
import slick.jdbc.JdbcBackend.Database
import slick.jdbc._

import scala.io.Source

object ReflectionFactory {

  def getBuilder[T](fullyQualifiedClassName: String): T = {
    val mirror = scala.reflect.runtime.universe.runtimeMirror(getClass.getClassLoader)
    val module = mirror.staticModule(fullyQualifiedClassName)
    mirror.reflectModule(module).instance.asInstanceOf[T]
  }

  def getDbInfo(config: Config): (String, JdbcBackend.Database, JdbcProfile) = {
    val query = config.getString("query")
    val jdbcUrl = config.getString("jdbc-url")
    val jdbcDriver = config.getString("jdbc-driver")
    val (username, password) = getCredentials(config)

    val profile = getProfile(jdbcDriver)
    val db = getDatabase(jdbcUrl, jdbcDriver, username, password)

    (query, db, profile)
  }

  def getDatabase(jdbcUrl: String, jdbcDriver: String, user: String = null, password: String = null): Database = {
    val ifUser = if (user != null) s"user = $user" else ""
    val ifPass = if (password != null) s"pass = $password" else ""

    val config =
      s"""
        |dbConf {
        |  url = "$jdbcUrl"
        |  driver = $jdbcDriver
        |  $ifUser
        |  $ifPass
        |}
        |""".stripMargin

    Database.forConfig("dbConf", ConfigFactory.parseString(config))
  }

  private[util] def getCredentials(config: Config): (String, String) =
    if (!config.hasPath("credentials")) {
      (null, null)
    } else {
      def getPlainCredentials(config: Config) =
        (config.getStringOrNull("credentials.user"), config.getStringOrNull("credentials.pass"))

      config.getString("credentials.type") match {
        case "plain" =>
          getPlainCredentials(config)
        case "env" =>
          (System.getenv(config.getString("credentials.user")), System.getenv(config.getString("credentials.pass")))
        case "file" =>
          Source.fromFile(config.getString("credentials.file")).use { it =>
            getPlainCredentials(ConfigFactory.parseString(it.getLines().mkString("\n")))
          }
      }
    }

  def getProfile(jdbcDriver: String): JdbcProfile =
    jdbcDriver match {
      case "org.apache.derby.jdbc.EmbeddedDriver" => DerbyProfile
      case "org.h2.Driver"                        => H2Profile
      case "org.hsqldb.jdbcDriver"                => HsqldbProfile
      case "com.mysql.jdbc.Driver"                => MySQLProfile
      case "org.postgresql.Driver"                => PostgresProfile
      case "org.sqlite.JDBC"                      => SQLiteProfile
    }

}
