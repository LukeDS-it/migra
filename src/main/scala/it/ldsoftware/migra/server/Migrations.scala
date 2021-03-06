package it.ldsoftware.migra.server

import it.ldsoftware.migra.configuration.AppConfig
import org.flywaydb.core.Flyway
import org.flywaydb.core.api.output.MigrateResult

class Migrations(config: AppConfig) {

  private lazy val flyway: Flyway = Flyway
    .configure()
    .dataSource(config.dbUrl, config.dbUser, config.dbPass)
    .failOnMissingLocations(true)
    .load()

  def migrate(): MigrateResult = flyway.migrate()

}
