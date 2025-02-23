package it.ldsoftware.migra.engine

import org.apache.pekko.actor.ActorSystem
import org.apache.pekko.stream.Materializer
import org.apache.pekko.stream.scaladsl.{RunnableGraph, Sink}
import cats.implicits.catsSyntaxOptionId
import com.typesafe.scalalogging.{LazyLogging, Logger}
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.ProcessRunner.BiLogger
import it.ldsoftware.migra.engine.resolvers.LocalFileResolver
import it.ldsoftware.migra.extensions.IOExtensions.FileFromFileExtensions

import java.io.{BufferedWriter, File, FileWriter, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.util.{Failure, Success}

class ProcessRunner extends LazyLogging {

  private val system = ActorSystem("migra-studio")
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val mat: Materializer = Materializer(system)

  def startProcessFromFile(file: File, appConfig: AppConfig): Unit =
    if (!file.exists()) {
      logger.error(s"Specified file ${file.getAbsolutePath} does not exist")
    } else {
      val manifest = file.readFile
      logger.debug(s"Executing following plan:\n$manifest")
      val pc = ProcessContext(system, appConfig, new LocalFileResolver(file))

      val process = new ProcessFactory(appConfig.parallelism).generateProcess(manifest, pc)
      val processLogger = new BiLogger(logger, file.getAbsolutePath)

      val loggerSink = Sink.fold[ProcessStats, ConsumerResult](ProcessStats(0, 0)) {
        case (acc, Consumed(info)) =>
          processLogger.info(info)
          acc.withSuccess
        case (acc, NotConsumed(consumer, reason, data, err)) =>
          processLogger.error(s"$consumer could not consume $data: $reason", err)
          acc.withFailure
      }

      RunnableGraph
        .fromGraph(process.executionGraph(loggerSink))
        .run()
        .onComplete {
          case Success(stats) =>
            processLogger.info("-----------------------------------")
            processLogger.info("Process was completed with success.")
            processLogger.info("-----------------------------------")
            processLogger.info(s"${stats.consumed + stats.notConsumed} total elements consumed.")
            processLogger.info(s"${stats.consumed} successfully processed.")
            processLogger.info(s"${stats.notConsumed} processed with errors.")
            processLogger.info("-----------------------------------")
            processLogger.terminate()
            system.terminate()
          case Failure(exception) =>
            processLogger.info("-----------------------------------")
            processLogger.error("Could not complete the process due to an error", exception.some)
            processLogger.info("-----------------------------------")
            system.terminate()
        }
    }

}

object ProcessRunner {

  private class BiLogger(logger: Logger, descriptorPath: String) {
    private val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss")
    private val startDate = formatter.format(LocalDateTime.now())
    private val logFile = new File(s"$descriptorPath-$startDate.log")
    private val fileLogger = new BufferedWriter(new FileWriter(logFile))

    def info(msg: String): Unit = {
      logger.info(msg)
      fileLogger.write(msg)
      fileLogger.newLine()
      fileLogger.flush()
    }

    def error(msg: String, throwable: Option[Throwable]): Unit = {
      throwable match {
        case Some(err) => logger.error(msg, err)
        case None => logger.error(msg)
      }
      fileLogger.write(msg)
      fileLogger.newLine()
      fileLogger.flush()
    }

    def terminate(): Unit = fileLogger.close()
  }

}
