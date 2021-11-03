package it.ldsoftware.starling.engine

import akka.actor.ActorSystem
import akka.stream.Materializer
import akka.stream.scaladsl.{RunnableGraph, Sink}
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.extensions.UsableExtensions.UsableSource

import java.io.{File, PrintWriter}
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor}
import scala.io.Source
import scala.util.{Failure, Success}

class ProcessRunner extends LazyLogging {

  private val system = ActorSystem("standalone-system")
  implicit val ec: ExecutionContextExecutor = ExecutionContext.global
  implicit val mat: Materializer = Materializer(system)

  def startProcessFromFile(file: File): Unit =
    if (!file.exists()) {
      logger.error(s"Specified file ${file.getAbsolutePath} does not exist")
    } else {
      val manifest = Source.fromFile(file).use(_.getLines().mkString("\n"))
      logger.debug(s"Executing following plan:\n$manifest")

      val process = new ProcessFactory(4).generateProcess(manifest)

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
