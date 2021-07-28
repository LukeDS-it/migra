package it.ldsoftware.starling.workers.consumers

import com.typesafe.config.Config
import com.typesafe.scalalogging.LazyLogging
import it.ldsoftware.starling.workers.model.{Consumed, ConsumerResult, Extracted}

import scala.concurrent.Future

import it.ldsoftware.starling.workers.tools.Interpolator._

class PrinterConsumer(template: String) extends Consumer with LazyLogging {

  override def consumeSuccess(data: Extracted): Future[ConsumerResult] = Future {
    logger.info(template interpolatedWith data)
    Consumed("PrinterConsumer")
  }

}

object PrinterConsumer extends ConsumerBuilder {

  override def apply(config: Config): Consumer = new PrinterConsumer(config.getString("template"))

}


