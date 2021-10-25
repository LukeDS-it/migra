package it.ldsoftware.starling.engine.util

import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc._
import it.ldsoftware.starling.extensions.ConfigExtensions._

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
    val db = getDatabase(jdbcUrl, jdbcDriver)

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
      config.getString("credentials.type") match {
        case "plain" => (config.getStringOrNull("credentials.user"), config.getStringOrNull("credentials.pass"))
        case "env"   => ???
        case "file"  => ???
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
