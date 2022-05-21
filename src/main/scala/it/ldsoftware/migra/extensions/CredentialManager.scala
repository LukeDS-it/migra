package it.ldsoftware.migra.extensions

import com.typesafe.config.{Config, ConfigFactory}
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.migra.extensions.IOExtensions.FileFromStringExtensions

object CredentialManager { // TODO put in PC so that we can get file configuration from relative pos

  def getCredentials(config: Config): (String, String) =
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
          getPlainCredentials(ConfigFactory.parseString(config.getString("credentials.file").readFile))
      }
    }

  def getToken(config: Config): String =
    if (!config.hasPath("credentials")) {
      null
    } else {
      def getPlainToken(config: Config) = config.getStringOrNull("credentials.token")

      config.getString("credentials.type") match {
        case "plain" =>
          getPlainToken(config)
        case "env" =>
          System.getenv(config.getString("credentials.token"))
        case "file" =>
          getPlainToken(ConfigFactory.parseString(config.getString("credentials.file").readFile))
      }
    }

}
