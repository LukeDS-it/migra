package it.ldsoftware.starling.configuration

import com.typesafe.config.Config

case class AppConfig(private val config: Config) {

  def getMode: String = config.getString("it.ldsoftware.starling.mode").toLowerCase

  lazy val serverPort: Int = config.getInt("it.ldsoftware.starling.server.port")

}

object AppConfig {
  val StandaloneMode = "standalone"
  val ServerMode = "server"
}
