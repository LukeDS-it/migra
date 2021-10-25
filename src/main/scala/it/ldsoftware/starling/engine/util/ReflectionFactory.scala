package it.ldsoftware.starling.engine.util

import com.typesafe.config.{Config, ConfigFactory}
import slick.jdbc.JdbcBackend.Database
import slick.jdbc.{DerbyProfile, H2Profile, HsqldbProfile, JdbcBackend, JdbcProfile, MySQLProfile, PostgresProfile, SQLiteProfile}

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

    val profile = getProfile(jdbcDriver)
    val db = getDatabase(jdbcUrl, jdbcDriver)

    (query, db, profile)
  }

  def getDatabase(jdbcUrl: String, jdbcDriver: String, user: String = null, password: String = null): Database = {
    val config =
      s"""
        |dbConf {
        |  url = "$jdbcUrl"
        |  driver = $jdbcDriver
        |}
        |""".stripMargin

    Database.forConfig("dbConf", ConfigFactory.parseString(config))
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
