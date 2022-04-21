package it.ldsoftware.migra.engine

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{RunnableGraph, Sink}
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.migra.configuration.AppConfig
import it.ldsoftware.migra.engine.resolvers.LocalFileResolver
import it.ldsoftware.migra.extensions.IOExtensions.FileFromFileExtensions

import java.io.{File, PrintWriter}
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

      val loggerSink = Sink.fold[String, ConsumerResult]("") {
        case (acc, Consumed(info)) =>
          logger.info(info)
          if (acc.isEmpty) info else s"$acc\n$info"
        case (acc, NotConsumed(consumer, reason, data, err)) =>
          logger.error(s"$consumer could not consume $data: $reason", err)
          val info = s"$consumer could not consume $data: $reason"
          if (acc.isEmpty) info else s"$acc\n$info"
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
    val formatter = DateTimeFormatter.ofPattern("YYYY-MM-dd-HH-mm-ss")
    val dateTime = formatter.format(LocalDateTime.now())
    val outputPath = s"$descriptorPath.executed[$dateTime]"
    val writer = new PrintWriter(new File(outputPath))
    writer.write(report)
    writer.close()
  }

}
