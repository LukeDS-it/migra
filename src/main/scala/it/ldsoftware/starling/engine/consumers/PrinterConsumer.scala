package it.ldsoftware.starling.engine.consumers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.engine.util.Interpolator._
import it.ldsoftware.starling.engine._

import scala.concurrent.Future

class PrinterConsumer(template: String) extends Consumer with LazyLogging {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] = Future {
    logger.info(template <-- data)
    Consumed("PrinterConsumer")
  }

}

object PrinterConsumer extends ConsumerBuilder {

  override def apply(config: Config): Consumer = new PrinterConsumer(config.getString("template"))

}


