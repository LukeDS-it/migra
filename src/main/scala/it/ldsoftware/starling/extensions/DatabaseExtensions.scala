package it.ldsoftware.starling.extensions

import com.typesafe.config.Config
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import it.ldsoftware.starling.extensions.UsableExtensions.{LetOperations, MutateOperations}

import java.sql.Connection

object DatabaseExtensions {

  def getConnection(config: Config): Connection =
    new HikariConfig()
      .mutate { cfg =>
        cfg.setJdbcUrl(config.getString("jdbc-url"))
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
      .getConnection

}
