package it.ldsoftware.starling

import com.typesafe.config.ConfigFactory
import it.ldsoftware.starling.configuration.AppConfig

object StarlingApp extends App {

  val config = AppConfig(ConfigFactory.load())

  config.getMode match {
    case AppConfig.ServerMode     => processServer(config)
    case AppConfig.StandaloneMode => processStandalone(config, args)
    case _                        => throw new Error("STARLING_MODE env var not found!")
  }

  def processServer(config: AppConfig): Unit = {}

  def processStandalone(config: AppConfig, args: Array[String]): Unit = {}

}
