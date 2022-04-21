package it.ldsoftware.migra.engine.consumers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.migra.engine._
import it.ldsoftware.migra.extensions.Interpolator._
import it.ldsoftware.migra.extensions.UsableExtensions.LetOperations

import scala.concurrent.{ExecutionContext, Future}

class PrinterConsumer(template: String, override val config: Config)(implicit val ec: ExecutionContext)
    extends Consumer
    with LazyLogging {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] =
    Future {
      (template <-- data).let { it =>
        logger.info(it)
        Consumed(s"PrinterConsumer printed $it")
      }
    }

}

object PrinterConsumer extends ConsumerBuilder {

  override def apply(config: Config, pc: ProcessContext): Consumer =
    new PrinterConsumer(config.getString("template"), config)(pc.executionContext)

}
