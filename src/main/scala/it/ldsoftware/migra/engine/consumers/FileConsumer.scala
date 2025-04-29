package it.ldsoftware.migra.engine.consumers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.migra.engine
import it.ldsoftware.migra.engine.*
import it.ldsoftware.migra.extensions.ConfigExtensions.ConfigOperations
import it.ldsoftware.migra.extensions.Interpolator.StringInterpolator
import it.ldsoftware.migra.extensions.UsableExtensions.LetOperations

import java.io.BufferedWriter
import scala.concurrent.{ExecutionContext, Future}

class FileConsumer(fileName: String, fileWriter: BufferedWriter, template: String, override val config: Config)(implicit
    val ec: ExecutionContext
) extends Consumer
    with LazyLogging {

  override def consumeSuccess(data: Extracted): Future[engine.ConsumerResult] =
    Future {
      (template <-- data).let { it =>
        fileWriter.write(it)
        fileWriter.newLine()
        fileWriter.flush()
        Consumed(s"FileConsumer wrote $it on $fileName")
      }
    }

}

object FileConsumer extends ConsumerBuilder {

  override def apply(config: Config, pc: ProcessContext): Consumer = {
    val fileName = config.getString("file")
    implicit val ec: ExecutionContext = pc.executionContext
    val writer = pc.openFile(fileName)
    config.getOptString("header").foreach { header =>
      writer.write(header)
      writer.newLine()
      writer.flush()
    }
    new FileConsumer(fileName, writer, config.getString("template"), config)
  }

}
