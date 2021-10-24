package it.ldsoftware.starling

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{RunnableGraph, Sink}
import com.typesafe.config.ConfigFactory
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.configuration.AppConfig
import it.ldsoftware.starling.engine.{Consumed, ConsumerResult, NotConsumed, ProcessFactory}

import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.Source
import scala.util.{Failure, Success}

object StarlingApp extends App with LazyLogging {

  val config = AppConfig(ConfigFactory.load())
  run(config, args)

  def run(config: AppConfig, args: Array[String]): Unit =
    config.getMode match {
      case AppConfig.ServerMode     => processServer(config)
      case AppConfig.StandaloneMode => processStandalone(args)
      case _                        => throw new Error("STARLING_MODE env var not found!")
    }

  private def processServer(config: AppConfig): Unit = {}

  private def processStandalone(args: Array[String]): Unit = {
    logger.info("Starting in standalone mode")
    Option(args.head).map(new File(_)) match {
      case None       => logger.error("No descriptor provided, quitting.")
      case Some(file) => startProcessFromFile(file)
    }
  }

  private def startProcessFromFile(file: File): Unit =
    if (!file.exists()) {
      logger.error(s"Specified file ${file.getAbsolutePath} does not exist")
    } else {
      val source = Source.fromFile(file)
      val manifest = source.getLines().mkString("\n")
      source.close()
      logger.debug(s"Executing following plan:\n$manifest")

      val system = ActorSystem("starling-system")
      implicit val ec: ExecutionContextExecutor = ExecutionContext.global
      implicit val mat: Materializer = Materializer(system)

      val process = new ProcessFactory(4).generateProcess(manifest)

      val loggerSink = Sink.fold[String, ConsumerResult]("") {
        case (acc, Consumed(info)) =>
          logger.info(info)
          s"$acc\n$info"
        case (acc, NotConsumed(consumer, reason, data, err)) =>
          logger.error(s"$consumer could not consume $data: $reason", err)
          s"$acc\n$consumer could not consume $data: $reason"
      }

      RunnableGraph
        .fromGraph(process.executionGraph(loggerSink))
        .run()
        .onComplete {
          case Success(value) =>
            logger.info("Process was completed with success. Log report has been generated.")
            writeOutput(file.getAbsolutePath, value)
            system.terminate()
          case Failure(exception) =>
            logger.error("Could not complete the process due to an error", exception)
            system.terminate()
        }
    }

  private def writeOutput(descriptorPath: String, report: String): Unit = {
    val formatter = DateTimeFormatter.ISO_DATE_TIME
    val dateTime = formatter.format(LocalDateTime.now())
    val outputPath = s"$descriptorPath.executed[$dateTime]"
    val writer = new PrintWriter(new File(outputPath))
    writer.write(report)
    writer.close()
  }

}
