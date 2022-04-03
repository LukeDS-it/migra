package it.ldsoftware.starling

import com.typesafe.config.ConfigFactory
import slick.jdbc.JdbcBackend.Database
import slick.jdbc._

object DatabaseUtils {

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
