package it.ldsoftware.migra.extensions

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import it.ldsoftware.migra.extensions.UsableExtensions.{LetOperations, MutateOperations}
import ConfigExtensions._

import javax.sql.DataSource
import scala.collection.mutable

object DatabaseExtensions {

  private val dataSources = mutable.Map[String, DataSource]()

  def getDataSource(config: Config): DataSource = {
    val jdbcUrl = config.getString("jdbc-url")
    if (!dataSources.contains(jdbcUrl)) {
      dataSources.put(jdbcUrl, makeDataSource(config))
    }
    dataSources(jdbcUrl)
  }

  private def makeDataSource(config: Config): DataSource =
    new HikariConfig()
      .mutate { cfg =>
        cfg.setJdbcUrl(config.getString("jdbc-url"))
        config.getOptString("jdbc-driver").foreach { driver =>
          cfg.setDriverClassName(driver)
        }
        val (username, password) = CredentialManager.getCredentials(config)
        cfg.setUsername(username)
        cfg.setPassword(password)
        cfg.addDataSourceProperty("cachePrepStmts", "true")
        cfg.addDataSourceProperty("prepStmtCacheSize", "250")
        cfg.addDataSourceProperty("prepStmtCacheSqlLimit", "2048")
      }
      .let { cfg =>
        new HikariDataSource(cfg)
      }

}
