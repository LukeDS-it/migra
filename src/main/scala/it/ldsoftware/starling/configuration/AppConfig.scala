package it.ldsoftware.starling.configuration

import com.typesafe.config.Config

case class AppConfig(private val config: Config) {

  def getMode: String = config.getString("it.ldsoftware.starling.mode").toLowerCase

  lazy val serverPort: Int = config.getInt("it.ldsoftware.starling.server.port")

  lazy val dbUrl: String = config.getString("it.ldsoftware.starling.server.database.url")
  lazy val dbUser: String = config.getString("it.ldsoftware.starling.server.database.user")
  lazy val dbPass: String = config.getString("it.ldsoftware.starling.server.database.pass")

  lazy val parallelism: Int = config.getInt("it.ldsoftware.starling.par-level")
  lazy val maxScriptEngines: Int = config.getInt("it.ldsoftware.starling.max-script-engines")

}

object AppConfig {
  val StandaloneMode = "standalone"
  val ServerMode = "server"
}
