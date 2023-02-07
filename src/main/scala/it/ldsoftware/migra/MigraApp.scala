package it.ldsoftware.migra

import akka.actor.typed.ActorSystem
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.ProcessRunner
import it.ldsoftware.migra.server.ServerBehavior

import java.io.File

object MigraApp extends App with LazyLogging {

  val config = AppConfig(ConfigFactory.load())
  run(config, args)

  def run(config: AppConfig, args: Array[String]): Unit =
    config.getMode match {
      case AppConfig.ServerMode     => processServer(config)
      case AppConfig.StandaloneMode => processStandalone(config, args)
      case _                        => throw new Error("MIGRA_MODE env var not found!")
    }

  private def processServer(config: AppConfig): Unit =
    ActorSystem[Nothing](ServerBehavior(config), "migra-studio")

  private def processStandalone(config: AppConfig, args: Array[String]): Unit = {
    logger.info("Starting in standalone mode")
    Option(args.head).map(new File(_)) match {
      case None       => logger.error("No descriptor provided, quitting.")
      case Some(file) => new ProcessRunner().startProcessFromFile(file, config)
    }
  }

}
