package it.ldsoftware.migra.configuration

import com.typesafe.config.Config

case class AppConfig(private val config: Config) {

  def getMode: String = config.getString("it.ldsoftware.migra.mode").toLowerCase

  lazy val serverPort: Int = config.getInt("it.ldsoftware.migra.server.port")

  lazy val dbUrl: String = config.getString("it.ldsoftware.migra.server.database.url")
  lazy val dbUser: String = config.getString("it.ldsoftware.migra.server.database.user")
  lazy val dbPass: String = config.getString("it.ldsoftware.migra.server.database.pass")

  lazy val parallelism: Int = config.getInt("it.ldsoftware.migra.par-level")
  lazy val maxScriptEngines: Int = config.getInt("it.ldsoftware.migra.max-script-engines")

}

object AppConfig {
  val StandaloneMode = "standalone"
  val ServerMode = "server"
}
